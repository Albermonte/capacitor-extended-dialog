import UIKit

public enum DialogType {
    case alert
    case confirm
    case prompt
    case singleSelect
    case multiSelect
}

public class FullScreenDialogViewController: UIViewController {

    // MARK: - Callbacks

    var alertCallback: (() -> Void)?
    var confirmCallback: ((Bool) -> Void)?
    var promptCallback: ((String, Bool) -> Void)?
    var singleSelectCallback: ((String?, Bool) -> Void)?
    var multiSelectCallback: (([String], Bool) -> Void)?

    // MARK: - Properties

    private let dialogType: DialogType
    private let dialogTitle: String?
    private let message: String
    private let okButtonTitle: String
    private let cancelButtonTitle: String?
    private let inputPlaceholder: String?
    private let inputText: String?
    private let options: [SelectOption]?
    private var selectedValue: String?
    private var selectedValues: Set<String>
    private let styleOptions: DialogStyleOptions?

    private var inputTextField: UITextField?
    private var tableView: UITableView?
    private var dismissed = false
    private var buttonStackBottomConstraint: NSLayoutConstraint?

    // MARK: - UI Components

    private lazy var headerView: UIView = {
        let header = UIView()
        header.translatesAutoresizingMaskIntoConstraints = false
        return header
    }()

