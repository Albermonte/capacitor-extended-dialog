import Foundation
import UIKit

public struct SelectOption {
    let label: String
    let value: String
}

public struct DialogStyleOptions {
    var buttonColor: UIColor?
    var cancelButtonColor: UIColor?
    var titleColor: UIColor?
    var messageColor: UIColor?
    var backgroundColor: UIColor?
    var titleFontSize: CGFloat?
    var messageFontSize: CGFloat?
    var buttonFontSize: CGFloat?

    init(
        buttonColor: String? = nil,
        cancelButtonColor: String? = nil,
        titleColor: String? = nil,
        messageColor: String? = nil,
        backgroundColor: String? = nil,
        titleFontSize: Double? = nil,
        messageFontSize: Double? = nil,
        buttonFontSize: Double? = nil
    ) {
        self.buttonColor = Self.parseColor(buttonColor)
        self.cancelButtonColor = Self.parseColor(cancelButtonColor)
        self.titleColor = Self.parseColor(titleColor)
        self.messageColor = Self.parseColor(messageColor)
        self.backgroundColor = Self.parseColor(backgroundColor)
        self.titleFontSize = titleFontSize.map { CGFloat($0) }
        self.messageFontSize = messageFontSize.map { CGFloat($0) }
        self.buttonFontSize = buttonFontSize.map { CGFloat($0) }
    }

    private static func parseColor(_ hex: String?) -> UIColor? {
        guard let hex = hex, !hex.isEmpty else { return nil }
        var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")

        var rgb: UInt64 = 0
        guard Scanner(string: hexSanitized).scanHexInt64(&rgb) else { return nil }

        if hexSanitized.count == 6 {
            return UIColor(
                red: CGFloat((rgb & 0xFF0000) >> 16) / 255.0,
                green: CGFloat((rgb & 0x00FF00) >> 8) / 255.0,
                blue: CGFloat(rgb & 0x0000FF) / 255.0,
                alpha: 1.0
            )
        } else if hexSanitized.count == 8 {
            return UIColor(
                red: CGFloat((rgb & 0xFF000000) >> 24) / 255.0,
                green: CGFloat((rgb & 0x00FF0000) >> 16) / 255.0,
                blue: CGFloat((rgb & 0x0000FF00) >> 8) / 255.0,
                alpha: CGFloat(rgb & 0x000000FF) / 255.0
            )
        }
        return nil
    }

    var hasStyles: Bool {
        return buttonColor != nil || cancelButtonColor != nil ||
               titleColor != nil || messageColor != nil ||
               backgroundColor != nil || titleFontSize != nil ||
               messageFontSize != nil || buttonFontSize != nil
    }
}

@objc public class ExtendedDialog: NSObject {

    public typealias AlertCallback = () -> Void
    public typealias ConfirmCallback = (Bool) -> Void
    public typealias PromptCallback = (String, Bool) -> Void
    public typealias SingleSelectCallback = (String?, Bool) -> Void
    public typealias MultiSelectCallback = ([String], Bool) -> Void

    // MARK: - Alert

    public func showAlert(
        title: String?,
        message: String,
        buttonTitle: String?,
        fullscreen: Bool,
        styleOptions: DialogStyleOptions? = nil,
        callback: @escaping AlertCallback
    ) {
        DispatchQueue.main.async {
            if fullscreen {
                self.showFullScreenAlert(title: title, message: message, buttonTitle: buttonTitle, styleOptions: styleOptions, callback: callback)
            } else {
                self.showBasicAlert(title: title, message: message, buttonTitle: buttonTitle, styleOptions: styleOptions, callback: callback)
            }
        }
    }

