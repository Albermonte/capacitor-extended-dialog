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
        CAPPluginMethod(name: "multiSelect", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = ExtendedDialog()

    private func extractStyleOptions(_ call: CAPPluginCall) -> DialogStyleOptions {
        return DialogStyleOptions(
            buttonColor: call.getString("buttonColor"),
            cancelButtonColor: call.getString("cancelButtonColor"),
            titleColor: call.getString("titleColor"),
            messageColor: call.getString("messageColor"),
            backgroundColor: call.getString("backgroundColor"),
            titleFontSize: call.getDouble("titleFontSize"),
            messageFontSize: call.getDouble("messageFontSize"),
            buttonFontSize: call.getDouble("buttonFontSize")
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

        implementation.showAlert(
            title: title,
            message: message,
            buttonTitle: buttonTitle,
            fullscreen: fullscreen,
            styleOptions: styleOptions
        ) {
            call.resolve()
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

        implementation.showConfirm(
            title: title,
            message: message,
            okButtonTitle: okButtonTitle,
            cancelButtonTitle: cancelButtonTitle,
            fullscreen: fullscreen,
            styleOptions: styleOptions
        ) { value in
            call.resolve(["value": value])
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
        let styleOptions = extractStyleOptions(call)

        implementation.showPrompt(
            title: title,
            message: message,
            okButtonTitle: okButtonTitle,
            cancelButtonTitle: cancelButtonTitle,
            inputPlaceholder: inputPlaceholder,
            inputText: inputText,
            fullscreen: fullscreen,
            styleOptions: styleOptions
        ) { value, cancelled in
            call.resolve([
                "value": value,
                "cancelled": cancelled
            ])
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

        implementation.showSingleSelect(
            title: title,
            message: message,
            options: optionsArray,
            selectedValue: selectedValue,
            okButtonTitle: okButtonTitle,
            cancelButtonTitle: cancelButtonTitle,
            fullscreen: fullscreen,
            styleOptions: styleOptions
        ) { value, cancelled in
            call.resolve([
                "value": value as Any,
                "cancelled": cancelled
            ])
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

        implementation.showMultiSelect(
            title: title,
            message: message,
            options: optionsArray,
            selectedValues: selectedValues,
            okButtonTitle: okButtonTitle,
            cancelButtonTitle: cancelButtonTitle,
            fullscreen: fullscreen,
            styleOptions: styleOptions
        ) { values, cancelled in
            call.resolve([
                "values": values,
                "cancelled": cancelled
            ])
        }
    }
}
