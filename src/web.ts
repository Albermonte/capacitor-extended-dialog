import { WebPlugin } from '@capacitor/core';

import type {
  ExtendedDialogPlugin,
  AlertOptions,
  ConfirmOptions,
  ConfirmResult,
  PromptOptions,
  PromptResult,
  SingleSelectOptions,
  SingleSelectResult,
  MultiSelectOptions,
  MultiSelectResult,
  DialogStyleOptions,
} from './definitions';

export class ExtendedDialogWeb extends WebPlugin implements ExtendedDialogPlugin {
  async alert(options: AlertOptions): Promise<void> {
    const message = options.title ? `${options.title}\n\n${options.message}` : options.message;
    window.alert(message);
  }

  async confirm(options: ConfirmOptions): Promise<ConfirmResult> {
    const message = options.title ? `${options.title}\n\n${options.message}` : options.message;
    const result = window.confirm(message);
    return { value: result };
  }

  async prompt(options: PromptOptions): Promise<PromptResult> {
    const message = options.title ? `${options.title}\n\n${options.message}` : options.message;
    const result = window.prompt(message, options.inputText ?? '');
    return {
      value: result ?? '',
      cancelled: result === null,
    };
  }

  async singleSelect(options: SingleSelectOptions): Promise<SingleSelectResult> {
    return new Promise((resolve) => {
      const overlay = this.createOverlay();
      const dialog = this.createDialogContainer(options.title, options.message, options);

      const optionsContainer = document.createElement('div');
      optionsContainer.style.cssText = 'max-height: 300px; overflow-y: auto; margin: 16px 0;';

      let selectedValue = options.selectedValue ?? null;

      options.options.forEach((option) => {
        const label = document.createElement('label');
        label.style.cssText =
          'display: flex; align-items: center; padding: 12px 0; cursor: pointer; border-bottom: 1px solid #e0e0e0;';

        const radio = document.createElement('input');
        radio.type = 'radio';
        radio.name = 'singleSelect';
        radio.value = option.value;
        radio.checked = option.value === selectedValue;
        radio.style.cssText = 'margin-right: 12px; width: 20px; height: 20px;';
        radio.addEventListener('change', () => {
          selectedValue = option.value;
        });

        const text = document.createElement('span');
        text.textContent = option.label;
        text.style.cssText = 'font-size: 16px;';

        label.appendChild(radio);
        label.appendChild(text);
        optionsContainer.appendChild(label);
      });

      dialog.appendChild(optionsContainer);

      const buttonContainer = this.createButtonContainer();

      const cancelButton = this.createButton(
        options.cancelButtonTitle ?? 'Cancel',
        false,
        () => {
          this.removeOverlay(overlay);
          resolve({ value: null, cancelled: true });
        },
        options,
      );

      const okButton = this.createButton(
        options.okButtonTitle ?? 'OK',
        true,
        () => {
          this.removeOverlay(overlay);
          resolve({ value: selectedValue, cancelled: false });
        },
        options,
      );

      buttonContainer.appendChild(cancelButton);
      buttonContainer.appendChild(okButton);
      dialog.appendChild(buttonContainer);
      overlay.appendChild(dialog);
      document.body.appendChild(overlay);
    });
  }

