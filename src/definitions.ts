/**
 * Dialog presentation mode.
 *
 * - `'basic'` — Standard modal dialog (default)
 * - `'fullscreen'` — Full-screen dialog presentation
 */
export type DialogMode = 'basic' | 'fullscreen';

export interface DialogStyleOptions {
  /**
   * Color for the OK/primary button (hex format, e.g., "#FF5722")
   */
  buttonColor?: string;
  /**
   * Color for the cancel button (hex format, e.g., "#757575")
   */
  cancelButtonColor?: string;
  /**
   * Color for the title text (hex format, e.g., "#000000")
   */
  titleColor?: string;
  /**
   * Color for the message text (hex format, e.g., "#666666")
   */
  messageColor?: string;
  /**
   * Background color of the dialog (hex format, e.g., "#FFFFFF")
   */
  backgroundColor?: string;
  /**
   * Font size for the title text in sp/pt (e.g., 20)
   */
  titleFontSize?: number;
  /**
   * Font size for the message text in sp/pt (e.g., 16)
   */
  messageFontSize?: number;
  /**
   * Font size for the button text in sp/pt (e.g., 14)
   */
  buttonFontSize?: number;
  /**
   * Vertical spacing between dialog content and the confirm/cancel buttons in dp/pt/px (e.g., 16).
   * Only applies to fullscreen and sheet modes.
   *
   * @example
   * // Fullscreen confirm with extra spacing between content and buttons
   * await ExtendedDialog.confirm({
   *   title: 'Delete Account',
   *   message: 'This action cannot be undone.',
   *   mode: 'fullscreen',
   *   contentButtonSpacing: 32,
   * });
   *
   * @example
   * // Sheet with tight spacing
   * await ExtendedDialog.sheet({
   *   title: 'Order Summary',
   *   rows: [{ title: 'Total', value: '$9.99' }],
   *   contentButtonSpacing: 8,
   * });
   */
  contentButtonSpacing?: number;
}

export interface BaseDialogOptions extends DialogStyleOptions {
  /**
   * Title displayed at the top of the dialog.
   */
  title?: string;
  /**
   * Message body displayed in the dialog.
   */
  message: string;
  /**
   * Presentation mode for the dialog.
   *
   * @default 'basic'
   */
  mode?: DialogMode;
}

export interface AlertOptions extends BaseDialogOptions {
  /**
   * Title for the dismiss button.
   *
   * @default "OK"
   */
  buttonTitle?: string;
}

export interface ConfirmOptions extends BaseDialogOptions {
  /**
   * Title for the confirmation button.
   *
   * @default "OK"
   */
  okButtonTitle?: string;
  /**
   * Title for the cancel button.
   *
   * @default "Cancel"
   */
  cancelButtonTitle?: string;
}

export interface PromptOptions extends BaseDialogOptions {
  /**
   * Title for the confirmation button.
   *
   * @default "OK"
   */
  okButtonTitle?: string;
  /**
   * Title for the cancel button.
   *
   * @default "Cancel"
   */
  cancelButtonTitle?: string;
  /**
   * Placeholder text shown in the input field when empty.
   */
  inputPlaceholder?: string;
  /**
   * Pre-filled value for the input field.
   */
  inputText?: string;
  /**
   * Whether to automatically focus the input field and open the keyboard when the dialog appears.
   *
   * Note: On iOS, basic (non-fullscreen) dialogs always auto-focus the input field due to
   * UIAlertController's built-in behavior. This option only takes effect on iOS when using
   * fullscreen mode (`mode: 'fullscreen'`). On Android and web, this works for all modes.
   *
   * @default false
   */
  focusInput?: boolean;
}

export interface SelectOption {
  /**
   * Display text for the option.
   */
  label: string;
  /**
   * Value returned when the option is selected.
   */
  value: string;
}

