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
}

export interface BaseDialogOptions extends DialogStyleOptions {
  title?: string;
  message: string;
  mode?: DialogMode; // default: 'basic'
}

export interface AlertOptions extends BaseDialogOptions {
  buttonTitle?: string; // default: "OK"
}

export interface ConfirmOptions extends BaseDialogOptions {
  okButtonTitle?: string; // default: "OK"
  cancelButtonTitle?: string; // default: "Cancel"
}

export interface PromptOptions extends BaseDialogOptions {
  okButtonTitle?: string;
  cancelButtonTitle?: string;
  inputPlaceholder?: string;
  inputText?: string; // pre-filled value
}

export interface SelectOption {
  label: string;
  value: string;
}

export interface SingleSelectOptions extends BaseDialogOptions {
  options: SelectOption[];
  selectedValue?: string;
  okButtonTitle?: string;
  cancelButtonTitle?: string;
}

export interface MultiSelectOptions extends BaseDialogOptions {
  options: SelectOption[];
  selectedValues?: string[];
  okButtonTitle?: string;
  cancelButtonTitle?: string;
}

// Results
export interface ConfirmResult {
  value: boolean;
}

export interface PromptResult {
  value: string;
  cancelled: boolean;
}

export interface SingleSelectResult {
  value: string | null;
  cancelled: boolean;
}

export interface MultiSelectResult {
  values: string[];
  cancelled: boolean;
}

export interface ExtendedDialogPlugin {
  alert(options: AlertOptions): Promise<void>;
  confirm(options: ConfirmOptions): Promise<ConfirmResult>;
  prompt(options: PromptOptions): Promise<PromptResult>;
  singleSelect(options: SingleSelectOptions): Promise<SingleSelectResult>;
  multiSelect(options: MultiSelectOptions): Promise<MultiSelectResult>;
}