  async multiSelect(options: MultiSelectOptions): Promise<MultiSelectResult> {
    return new Promise((resolve) => {
      const overlay = this.createOverlay();
      const dialog = this.createDialogContainer(options.title, options.message, options);

      const optionsContainer = document.createElement('div');
      optionsContainer.style.cssText = 'max-height: 300px; overflow-y: auto; margin: 16px 0;';

      const selectedValues = new Set(options.selectedValues ?? []);

      options.options.forEach((option) => {
        const label = document.createElement('label');
        label.style.cssText =
          'display: flex; align-items: center; padding: 12px 0; cursor: pointer; border-bottom: 1px solid #e0e0e0;';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = option.value;
        checkbox.checked = selectedValues.has(option.value);
        checkbox.style.cssText = 'margin-right: 12px; width: 20px; height: 20px;';
        checkbox.addEventListener('change', () => {
          if (checkbox.checked) {
            selectedValues.add(option.value);
          } else {
            selectedValues.delete(option.value);
          }
        });

        const text = document.createElement('span');
        text.textContent = option.label;
        text.style.cssText = 'font-size: 16px;';

        label.appendChild(checkbox);
        label.appendChild(text);
        optionsContainer.appendChild(label);
      });

      dialog.appendChild(optionsContainer);

      const buttonContainer = this.createButtonContainer();

      const cancelButton = this.createButton(
        options.cancelButtonTitle ?? 'Cancel',
        false,
        () => {
          this.removeOverlay(overlay);
          resolve({ values: [], cancelled: true });
        },
        options,
      );

      const okButton = this.createButton(
        options.okButtonTitle ?? 'OK',
        true,
        () => {
          this.removeOverlay(overlay);
          resolve({ values: Array.from(selectedValues), cancelled: false });
        },
        options,
      );

      buttonContainer.appendChild(cancelButton);
      buttonContainer.appendChild(okButton);
      dialog.appendChild(buttonContainer);
      overlay.appendChild(dialog);
      document.body.appendChild(overlay);
    });
  }

  private createOverlay(): HTMLDivElement {
    const overlay = document.createElement('div');
    overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 999999;
    `;
    return overlay;
  }

  private createDialogContainer(title?: string, message?: string, styleOptions?: DialogStyleOptions): HTMLDivElement {
    const dialog = document.createElement('div');
    const bgColor = styleOptions?.backgroundColor ?? 'white';
    dialog.style.cssText = `
      background: ${bgColor};
      border-radius: 16px;
      padding: 24px;
      min-width: 300px;
      max-width: 400px;
      max-height: 80vh;
      overflow-y: auto;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
    `;

    if (title) {
      const titleEl = document.createElement('h2');
      titleEl.textContent = title;
      const titleFontSize = styleOptions?.titleFontSize ?? 20;
      const titleColor = styleOptions?.titleColor ?? 'inherit';
      titleEl.style.cssText = `margin: 0 0 8px 0; font-size: ${titleFontSize}px; font-weight: 600; color: ${titleColor};`;
      dialog.appendChild(titleEl);
    }

    if (message) {
      const messageEl = document.createElement('p');
      messageEl.textContent = message;
      const messageFontSize = styleOptions?.messageFontSize ?? 14;
      const messageColor = styleOptions?.messageColor ?? '#666';
      messageEl.style.cssText = `margin: 0; font-size: ${messageFontSize}px; color: ${messageColor};`;
      dialog.appendChild(messageEl);
    }

    return dialog;
  }

  private createButtonContainer(): HTMLDivElement {
    const container = document.createElement('div');
    container.style.cssText = 'display: flex; justify-content: flex-end; gap: 8px; margin-top: 24px;';
    return container;
  }

  private createButton(
    text: string,
    primary: boolean,
    onClick: () => void,
    styleOptions?: DialogStyleOptions,
  ): HTMLButtonElement {
    const button = document.createElement('button');
    button.textContent = text;
    const fontSize = styleOptions?.buttonFontSize ?? 14;
    const primaryColor = styleOptions?.buttonColor ?? '#6750A4';
    const cancelColor = styleOptions?.cancelButtonColor ?? '#6750A4';
    button.style.cssText = `
      padding: 10px 24px;
      border-radius: 8px;
      font-size: ${fontSize}px;
      font-weight: 500;
      cursor: pointer;
      border: none;
      ${primary ? `background: ${primaryColor}; color: white;` : `background: transparent; color: ${cancelColor};`}
    `;
    button.addEventListener('click', onClick);
    return button;
  }

  private removeOverlay(overlay: HTMLDivElement): void {
    overlay.remove();
  }
}
