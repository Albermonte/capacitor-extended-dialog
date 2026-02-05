import UIKit

public enum DialogType {
    case alert
    case confirm
    case prompt
    case singleSelect
    case multiSelect
    case sheet
    case messageSheet
}

public class FullScreenDialogViewController: UIViewController {

    // MARK: - Callbacks

    var alertCallback: (() -> Void)?
    var confirmCallback: ((Bool) -> Void)?
    var promptCallback: ((String, Bool) -> Void)?
    var singleSelectCallback: ((String?, Bool) -> Void)?
    var multiSelectCallback: (([String], Bool) -> Void)?
    var sheetCallback: ((Bool) -> Void)?

    // MARK: - Sheet Properties

    var headerLogo: String?
    var sheetRows: [SheetRow]?

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
    private let focusInput: Bool
    private let styleOptions: DialogStyleOptions?

    private var inputTextField: UITextField?
    private var tableView: UITableView?
    private var dismissed = false
    private var buttonStackBottomConstraint: NSLayoutConstraint?
    private var contentContainer: UIView?

    // MARK: - UI Components

    private lazy var headerView: UIView = {
        let header = UIView()
        header.translatesAutoresizingMaskIntoConstraints = false
        return header
    }()

    private lazy var closeButtonView: UIButton = {
        let button: UIButton
        if #available(iOS 26, *) {
            var config = UIButton.Configuration.glass()
            let symbolConfig = UIImage.SymbolConfiguration(pointSize: 14, weight: .bold)
            config.image = UIImage(systemName: "xmark", withConfiguration: symbolConfig)
            config.baseForegroundColor = .label
            let glassButton = UIButton(configuration: config)
            glassButton.translatesAutoresizingMaskIntoConstraints = false
            glassButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
            button = glassButton
        } else {
            let legacyButton = UIButton(type: .system)
            legacyButton.translatesAutoresizingMaskIntoConstraints = false
            let config = UIImage.SymbolConfiguration(pointSize: 14, weight: .bold)
            legacyButton.setImage(UIImage(systemName: "xmark", withConfiguration: config), for: .normal)
            legacyButton.tintColor = .label
            legacyButton.backgroundColor = .systemBackground
            legacyButton.layer.cornerRadius = 22
            legacyButton.layer.cornerCurve = .continuous
            legacyButton.layer.shadowColor = UIColor.black.cgColor
            legacyButton.layer.shadowOpacity = 0.08
            legacyButton.layer.shadowOffset = CGSize(width: 0, height: 2)
            legacyButton.layer.shadowRadius = 4
            legacyButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
            button = legacyButton
        }
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
        let button: UIButton
        if #available(iOS 26, *) {
            var config = UIButton.Configuration.glass()
            config.title = cancelButtonTitle ?? "Cancel"
            config.baseForegroundColor = styleOptions?.cancelButtonColor ?? .label
            config.cornerStyle = .capsule
            let fontSize = styleOptions?.buttonFontSize ?? 17
            config.titleTextAttributesTransformer = UIConfigurationTextAttributesTransformer { incoming in
                var outgoing = incoming
                outgoing.font = .systemFont(ofSize: fontSize, weight: .medium)
                return outgoing
            }
            let glassButton = UIButton(configuration: config)
            glassButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
            button = glassButton
        } else {
            let legacyButton = UIButton(type: .system)
            legacyButton.setTitle(cancelButtonTitle ?? "Cancel", for: .normal)
            let fontSize = styleOptions?.buttonFontSize ?? 17
            legacyButton.titleLabel?.font = .systemFont(ofSize: fontSize, weight: .medium)
            let cancelColor = styleOptions?.cancelButtonColor ?? .systemBlue
            legacyButton.setTitleColor(cancelColor, for: .normal)
            legacyButton.layer.cornerRadius = 25
            legacyButton.layer.cornerCurve = .continuous
            legacyButton.layer.borderWidth = 1
            legacyButton.layer.borderColor = cancelColor.cgColor
            legacyButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
            button = legacyButton
        }
        return button
    }()

    private lazy var okButton: UIButton = {
        let button: UIButton
        if #available(iOS 26, *) {
            var config = UIButton.Configuration.filled()
            config.title = okButtonTitle
            config.baseForegroundColor = .white
            config.baseBackgroundColor = styleOptions?.buttonColor ?? .systemBlue
            config.cornerStyle = .capsule
            let fontSize = styleOptions?.buttonFontSize ?? 17
            config.titleTextAttributesTransformer = UIConfigurationTextAttributesTransformer { incoming in
                var outgoing = incoming
                outgoing.font = .systemFont(ofSize: fontSize, weight: .semibold)
                return outgoing
            }
            let glassButton = UIButton(configuration: config)
            glassButton.addTarget(self, action: #selector(okTapped), for: .touchUpInside)
            button = glassButton
        } else {
            let legacyButton = UIButton(type: .system)
            legacyButton.setTitle(okButtonTitle, for: .normal)
            legacyButton.setTitleColor(.white, for: .normal)
            let fontSize = styleOptions?.buttonFontSize ?? 17
            legacyButton.titleLabel?.font = .systemFont(ofSize: fontSize, weight: .semibold)
            let buttonColor = styleOptions?.buttonColor ?? .systemBlue
            legacyButton.backgroundColor = buttonColor
            legacyButton.layer.cornerRadius = 25
            legacyButton.layer.cornerCurve = .continuous
            legacyButton.addTarget(self, action: #selector(okTapped), for: .touchUpInside)
            button = legacyButton
        }
        return button
    }()

    private lazy var blurView: UIVisualEffectView = {
        let effect = UIBlurEffect(style: .systemUltraThinMaterial)
        let view = UIVisualEffectView(effect: effect)
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
        focusInput: Bool = false,
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
        self.focusInput = focusInput
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
        calculatePreferredContentSize()
    }

    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()

        guard modalPresentationStyle == .pageSheet else { return }

        let previousHeight = preferredContentSize.height
        calculatePreferredContentSize()

        if #available(iOS 16.0, *) {
            if let sheet = sheetPresentationController,
               abs(preferredContentSize.height - previousHeight) > 0.5 {
                sheet.invalidateDetents()
            }
        }
    }

    private func calculatePreferredContentSize() {
        view.layoutIfNeeded()

        let safeTop = view.safeAreaInsets.top
        let safeBottom = view.safeAreaInsets.bottom

        let isSheetLike = dialogType == .sheet || dialogType == .messageSheet
        let headerTop: CGFloat = (isSheetLike && modalPresentationStyle == .pageSheet) ? 20 : 12
        let headerHeight: CGFloat = (isSheetLike && modalPresentationStyle == .pageSheet) ? 12 : 64
        let spacingAfterHeader: CGFloat = 8

        // Measure content height from layout to include table/message in multi-select
        var contentHeight: CGFloat = 0
        if let container = contentContainer {
            container.layoutIfNeeded()
            let fallbackWidth = max(view.bounds.width - 48, 0)
            let targetWidth = container.bounds.width > 0 ? container.bounds.width : fallbackWidth
            let targetSize = CGSize(width: targetWidth, height: UIView.layoutFittingCompressedSize.height)
            contentHeight = container.systemLayoutSizeFitting(
                targetSize,
                withHorizontalFittingPriority: .required,
                verticalFittingPriority: .fittingSizeLevel
            ).height
        }

        if contentHeight <= 0 {
            for subview in view.subviews {
                if let scrollView = subview as? UIScrollView {
                    contentHeight = scrollView.contentSize.height
                    break
                }
            }
        }

        var spacingToButtons: CGFloat = styleOptions?.contentButtonSpacing ?? 16
        let buttonStackHeight: CGFloat = (isSheetLike && cancelButtonTitle != nil) ? 112 : 50
        let bottomPadding: CGFloat = 4
        let grabberAllowance: CGFloat = 20

        // For sheets with ≤4 rows and no explicit spacing, add extra spacing to reach ~50% screen height
        if dialogType == .sheet && styleOptions?.contentButtonSpacing == nil {
            let rowCount = sheetRows?.count ?? 0
            if rowCount <= 4 {
                let screenHeight = UIScreen.main.bounds.height
                let targetHeight = screenHeight * 0.5
                let baseHeight = safeTop + headerTop + headerHeight + spacingAfterHeader
                    + contentHeight + spacingToButtons + buttonStackHeight + bottomPadding
                    + safeBottom + grabberAllowance
                if targetHeight > baseHeight {
                    spacingToButtons += targetHeight - baseHeight
                }
            }
        }

        let totalHeight = safeTop + headerTop + headerHeight + spacingAfterHeader
            + contentHeight + spacingToButtons + buttonStackHeight + bottomPadding
            + safeBottom + grabberAllowance

        preferredContentSize = CGSize(width: 0, height: totalHeight)
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
            self.buttonStackBottomConstraint?.constant = -4
            self.view.layoutIfNeeded()
        }
    }

    // MARK: - Setup

    private func setupUI() {
        let isSheetLike = dialogType == .sheet || dialogType == .messageSheet
        // Apply background: custom color, or blur for sheets, or solid for fullscreen
        if let bgColor = styleOptions?.backgroundColor {
            view.backgroundColor = bgColor
        } else if modalPresentationStyle == .pageSheet {
            // Sheets use blur so content peeks through
            view.backgroundColor = .clear
            view.insertSubview(blurView, at: 0)
            NSLayoutConstraint.activate([
                blurView.topAnchor.constraint(equalTo: view.topAnchor),
                blurView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                blurView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                blurView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
            ])
        } else {
            // Fullscreen dialogs use solid background
            view.backgroundColor = .systemGroupedBackground
        }

        // Header with close button and title
        view.addSubview(headerView)
        headerView.addSubview(closeButtonView)
        headerView.addSubview(titleLabel)

        let headerTop: CGFloat = (isSheetLike && modalPresentationStyle == .pageSheet) ? 20 : 12
        let headerHeight: CGFloat = (isSheetLike && modalPresentationStyle == .pageSheet) ? 12 : 64

        NSLayoutConstraint.activate([
            headerView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: headerTop),
            headerView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            headerView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            headerView.heightAnchor.constraint(equalToConstant: headerHeight),

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
        contentContainer = contentView

        NSLayoutConstraint.activate([
            contentView.topAnchor.constraint(equalTo: headerView.bottomAnchor, constant: 8),
            contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24)
        ])

        // Message (hidden for sheet dialogs - title is shown in content area)
        contentView.addSubview(messageLabel)
        if isSheetLike {
            messageLabel.isHidden = true
            titleLabel.isHidden = true // Also hide header title for sheets
            // Hide close button in basic sheet mode (.pageSheet presentation)
            if modalPresentationStyle == .pageSheet {
                closeButtonView.isHidden = true
            }
        }
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

        case .sheet:
            // For sheet, use contentView.topAnchor since messageLabel is hidden
            lastView = createSheetContent(in: contentView, below: contentView)

        case .messageSheet:
            // For message sheet, use contentView.topAnchor since messageLabel is hidden
            lastView = createMessageSheetContent(in: contentView, below: contentView)

        default:
            break
        }

        // Buttons (add before contentView bottom constraint so we can reference buttonStack)
        view.addSubview(buttonStack)

        // For sheet dialogs: vertical layout with primary button first, cancel below
        if isSheetLike {
            buttonStack.axis = .vertical
            buttonStack.distribution = .fill
            buttonStack.addArrangedSubview(okButton)
            if cancelButtonTitle != nil {
                configureCancelButtonForSheet()
                buttonStack.addArrangedSubview(cancelButton)
            }
        } else {
            // For other dialogs: horizontal layout with cancel first
            if dialogType != .alert && cancelButtonTitle != nil {
                buttonStack.addArrangedSubview(cancelButton)
            }
            buttonStack.addArrangedSubview(okButton)
        }

        buttonStackBottomConstraint = buttonStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -4)

        // Calculate button stack height: 50 for single button or horizontal layout, 112 for vertical (two 50pt buttons + 12pt spacing)
        let buttonStackHeight: CGFloat = (isSheetLike && cancelButtonTitle != nil) ? 112 : 50

        NSLayoutConstraint.activate([
            buttonStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            buttonStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            buttonStackBottomConstraint!,
            buttonStack.heightAnchor.constraint(equalToConstant: buttonStackHeight)
        ])

        // For sheet dialogs, wrap contentView in a scroll view so rows are scrollable
        if isSheetLike {
            // Remove contentView from its current superview and re-add inside a scroll view
            contentView.removeFromSuperview()

            let scrollView = UIScrollView()
            scrollView.translatesAutoresizingMaskIntoConstraints = false
            scrollView.showsVerticalScrollIndicator = true
            scrollView.alwaysBounceVertical = false
            view.addSubview(scrollView)

            scrollView.addSubview(contentView)

            NSLayoutConstraint.activate([
                // Scroll view frame: between header and buttons
                scrollView.topAnchor.constraint(equalTo: headerView.bottomAnchor, constant: 8),
                scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
                scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
                scrollView.bottomAnchor.constraint(equalTo: buttonStack.topAnchor, constant: -(styleOptions?.contentButtonSpacing ?? 16)),

                // Content view pinned to scroll view's content guide
                contentView.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
                contentView.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor),
                contentView.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor),
                contentView.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),

                // Content width matches scroll view frame (no horizontal scrolling)
                contentView.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor),

                // Content view bottom matches its last subview
                contentView.bottomAnchor.constraint(equalTo: lastView.bottomAnchor)
            ])
        } else {
            // Content bottom constraint - ensure content stays above buttons
            NSLayoutConstraint.activate([
                contentView.bottomAnchor.constraint(equalTo: lastView.bottomAnchor),
                contentView.bottomAnchor.constraint(lessThanOrEqualTo: buttonStack.topAnchor, constant: -(styleOptions?.contentButtonSpacing ?? 16))
            ])
        }
    }

    private func createTextField() -> UITextField {
        let textField = UITextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.placeholder = inputPlaceholder
        textField.text = inputText
        textField.font = .systemFont(ofSize: 16)
        textField.borderStyle = .roundedRect
        textField.backgroundColor = .secondarySystemGroupedBackground
        textField.layer.cornerRadius = 8

        if focusInput {
            textField.becomeFirstResponder()
        }
        return textField
    }

    private func createTableView() -> UITableView {
        let table = UITableView(frame: .zero, style: .plain)
        table.translatesAutoresizingMaskIntoConstraints = false
        table.delegate = self
        table.dataSource = self
        table.register(UITableViewCell.self, forCellReuseIdentifier: "OptionCell")
        table.layer.cornerRadius = 12
        table.layer.cornerCurve = .continuous
        table.clipsToBounds = true
        table.separatorInset = UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16)
        table.backgroundColor = .secondarySystemGroupedBackground

        return table
    }

    // MARK: - Sheet Content

    private func createSheetContent(in container: UIView, below topView: UIView) -> UIView {
        let sheetContainer = UIStackView()
        sheetContainer.translatesAutoresizingMaskIntoConstraints = false
        sheetContainer.axis = .vertical
        sheetContainer.spacing = 0
        sheetContainer.alignment = .fill
        container.addSubview(sheetContainer)

        // Header logo (centered, 48pt for basic, 64pt for fullscreen)
        if let logoUrl = headerLogo, !logoUrl.isEmpty {
            let logoSize: CGFloat = modalPresentationStyle == .fullScreen ? 64 : 48
            let logoView = UIImageView()
            logoView.translatesAutoresizingMaskIntoConstraints = false
            logoView.contentMode = .scaleAspectFit

            // Apply corner radius (default 8pt, -1 for circle)
            let radiusPt: CGFloat
            if let customRadius = styleOptions?.headerLogoCornerRadius {
                if customRadius < 0 {
                    // -1 means full circle: radius = half of logo size
                    radiusPt = logoSize / 2
                } else {
                    radiusPt = customRadius
                }
            } else {
                radiusPt = 8
            }
            logoView.layer.cornerRadius = radiusPt
            logoView.layer.cornerCurve = .continuous
            logoView.clipsToBounds = true

            let logoWrapper = UIView()
            logoWrapper.translatesAutoresizingMaskIntoConstraints = false
            logoWrapper.addSubview(logoView)

            NSLayoutConstraint.activate([
                logoView.centerXAnchor.constraint(equalTo: logoWrapper.centerXAnchor),
                logoView.topAnchor.constraint(equalTo: logoWrapper.topAnchor),
                logoView.bottomAnchor.constraint(equalTo: logoWrapper.bottomAnchor),
                logoView.widthAnchor.constraint(equalToConstant: logoSize),
                logoView.heightAnchor.constraint(equalToConstant: logoSize)
            ])

            sheetContainer.addArrangedSubview(logoWrapper)
            sheetContainer.setCustomSpacing(16, after: logoWrapper)
            loadImage(from: logoUrl, into: logoView)
        }

        // Title (centered in sheet content, not in header for sheet type)
        let sheetTitleLabel = UILabel()
        sheetTitleLabel.translatesAutoresizingMaskIntoConstraints = false
        sheetTitleLabel.text = dialogTitle
        let fontSize = styleOptions?.titleFontSize ?? 20
        sheetTitleLabel.font = .systemFont(ofSize: fontSize, weight: .semibold)
        sheetTitleLabel.textAlignment = .center
        if let titleColor = styleOptions?.titleColor {
            sheetTitleLabel.textColor = titleColor
        }
        sheetContainer.addArrangedSubview(sheetTitleLabel)
        sheetContainer.setCustomSpacing(24, after: sheetTitleLabel)

        // Rows container with rounded background
        let rowsBackground = UIView()
        rowsBackground.translatesAutoresizingMaskIntoConstraints = false
        rowsBackground.backgroundColor = .secondarySystemGroupedBackground
        var rowsCornerRadius: CGFloat = 12
        if #available(iOS 26, *) {
            rowsCornerRadius = 16
        }
        rowsBackground.layer.cornerRadius = rowsCornerRadius
        rowsBackground.layer.cornerCurve = .continuous
        rowsBackground.clipsToBounds = true

        let rowsStack = UIStackView()
        rowsStack.translatesAutoresizingMaskIntoConstraints = false
        rowsStack.axis = .vertical
        rowsStack.spacing = 0
        rowsStack.alignment = .fill

        rowsBackground.addSubview(rowsStack)
        NSLayoutConstraint.activate([
            rowsStack.topAnchor.constraint(equalTo: rowsBackground.topAnchor),
            rowsStack.leadingAnchor.constraint(equalTo: rowsBackground.leadingAnchor),
            rowsStack.trailingAnchor.constraint(equalTo: rowsBackground.trailingAnchor),
            rowsStack.bottomAnchor.constraint(equalTo: rowsBackground.bottomAnchor)
        ])

        if let rows = sheetRows {
            for (index, row) in rows.enumerated() {
                let rowView = createSheetRow(row: row, isLast: index == rows.count - 1)
                rowsStack.addArrangedSubview(rowView)
            }
        }

        sheetContainer.addArrangedSubview(rowsBackground)

        // If topView is the container itself, use topAnchor directly
        if topView === container {
            NSLayoutConstraint.activate([
                sheetContainer.topAnchor.constraint(equalTo: container.topAnchor),
                sheetContainer.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                sheetContainer.trailingAnchor.constraint(equalTo: container.trailingAnchor)
            ])
        } else {
            NSLayoutConstraint.activate([
                sheetContainer.topAnchor.constraint(equalTo: topView.bottomAnchor, constant: 16),
                sheetContainer.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                sheetContainer.trailingAnchor.constraint(equalTo: container.trailingAnchor)
            ])
        }

        return sheetContainer
    }

    private func createMessageSheetContent(in container: UIView, below topView: UIView) -> UIView {
        let sheetContainer = UIStackView()
        sheetContainer.translatesAutoresizingMaskIntoConstraints = false
        sheetContainer.axis = .vertical
        sheetContainer.spacing = 0
        sheetContainer.alignment = .fill
        container.addSubview(sheetContainer)

        // Header logo (centered, 48pt for basic, 64pt for fullscreen)
        if let logoUrl = headerLogo, !logoUrl.isEmpty {
            let logoSize: CGFloat = modalPresentationStyle == .fullScreen ? 64 : 48
            let logoView = UIImageView()
            logoView.translatesAutoresizingMaskIntoConstraints = false
            logoView.contentMode = .scaleAspectFit

            // Apply corner radius (default 8pt, -1 for circle)
            let radiusPt: CGFloat
            if let customRadius = styleOptions?.headerLogoCornerRadius {
                if customRadius < 0 {
                    // -1 means full circle: radius = half of logo size
                    radiusPt = logoSize / 2
                } else {
                    radiusPt = customRadius
                }
            } else {
                radiusPt = 8
            }
            logoView.layer.cornerRadius = radiusPt
            logoView.layer.cornerCurve = .continuous
            logoView.clipsToBounds = true

            let logoWrapper = UIView()
            logoWrapper.translatesAutoresizingMaskIntoConstraints = false
            logoWrapper.addSubview(logoView)

            NSLayoutConstraint.activate([
                logoView.centerXAnchor.constraint(equalTo: logoWrapper.centerXAnchor),
                logoView.topAnchor.constraint(equalTo: logoWrapper.topAnchor),
                logoView.bottomAnchor.constraint(equalTo: logoWrapper.bottomAnchor),
                logoView.widthAnchor.constraint(equalToConstant: logoSize),
                logoView.heightAnchor.constraint(equalToConstant: logoSize)
            ])

            sheetContainer.addArrangedSubview(logoWrapper)
            sheetContainer.setCustomSpacing(16, after: logoWrapper)
            loadImage(from: logoUrl, into: logoView)
        }

        // Title (centered)
        let sheetTitleLabel = UILabel()
        sheetTitleLabel.translatesAutoresizingMaskIntoConstraints = false
        sheetTitleLabel.text = dialogTitle
        let titleFontSize = styleOptions?.titleFontSize ?? 20
        sheetTitleLabel.font = .systemFont(ofSize: titleFontSize, weight: .semibold)
        sheetTitleLabel.textAlignment = .center
        if let titleColor = styleOptions?.titleColor {
            sheetTitleLabel.textColor = titleColor
        }
        sheetContainer.addArrangedSubview(sheetTitleLabel)
        sheetContainer.setCustomSpacing(16, after: sheetTitleLabel)

        // Message container with background
        let messageBackground = UIView()
        messageBackground.translatesAutoresizingMaskIntoConstraints = false
        messageBackground.backgroundColor = .secondarySystemGroupedBackground
        var messageCornerRadius: CGFloat = 12
        if #available(iOS 26, *) {
            messageCornerRadius = 16
        }
        messageBackground.layer.cornerRadius = messageCornerRadius
        messageBackground.layer.cornerCurve = .continuous
        messageBackground.clipsToBounds = true

        let messageLabel = UILabel()
        messageLabel.translatesAutoresizingMaskIntoConstraints = false
        messageLabel.text = message
        let messageFontSize = styleOptions?.messageFontSize ?? 16
        messageLabel.font = .systemFont(ofSize: messageFontSize)
        messageLabel.textColor = styleOptions?.messageColor ?? .secondaryLabel
        messageLabel.numberOfLines = 0
        messageLabel.lineBreakMode = .byWordWrapping
        messageLabel.textAlignment = .left

        messageBackground.addSubview(messageLabel)
        NSLayoutConstraint.activate([
            messageLabel.topAnchor.constraint(equalTo: messageBackground.topAnchor, constant: 16),
            messageLabel.leadingAnchor.constraint(equalTo: messageBackground.leadingAnchor, constant: 16),
            messageLabel.trailingAnchor.constraint(equalTo: messageBackground.trailingAnchor, constant: -16),
            messageLabel.bottomAnchor.constraint(equalTo: messageBackground.bottomAnchor, constant: -16)
        ])

        sheetContainer.addArrangedSubview(messageBackground)

        // If topView is the container itself, use topAnchor directly
        if topView === container {
            NSLayoutConstraint.activate([
                sheetContainer.topAnchor.constraint(equalTo: container.topAnchor),
                sheetContainer.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                sheetContainer.trailingAnchor.constraint(equalTo: container.trailingAnchor)
            ])
        } else {
            NSLayoutConstraint.activate([
                sheetContainer.topAnchor.constraint(equalTo: topView.bottomAnchor, constant: 16),
                sheetContainer.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                sheetContainer.trailingAnchor.constraint(equalTo: container.trailingAnchor)
            ])
        }

        return sheetContainer
    }

    private func createSheetRow(row: SheetRow, isLast: Bool) -> UIView {
        let rowView = UIView()
        rowView.translatesAutoresizingMaskIntoConstraints = false

        let contentStack = UIStackView()
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        contentStack.axis = .horizontal
        contentStack.spacing = 12
        contentStack.alignment = .center
        rowView.addSubview(contentStack)

        NSLayoutConstraint.activate([
            contentStack.topAnchor.constraint(equalTo: rowView.topAnchor, constant: 16),
            contentStack.leadingAnchor.constraint(equalTo: rowView.leadingAnchor, constant: 16),
            contentStack.trailingAnchor.constraint(equalTo: rowView.trailingAnchor, constant: -16),
            contentStack.bottomAnchor.constraint(equalTo: rowView.bottomAnchor, constant: -16)
        ])

        // Row logo (optional, 24pt)
        if let logoUrl = row.logo, !logoUrl.isEmpty {
            let logoView = UIImageView()
            logoView.translatesAutoresizingMaskIntoConstraints = false
            logoView.contentMode = .scaleAspectFit
            logoView.layer.cornerRadius = 4
            logoView.layer.cornerCurve = .continuous
            logoView.clipsToBounds = true

            NSLayoutConstraint.activate([
                logoView.widthAnchor.constraint(equalToConstant: 24),
                logoView.heightAnchor.constraint(equalToConstant: 24)
            ])

            contentStack.addArrangedSubview(logoView)
            loadImage(from: logoUrl, into: logoView)
        }

        // Row title
        let titleLabel = UILabel()
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.text = row.title
        let messageFontSize = styleOptions?.messageFontSize ?? 16
        titleLabel.font = .systemFont(ofSize: messageFontSize)
        titleLabel.textColor = styleOptions?.messageColor ?? .label
        titleLabel.numberOfLines = 1
        titleLabel.lineBreakMode = .byTruncatingTail
        titleLabel.setContentHuggingPriority(.defaultLow, for: .horizontal)
        titleLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
        contentStack.addArrangedSubview(titleLabel)

        // Row value (optional, aligned right)
        if let value = row.value, !value.isEmpty {
            let valueLabel = UILabel()
            valueLabel.translatesAutoresizingMaskIntoConstraints = false
            valueLabel.text = value
            valueLabel.font = .systemFont(ofSize: messageFontSize)
            valueLabel.textColor = .secondaryLabel
            valueLabel.textAlignment = .right
            valueLabel.numberOfLines = 2
            valueLabel.lineBreakMode = .byTruncatingTail
            valueLabel.setContentHuggingPriority(.required, for: .horizontal)
            valueLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
            contentStack.addArrangedSubview(valueLabel)
        }

        // Separator (except for last row)
        if !isLast {
            let separator = UIView()
            separator.translatesAutoresizingMaskIntoConstraints = false
            separator.backgroundColor = .separator
            rowView.addSubview(separator)

            NSLayoutConstraint.activate([
                separator.leadingAnchor.constraint(equalTo: rowView.leadingAnchor, constant: 16),
                separator.trailingAnchor.constraint(equalTo: rowView.trailingAnchor, constant: -16),
                separator.bottomAnchor.constraint(equalTo: rowView.bottomAnchor),
                separator.heightAnchor.constraint(equalToConstant: 1 / UIScreen.main.scale)
            ])
        }

        return rowView
    }

    private func loadImage(from urlString: String, into imageView: UIImageView) {
        // Check if it's a base64 data URL
        if urlString.hasPrefix("data:") {
            if let commaIndex = urlString.firstIndex(of: ",") {
                let base64String = String(urlString[urlString.index(after: commaIndex)...])
                if let imageData = Data(base64Encoded: base64String),
                   let image = UIImage(data: imageData) {
                    imageView.image = image
                }
            }
        } else if let url = URL(string: urlString) {
            // Load from HTTP/HTTPS URL asynchronously
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        imageView.image = image
                    }
                }
            }.resume()
        }
    }

    private func applyLiquidGlassStyle() {
        // Liquid Glass effects are applied at button creation time (iOS 26+)
        // using UIButton.Configuration.glass() and .prominentGlass().
        // No additional runtime adjustments needed.
    }

    private func configureCancelButtonForSheet() {
        let cancelColor = styleOptions?.cancelButtonColor ?? .secondaryLabel
        let fontSize = styleOptions?.buttonFontSize ?? 17

        if #available(iOS 26, *) {
            var config = UIButton.Configuration.plain()
            config.title = cancelButtonTitle ?? "Cancel"
            config.baseForegroundColor = cancelColor
            config.titleTextAttributesTransformer = UIConfigurationTextAttributesTransformer { incoming in
                var outgoing = incoming
                outgoing.font = .systemFont(ofSize: fontSize, weight: .medium)
                return outgoing
            }
            cancelButton.configuration = config
        } else {
            cancelButton.setTitleColor(cancelColor, for: .normal)
            cancelButton.layer.borderWidth = 0
        }
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
        case .sheet:
            sheetCallback?(true)
        case .messageSheet:
            sheetCallback?(true)
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
        case .sheet:
            sheetCallback?(false)
        case .messageSheet:
            sheetCallback?(false)
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