    private lazy var closeButtonView: UIButton = {
        let button = UIButton(type: .system)
        button.translatesAutoresizingMaskIntoConstraints = false
        let config = UIImage.SymbolConfiguration(pointSize: 14, weight: .bold)
        button.setImage(UIImage(systemName: "xmark", withConfiguration: config), for: .normal)
        button.tintColor = UIColor.darkGray
        button.backgroundColor = UIColor.white
        button.layer.cornerRadius = 22
        button.layer.shadowColor = UIColor.black.cgColor
        button.layer.shadowOpacity = 0.08
        button.layer.shadowOffset = CGSize(width: 0, height: 2)
        button.layer.shadowRadius = 4
        button.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
        return button
    }()

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = dialogTitle
        let fontSize = styleOptions?.titleFontSize ?? 18
        label.font = .systemFont(ofSize: fontSize, weight: .bold)
        label.textAlignment = .center
        if let titleColor = styleOptions?.titleColor {
            label.textColor = titleColor
        }
        return label
    }()

    private lazy var messageLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = message
        label.numberOfLines = 0
        let fontSize = styleOptions?.messageFontSize ?? 16
        label.font = .systemFont(ofSize: fontSize)
        label.textColor = styleOptions?.messageColor ?? .secondaryLabel
        return label
    }()

    private lazy var buttonStack: UIStackView = {
        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .horizontal
        stack.spacing = 12
        stack.distribution = .fillEqually
        return stack
    }()

    private lazy var cancelButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle(cancelButtonTitle ?? "Cancel", for: .normal)
        let fontSize = styleOptions?.buttonFontSize ?? 17
        button.titleLabel?.font = .systemFont(ofSize: fontSize, weight: .medium)
        button.layer.cornerRadius = 12
        button.layer.borderWidth = 1
        let cancelColor = styleOptions?.cancelButtonColor ?? .systemBlue
        button.setTitleColor(cancelColor, for: .normal)
        button.layer.borderColor = cancelColor.cgColor
        button.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
        return button
    }()

    private lazy var okButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle(okButtonTitle, for: .normal)
        button.setTitleColor(.white, for: .normal)
        let fontSize = styleOptions?.buttonFontSize ?? 17
        button.titleLabel?.font = .systemFont(ofSize: fontSize, weight: .semibold)
        let buttonColor = styleOptions?.buttonColor ?? .systemBlue
        button.backgroundColor = buttonColor
        button.layer.cornerRadius = 12
        button.addTarget(self, action: #selector(okTapped), for: .touchUpInside)
        return button
    }()

    private lazy var blurView: UIVisualEffectView = {
        let blurEffect = UIBlurEffect(style: .systemThinMaterial)
        let view = UIVisualEffectView(effect: blurEffect)
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

    // MARK: - Init

    init(
        dialogType: DialogType,
        dialogTitle: String?,
        message: String,
        okButtonTitle: String,
        cancelButtonTitle: String?,
        inputPlaceholder: String?,
        inputText: String?,
        options: [SelectOption]?,
        selectedValue: String?,
        selectedValues: [String]?,
        styleOptions: DialogStyleOptions? = nil
    ) {
        self.dialogType = dialogType
        self.dialogTitle = dialogTitle
        self.message = message
        self.okButtonTitle = okButtonTitle
        self.cancelButtonTitle = cancelButtonTitle
        self.inputPlaceholder = inputPlaceholder
        self.inputText = inputText
        self.options = options
        self.selectedValue = selectedValue
        self.selectedValues = Set(selectedValues ?? [])
        self.styleOptions = styleOptions
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // MARK: - Lifecycle

    public override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        applyLiquidGlassStyle()
        setupKeyboardHandling()
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    // MARK: - Keyboard Handling

    private func setupKeyboardHandling() {
        // Add tap gesture to dismiss keyboard
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
        tapGesture.cancelsTouchesInView = false
        view.addGestureRecognizer(tapGesture)

        // Listen for keyboard notifications
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillShow(_:)),
            name: UIResponder.keyboardWillShowNotification,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillHide(_:)),
            name: UIResponder.keyboardWillHideNotification,
            object: nil
        )
    }

    @objc private func dismissKeyboard() {
        view.endEditing(true)
    }

    @objc private func keyboardWillShow(_ notification: Notification) {
        guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect,
              let duration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else {
            return
        }

        let keyboardHeight = keyboardFrame.height
        let safeAreaBottom = view.safeAreaInsets.bottom

        UIView.animate(withDuration: duration) {
            self.buttonStackBottomConstraint?.constant = -(keyboardHeight - safeAreaBottom + 16)
            self.view.layoutIfNeeded()
        }
    }

    @objc private func keyboardWillHide(_ notification: Notification) {
        guard let duration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? TimeInterval else {
            return
        }

        UIView.animate(withDuration: duration) {
            self.buttonStackBottomConstraint?.constant = -24
            self.view.layoutIfNeeded()
        }
    }

    // MARK: - Setup

    private func setupUI() {
        // Apply custom background color or default light gray background like iOS Calendar
        if let bgColor = styleOptions?.backgroundColor {
            view.backgroundColor = bgColor
        } else {
            view.backgroundColor = UIColor(red: 0.95, green: 0.95, blue: 0.97, alpha: 1.0)
        }

        // Add blur background
        view.insertSubview(blurView, at: 0)
        NSLayoutConstraint.activate([
            blurView.topAnchor.constraint(equalTo: view.topAnchor),
            blurView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            blurView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            blurView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])

        // Header with close button and title
        view.addSubview(headerView)
        headerView.addSubview(closeButtonView)
        headerView.addSubview(titleLabel)

        NSLayoutConstraint.activate([
            headerView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 12),
            headerView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            headerView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            headerView.heightAnchor.constraint(equalToConstant: 64),

            closeButtonView.leadingAnchor.constraint(equalTo: headerView.leadingAnchor, constant: 16),
            closeButtonView.centerYAnchor.constraint(equalTo: headerView.centerYAnchor),
            closeButtonView.widthAnchor.constraint(equalToConstant: 44),
            closeButtonView.heightAnchor.constraint(equalToConstant: 44),

            titleLabel.centerXAnchor.constraint(equalTo: headerView.centerXAnchor),
            titleLabel.centerYAnchor.constraint(equalTo: headerView.centerYAnchor)
        ])

        // Content container
        let contentView = UIView()
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentView)

        NSLayoutConstraint.activate([
            contentView.topAnchor.constraint(equalTo: headerView.bottomAnchor, constant: 8),
            contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24)
        ])

        // Message
        contentView.addSubview(messageLabel)
        NSLayoutConstraint.activate([
            messageLabel.topAnchor.constraint(equalTo: contentView.topAnchor),
            messageLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            messageLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor)
        ])

        // Type-specific content
        var lastView: UIView = messageLabel

        switch dialogType {
        case .prompt:
            let textField = createTextField()
            contentView.addSubview(textField)
            NSLayoutConstraint.activate([
                textField.topAnchor.constraint(equalTo: messageLabel.bottomAnchor, constant: 24),
                textField.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
                textField.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
                textField.heightAnchor.constraint(equalToConstant: 48)
            ])
            inputTextField = textField
            lastView = textField

        case .singleSelect, .multiSelect:
            let table = createTableView()
            contentView.addSubview(table)

            // Calculate preferred height based on number of options (52pt per row)
            let rowCount = options?.count ?? 0
            let preferredHeight = CGFloat(rowCount) * 52
            let preferredHeightConstraint = table.heightAnchor.constraint(equalToConstant: min(preferredHeight, 300))
            preferredHeightConstraint.priority = .defaultHigh

            NSLayoutConstraint.activate([
                table.topAnchor.constraint(equalTo: messageLabel.bottomAnchor, constant: 16),
                table.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
                table.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
                table.heightAnchor.constraint(lessThanOrEqualToConstant: 300),
                preferredHeightConstraint
            ])
            tableView = table
            lastView = table

        default:
            break
        }

        // Buttons (add before contentView bottom constraint so we can reference buttonStack)
        view.addSubview(buttonStack)

        if dialogType != .alert && cancelButtonTitle != nil {
            buttonStack.addArrangedSubview(cancelButton)
        }
        buttonStack.addArrangedSubview(okButton)

        buttonStackBottomConstraint = buttonStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -24)

        NSLayoutConstraint.activate([
            buttonStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            buttonStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            buttonStackBottomConstraint!,
            buttonStack.heightAnchor.constraint(equalToConstant: 50)
        ])

        // Content bottom constraint - ensure content stays above buttons
        NSLayoutConstraint.activate([
            contentView.bottomAnchor.constraint(equalTo: lastView.bottomAnchor),
            contentView.bottomAnchor.constraint(lessThanOrEqualTo: buttonStack.topAnchor, constant: -16)
        ])
    }

    private func createTextField() -> UITextField {
        let textField = UITextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.placeholder = inputPlaceholder
        textField.text = inputText
        textField.borderStyle = .roundedRect
        textField.font = .systemFont(ofSize: 16)
        textField.backgroundColor = .white
        textField.layer.cornerRadius = 8
        textField.becomeFirstResponder()
        return textField
    }

    private func createTableView() -> UITableView {
        let table = UITableView(frame: .zero, style: .plain)
        table.translatesAutoresizingMaskIntoConstraints = false
        table.delegate = self
        table.dataSource = self
        table.register(UITableViewCell.self, forCellReuseIdentifier: "OptionCell")
        table.layer.cornerRadius = 12
        table.clipsToBounds = true
        table.backgroundColor = .white
        table.separatorInset = UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16)
        return table
    }

    private func applyLiquidGlassStyle() {
        // Apply Liquid Glass styling
        if #available(iOS 26, *) {
            // iOS 26 automatically applies Liquid Glass
            // Additional customization can be done here
        }

        // Apply corner curve to buttons
        cancelButton.layer.cornerCurve = .continuous
        okButton.layer.cornerCurve = .continuous
        closeButtonView.layer.cornerCurve = .continuous
    }

    // MARK: - Actions

    @objc private func cancelTapped() {
        handleCancel()
        dismiss(animated: true)
    }

    @objc private func okTapped() {
        handleConfirm()
        dismiss(animated: true)
    }

    private func handleConfirm() {
        guard !dismissed else { return }
        dismissed = true

        switch dialogType {
        case .alert:
            alertCallback?()
        case .confirm:
            confirmCallback?(true)
        case .prompt:
            let value = inputTextField?.text ?? ""
            promptCallback?(value, false)
        case .singleSelect:
            singleSelectCallback?(selectedValue, false)
        case .multiSelect:
            multiSelectCallback?(Array(selectedValues), false)
        }
    }

    private func handleCancel() {
        guard !dismissed else { return }
        dismissed = true

        switch dialogType {
        case .alert:
            alertCallback?()
        case .confirm:
            confirmCallback?(false)
        case .prompt:
            promptCallback?("", true)
        case .singleSelect:
            singleSelectCallback?(nil, true)
        case .multiSelect:
            multiSelectCallback?([], true)
        }
    }
}

// MARK: - UITableViewDelegate & DataSource

extension FullScreenDialogViewController: UITableViewDelegate, UITableViewDataSource {

    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return options?.count ?? 0
    }

    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "OptionCell", for: indexPath)
        guard let option = options?[indexPath.row] else { return cell }

        cell.textLabel?.text = option.label

        switch dialogType {
        case .singleSelect:
            cell.accessoryType = option.value == selectedValue ? .checkmark : .none
        case .multiSelect:
            cell.accessoryType = selectedValues.contains(option.value) ? .checkmark : .none
        default:
            break
        }

        cell.backgroundColor = .clear
        return cell
    }

    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        guard let option = options?[indexPath.row] else { return }

        switch dialogType {
        case .singleSelect:
            selectedValue = option.value
            tableView.reloadData()
        case .multiSelect:
            if selectedValues.contains(option.value) {
                selectedValues.remove(option.value)
            } else {
                selectedValues.insert(option.value)
            }
            tableView.reloadRows(at: [indexPath], with: .automatic)
        default:
            break
        }
    }

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 52
    }
}
