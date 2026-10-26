import Foundation
import Capacitor

@objc(ExtendedDialogPlugin)
public class ExtendedDialogPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "ExtendedDialogPlugin"
    public let jsName = "ExtendedDialog"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "alert", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "confirm", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "prompt", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "singleSelect", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "multiSelect", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "sheet", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "messageSheet", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = ExtendedDialog()

    // One dialog at a time: a new dialog or an elapsed `timeout` closes the open
    // one, which settles with its cancel result. Only touched on the main thread.
    private var activeCall: CAPPluginCall?
    private var activeCancelResult: [String: Any] = [:]
    private var pendingDialog: (present: () -> Void, cancel: () -> Void)?
    private var isDismissing = false

    func presentDialog(_ call: CAPPluginCall, cancelResult: [String: Any], show: @escaping (@escaping ([String: Any]) -> Void) -> Void) {
        DispatchQueue.main.async {
            self.pendingDialog?.cancel()
            self.pendingDialog = (
                present: {
                    let callbackId = call.callbackId!
                    self.activeCall = call
                    self.activeCancelResult = cancelResult
                    if let timeout = call.getDouble("timeout"), timeout > 0 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + timeout / 1000) {
                            if self.activeCall?.callbackId == callbackId {
                                self.dismissActive {}
                            }
                        }
                    }
                    show { result in self.settle(callbackId, result) }
                },
                cancel: { call.resolve(cancelResult) }
            )
            // Competing dismissal completions can otherwise overwrite an unresolved call.
            guard !self.isDismissing else { return }
            self.isDismissing = true
            self.dismissActive {
                self.isDismissing = false
                let pending = self.pendingDialog
                self.pendingDialog = nil
                pending?.present()
            }
        }
    }

    private func settle(_ callbackId: String, _ result: [String: Any]) {
        guard let call = activeCall, call.callbackId == callbackId else { return }
        activeCall = nil
        call.resolve(result)
    }

    private func dismissActive(then next: @escaping () -> Void) {
        if let call = activeCall {
            settle(call.callbackId, activeCancelResult)
        }
        closePresentedDialog(then: next)
    }

    func closePresentedDialog(then next: @escaping () -> Void) {
        guard let dialog = implementation.presentedDialog, dialog.presentingViewController != nil else {
            return next()
        }
        // UIKit drops presentations and dismissals started mid-transition, so wait for it.
        if dialog.isBeingPresented || dialog.isBeingDismissed,
           dialog.transitionCoordinator?.animate(alongsideTransition: nil, completion: { _ in self.closePresentedDialog(then: next) }) == true {
            return
        }
        dialog.dismiss(animated: true, completion: next)
    }

    private func extractStyleOptions(_ call: CAPPluginCall) -> DialogStyleOptions {
        return DialogStyleOptions(
            buttonColor: call.getString("buttonColor"),
            cancelButtonColor: call.getString("cancelButtonColor"),
            titleColor: call.getString("titleColor"),
            messageColor: call.getString("messageColor"),
            backgroundColor: call.getString("backgroundColor"),
            titleFontSize: call.getDouble("titleFontSize"),
            messageFontSize: call.getDouble("messageFontSize"),
            buttonFontSize: call.getDouble("buttonFontSize"),
            contentButtonSpacing: call.getDouble("contentButtonSpacing"),
            headerLogoCornerRadius: call.getDouble("headerLogoCornerRadius")
        )
    }

    @objc func alert(_ call: CAPPluginCall) {
        let title = call.getString("title")
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        let buttonTitle = call.getString("buttonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: [:]) { done in
            self.implementation.showAlert(
                title: title,
                message: message,
                buttonTitle: buttonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) {
                done([:])
            }
        }
    }

    @objc func confirm(_ call: CAPPluginCall) {
        let title = call.getString("title")
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        let okButtonTitle = call.getString("okButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["value": false]) { done in
            self.implementation.showConfirm(
                title: title,
                message: message,
                okButtonTitle: okButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) { value in
                done(["value": value])
            }
        }
    }

    @objc func prompt(_ call: CAPPluginCall) {
        let title = call.getString("title")
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        let okButtonTitle = call.getString("okButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let inputPlaceholder = call.getString("inputPlaceholder")
        let inputText = call.getString("inputText")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let focusInput = call.getBool("focusInput") ?? false
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["value": "", "cancelled": true]) { done in
            self.implementation.showPrompt(
                title: title,
                message: message,
                okButtonTitle: okButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                inputPlaceholder: inputPlaceholder,
                inputText: inputText,
                fullscreen: fullscreen,
                focusInput: focusInput,
                styleOptions: styleOptions
            ) { value, cancelled in
                done([
                    "value": value,
                    "cancelled": cancelled
                ])
            }
        }
    }

    @objc func singleSelect(_ call: CAPPluginCall) {
        let title = call.getString("title")
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        guard let optionsArray = call.getArray("options") as? [[String: String]] else {
            call.reject("options is required")
            return
        }
        let selectedValue = call.getString("selectedValue")
        let okButtonTitle = call.getString("okButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["value": NSNull(), "cancelled": true]) { done in
            self.implementation.showSingleSelect(
                title: title,
                message: message,
                options: optionsArray,
                selectedValue: selectedValue,
                okButtonTitle: okButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) { value, cancelled in
                done([
                    "value": value as Any,
                    "cancelled": cancelled
                ])
            }
        }
    }

    @objc func multiSelect(_ call: CAPPluginCall) {
        let title = call.getString("title")
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        guard let optionsArray = call.getArray("options") as? [[String: String]] else {
            call.reject("options is required")
            return
        }
        let selectedValues = call.getArray("selectedValues") as? [String]
        let okButtonTitle = call.getString("okButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["values": [String](), "cancelled": true]) { done in
            self.implementation.showMultiSelect(
                title: title,
                message: message,
                options: optionsArray,
                selectedValues: selectedValues,
                okButtonTitle: okButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) { values, cancelled in
                done([
                    "values": values,
                    "cancelled": cancelled
                ])
            }
        }
    }

    @objc func sheet(_ call: CAPPluginCall) {
        guard let title = call.getString("title") else {
            call.reject("title is required")
            return
        }
        guard let rowsArray = call.getArray("rows") as? [[String: String]] else {
            call.reject("rows is required")
            return
        }
        let subtitle = call.getString("subtitle")
        let headerLogo = call.getString("headerLogo")
        let confirmButtonTitle = call.getString("confirmButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["confirmed": false]) { done in
            self.implementation.showSheet(
                title: title,
                subtitle: subtitle,
                headerLogo: headerLogo,
                rows: rowsArray,
                confirmButtonTitle: confirmButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) { confirmed in
                done(["confirmed": confirmed])
            }
        }
    }

    @objc func messageSheet(_ call: CAPPluginCall) {
        guard let title = call.getString("title") else {
            call.reject("title is required")
            return
        }
        guard let message = call.getString("message") else {
            call.reject("message is required")
            return
        }
        let subtitle = call.getString("subtitle")
        let headerLogo = call.getString("headerLogo")
        let confirmButtonTitle = call.getString("confirmButtonTitle")
        let cancelButtonTitle = call.getString("cancelButtonTitle")
        let mode = call.getString("mode") ?? "basic"
        let fullscreen = mode == "fullscreen"
        let styleOptions = extractStyleOptions(call)

        presentDialog(call, cancelResult: ["confirmed": false]) { done in
            self.implementation.showMessageSheet(
                title: title,
                subtitle: subtitle,
                message: message,
                headerLogo: headerLogo,
                confirmButtonTitle: confirmButtonTitle,
                cancelButtonTitle: cancelButtonTitle,
                fullscreen: fullscreen,
                styleOptions: styleOptions
            ) { confirmed in
                done(["confirmed": confirmed])
            }
        }
    }
}