export interface SingleSelectOptions extends BaseDialogOptions {
  /**
   * List of options to display for selection.
   */
  options: SelectOption[];
  /**
   * Value of the initially selected option.
   */
  selectedValue?: string;
  /**
   * Title for the confirmation button.
   *
   * @default "OK"
   */
  okButtonTitle?: string;
  /**
   * Title for the cancel button.
   *
   * @default "Cancel"
   */
  cancelButtonTitle?: string;
}

export interface MultiSelectOptions extends BaseDialogOptions {
  /**
   * List of options to display for selection.
   */
  options: SelectOption[];
  /**
   * Values of the initially selected options.
   */
  selectedValues?: string[];
  /**
   * Title for the confirmation button.
   *
   * @default "OK"
   */
  okButtonTitle?: string;
  /**
   * Title for the cancel button.
   *
   * @default "Cancel"
   */
  cancelButtonTitle?: string;
}

export interface ConfirmResult {
  /**
   * Whether the user confirmed (`true`) or cancelled (`false`).
   */
  value: boolean;
}

export interface PromptResult {
  /**
   * The text entered by the user. Empty string if cancelled.
   */
  value: string;
  /**
   * Whether the user cancelled the dialog.
   */
  cancelled: boolean;
}

export interface SingleSelectResult {
  /**
   * The value of the selected option, or `null` if cancelled.
   */
  value: string | null;
  /**
   * Whether the user cancelled the dialog.
   */
  cancelled: boolean;
}

export interface MultiSelectResult {
  /**
   * The values of the selected options. Empty array if cancelled.
   */
  values: string[];
  /**
   * Whether the user cancelled the dialog.
   */
  cancelled: boolean;
}

export interface SheetRow {
  /** Row title (required) */
  title: string;
  /** Optional logo/icon - supports base64 data URL or HTTP/HTTPS URL */
  logo?: string;
  /** Optional value displayed on the right */
  value?: string;
}

export interface SheetOptions extends DialogStyleOptions {
  /** Header logo - supports base64 data URL or HTTP/HTTPS URL */
  headerLogo?: string;
  /** Sheet title */
  title: string;
  /** Description rows */
  rows: SheetRow[];
  /** Confirm button title */
  confirmButtonTitle?: string; // default: "Confirm"
  /** Cancel button title */
  cancelButtonTitle?: string; // default: "Cancel"
  /** Dialog mode */
  mode?: DialogMode;
}

export interface SheetResult {
  /** True if confirmed, false if cancelled */
  confirmed: boolean;
}

export interface ExtendedDialogPlugin {
  /**
   * Show an alert dialog with a single dismiss button.
   *
   * @param options - Alert configuration options.
   */
  alert(options: AlertOptions): Promise<void>;
  /**
   * Show a confirmation dialog with OK and Cancel buttons.
   *
   * @param options - Confirm configuration options.
   * @returns The user's confirmation choice.
   */
  confirm(options: ConfirmOptions): Promise<ConfirmResult>;
  /**
   * Show a prompt dialog with a text input field.
   *
   * @param options - Prompt configuration options.
   * @returns The entered text and whether the dialog was cancelled.
   */
  prompt(options: PromptOptions): Promise<PromptResult>;
  /**
   * Show a single-select dialog where the user picks one option.
   *
   * @param options - Single select configuration options.
   * @returns The selected value and whether the dialog was cancelled.
   */
  singleSelect(options: SingleSelectOptions): Promise<SingleSelectResult>;
  /**
   * Show a multi-select dialog where the user picks one or more options.
   *
   * @param options - Multi select configuration options.
   * @returns The selected values and whether the dialog was cancelled.
   */
  multiSelect(options: MultiSelectOptions): Promise<MultiSelectResult>;
  /**
   * Show a sheet dialog with a header, title, and structured data rows.
   *
   * @param options - Sheet configuration options.
   * @returns Whether the user confirmed or cancelled the sheet.
   */
  sheet(options: SheetOptions): Promise<SheetResult>;
}