    private func showBasicAlert(title: String?, message: String, buttonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping AlertCallback) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)

        let action = UIAlertAction(title: buttonTitle ?? "OK", style: .default) { _ in
            callback()
        }
        if let color = styleOptions?.buttonColor {
            action.setValue(color, forKey: "titleTextColor")
        }
        alert.addAction(action)

        applyLiquidGlassStyle(to: alert)
        applyMessageStyle(to: alert, styleOptions: styleOptions)
        presentAlert(alert)
    }

    private func showFullScreenAlert(title: String?, message: String, buttonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping AlertCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .alert,
            dialogTitle: title,
            message: message,
            okButtonTitle: buttonTitle ?? "OK",
            cancelButtonTitle: nil,
            inputPlaceholder: nil,
            inputText: nil,
            options: nil,
            selectedValue: nil,
            selectedValues: nil,
            styleOptions: styleOptions
        )
        vc.alertCallback = callback
        presentFullScreen(vc)
    }

    // MARK: - Confirm

    public func showConfirm(
        title: String?,
        message: String,
        okButtonTitle: String?,
        cancelButtonTitle: String?,
        fullscreen: Bool,
        styleOptions: DialogStyleOptions? = nil,
        callback: @escaping ConfirmCallback
    ) {
        DispatchQueue.main.async {
            if fullscreen {
                self.showFullScreenConfirm(title: title, message: message, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            } else {
                self.showBasicConfirm(title: title, message: message, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            }
        }
    }

    private func showBasicConfirm(title: String?, message: String, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping ConfirmCallback) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)

        let cancelAction = UIAlertAction(title: cancelButtonTitle ?? "Cancel", style: .cancel) { _ in
            callback(false)
        }
        let okAction = UIAlertAction(title: okButtonTitle ?? "OK", style: .default) { _ in
            callback(true)
        }

        if let color = styleOptions?.cancelButtonColor {
            cancelAction.setValue(color, forKey: "titleTextColor")
        }
        if let color = styleOptions?.buttonColor {
            okAction.setValue(color, forKey: "titleTextColor")
        }

        alert.addAction(cancelAction)
        alert.addAction(okAction)

        applyLiquidGlassStyle(to: alert)
        applyMessageStyle(to: alert, styleOptions: styleOptions)
        presentAlert(alert)
    }

    private func showFullScreenConfirm(title: String?, message: String, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping ConfirmCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .confirm,
            dialogTitle: title,
            message: message,
            okButtonTitle: okButtonTitle ?? "OK",
            cancelButtonTitle: cancelButtonTitle ?? "Cancel",
            inputPlaceholder: nil,
            inputText: nil,
            options: nil,
            selectedValue: nil,
            selectedValues: nil,
            styleOptions: styleOptions
        )
        vc.confirmCallback = callback
        presentFullScreen(vc)
    }

    // MARK: - Prompt

    public func showPrompt(
        title: String?,
        message: String,
        okButtonTitle: String?,
        cancelButtonTitle: String?,
        inputPlaceholder: String?,
        inputText: String?,
        fullscreen: Bool,
        styleOptions: DialogStyleOptions? = nil,
        callback: @escaping PromptCallback
    ) {
        DispatchQueue.main.async {
            if fullscreen {
                self.showFullScreenPrompt(title: title, message: message, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, inputPlaceholder: inputPlaceholder, inputText: inputText, styleOptions: styleOptions, callback: callback)
            } else {
                self.showBasicPrompt(title: title, message: message, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, inputPlaceholder: inputPlaceholder, inputText: inputText, styleOptions: styleOptions, callback: callback)
            }
        }
    }

    private func showBasicPrompt(title: String?, message: String, okButtonTitle: String?, cancelButtonTitle: String?, inputPlaceholder: String?, inputText: String?, styleOptions: DialogStyleOptions?, callback: @escaping PromptCallback) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)

        alert.addTextField { textField in
            textField.placeholder = inputPlaceholder
            textField.text = inputText
        }

        let cancelAction = UIAlertAction(title: cancelButtonTitle ?? "Cancel", style: .cancel) { _ in
            callback("", true)
        }
        let okAction = UIAlertAction(title: okButtonTitle ?? "OK", style: .default) { _ in
            let value = alert.textFields?.first?.text ?? ""
            callback(value, false)
        }

        if let color = styleOptions?.cancelButtonColor {
            cancelAction.setValue(color, forKey: "titleTextColor")
        }
        if let color = styleOptions?.buttonColor {
            okAction.setValue(color, forKey: "titleTextColor")
        }

        alert.addAction(cancelAction)
        alert.addAction(okAction)

        applyLiquidGlassStyle(to: alert)
        applyMessageStyle(to: alert, styleOptions: styleOptions)
        presentAlert(alert)
    }

    private func showFullScreenPrompt(title: String?, message: String, okButtonTitle: String?, cancelButtonTitle: String?, inputPlaceholder: String?, inputText: String?, styleOptions: DialogStyleOptions?, callback: @escaping PromptCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .prompt,
            dialogTitle: title,
            message: message,
            okButtonTitle: okButtonTitle ?? "OK",
            cancelButtonTitle: cancelButtonTitle ?? "Cancel",
            inputPlaceholder: inputPlaceholder,
            inputText: inputText,
            options: nil,
            selectedValue: nil,
            selectedValues: nil,
            styleOptions: styleOptions
        )
        vc.promptCallback = callback
        presentFullScreen(vc)
    }

    // MARK: - Single Select

    public func showSingleSelect(
        title: String?,
        message: String,
        options: [[String: String]],
        selectedValue: String?,
        okButtonTitle: String?,
        cancelButtonTitle: String?,
        fullscreen: Bool,
        styleOptions: DialogStyleOptions? = nil,
        callback: @escaping SingleSelectCallback
    ) {
        DispatchQueue.main.async {
            let selectOptions = options.compactMap { dict -> SelectOption? in
                guard let label = dict["label"], let value = dict["value"] else { return nil }
                return SelectOption(label: label, value: value)
            }

            if fullscreen {
                self.showFullScreenSingleSelect(title: title, message: message, options: selectOptions, selectedValue: selectedValue, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            } else {
                self.showBasicSingleSelect(title: title, message: message, options: selectOptions, selectedValue: selectedValue, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            }
        }
    }

    private func showBasicSingleSelect(title: String?, message: String, options: [SelectOption], selectedValue: String?, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping SingleSelectCallback) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .actionSheet)

        for option in options {
            let style: UIAlertAction.Style = option.value == selectedValue ? .default : .default
            let action = UIAlertAction(title: option.label, style: style) { _ in
                callback(option.value, false)
            }
            if option.value == selectedValue {
                action.setValue(true, forKey: "checked")
            }
            if let color = styleOptions?.buttonColor {
                action.setValue(color, forKey: "titleTextColor")
            }
            alert.addAction(action)
        }

        let cancelAction = UIAlertAction(title: cancelButtonTitle ?? "Cancel", style: .cancel) { _ in
            callback(nil, true)
        }
        if let color = styleOptions?.cancelButtonColor {
            cancelAction.setValue(color, forKey: "titleTextColor")
        }
        alert.addAction(cancelAction)

        applyLiquidGlassStyle(to: alert)
        applyMessageStyle(to: alert, styleOptions: styleOptions)
        presentAlert(alert)
    }

    private func showFullScreenSingleSelect(title: String?, message: String, options: [SelectOption], selectedValue: String?, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping SingleSelectCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .singleSelect,
            dialogTitle: title,
            message: message,
            okButtonTitle: okButtonTitle ?? "OK",
            cancelButtonTitle: cancelButtonTitle ?? "Cancel",
            inputPlaceholder: nil,
            inputText: nil,
            options: options,
            selectedValue: selectedValue,
            selectedValues: nil,
            styleOptions: styleOptions
        )
        vc.singleSelectCallback = callback
        presentFullScreen(vc)
    }

    // MARK: - Multi Select

    public func showMultiSelect(
        title: String?,
        message: String,
        options: [[String: String]],
        selectedValues: [String]?,
        okButtonTitle: String?,
        cancelButtonTitle: String?,
        fullscreen: Bool,
        styleOptions: DialogStyleOptions? = nil,
        callback: @escaping MultiSelectCallback
    ) {
        DispatchQueue.main.async {
            let selectOptions = options.compactMap { dict -> SelectOption? in
                guard let label = dict["label"], let value = dict["value"] else { return nil }
                return SelectOption(label: label, value: value)
            }

            if fullscreen {
                self.showFullScreenMultiSelect(title: title, message: message, options: selectOptions, selectedValues: selectedValues, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            } else {
                self.showBasicMultiSelect(title: title, message: message, options: selectOptions, selectedValues: selectedValues, okButtonTitle: okButtonTitle, cancelButtonTitle: cancelButtonTitle, styleOptions: styleOptions, callback: callback)
            }
        }
    }

    private func showBasicMultiSelect(title: String?, message: String, options: [SelectOption], selectedValues: [String]?, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping MultiSelectCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .multiSelect,
            dialogTitle: title,
            message: message,
            okButtonTitle: okButtonTitle ?? "OK",
            cancelButtonTitle: cancelButtonTitle ?? "Cancel",
            inputPlaceholder: nil,
            inputText: nil,
            options: options,
            selectedValue: nil,
            selectedValues: selectedValues,
            styleOptions: styleOptions
        )
        vc.multiSelectCallback = callback
        presentBasicSheet(vc)
    }

    private func showFullScreenMultiSelect(title: String?, message: String, options: [SelectOption], selectedValues: [String]?, okButtonTitle: String?, cancelButtonTitle: String?, styleOptions: DialogStyleOptions?, callback: @escaping MultiSelectCallback) {
        let vc = FullScreenDialogViewController(
            dialogType: .multiSelect,
            dialogTitle: title,
            message: message,
            okButtonTitle: okButtonTitle ?? "OK",
            cancelButtonTitle: cancelButtonTitle ?? "Cancel",
            inputPlaceholder: nil,
            inputText: nil,
            options: options,
            selectedValue: nil,
            selectedValues: selectedValues,
            styleOptions: styleOptions
        )
        vc.multiSelectCallback = callback
        presentFullScreen(vc)
    }

    // MARK: - Helpers

    private func applyLiquidGlassStyle(to alert: UIAlertController) {
        // Apply Liquid Glass styling for iOS 26+
        if #available(iOS 26, *) {
            // iOS 26 automatically applies Liquid Glass styling to system controls
            // Additional customization can be applied here if needed
        } else {
            // For older iOS versions, apply a similar visual style
            if let view = alert.view {
                view.layer.cornerRadius = 28.0
                view.layer.cornerCurve = .continuous
                view.clipsToBounds = true
            }
        }

        // Ensure dialog appears on top
        alert.view.layer.zPosition = CGFloat.greatestFiniteMagnitude
    }

    private func applyMessageStyle(to alert: UIAlertController, styleOptions: DialogStyleOptions?) {
        guard let styleOptions = styleOptions, let fontSize = styleOptions.messageFontSize else { return }

        // Access the message label using KVC (undocumented but commonly used)
        if let message = alert.message {
            let attributedMessage = NSMutableAttributedString(
                string: message,
                attributes: [
                    .font: UIFont.systemFont(ofSize: fontSize)
                ]
            )
            alert.setValue(attributedMessage, forKey: "attributedMessage")
        }
    }

    private func presentAlert(_ alert: UIAlertController) {
        guard let viewController = getTopViewController() else { return }

        // Configure popover for iPad
        if let popover = alert.popoverPresentationController {
            popover.sourceView = viewController.view
            popover.sourceRect = CGRect(x: viewController.view.bounds.midX, y: viewController.view.bounds.midY, width: 0, height: 0)
            popover.permittedArrowDirections = []
        }

        viewController.present(alert, animated: true)
    }

    private func presentFullScreen(_ viewController: UIViewController) {
        guard let topVC = getTopViewController() else { return }

        viewController.modalPresentationStyle = .fullScreen
        viewController.modalTransitionStyle = .coverVertical
        topVC.present(viewController, animated: true)
    }

    private func presentBasicSheet(_ viewController: UIViewController) {
        guard let topVC = getTopViewController() else { return }

        viewController.modalPresentationStyle = .pageSheet
        viewController.modalTransitionStyle = .coverVertical

        if #available(iOS 15.0, *) {
            if let sheet = viewController.sheetPresentationController {
                sheet.detents = [.medium(), .large()]
                sheet.prefersGrabberVisible = true
                sheet.prefersScrollingExpandsWhenScrolledToEdge = false
            }
        }

        topVC.present(viewController, animated: true)
    }

    private func getTopViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }),
              var topController = window.rootViewController else {
            return nil
        }

        while let presentedViewController = topController.presentedViewController {
            topController = presentedViewController
        }

        return topController
    }
}
