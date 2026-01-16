package com.albermonte.extendeddialog;

import android.graphics.Color;

/**
 * Style options for dialog customization.
 */
public class DialogStyleOptions {
    private Integer buttonColor;
    private Integer cancelButtonColor;
    private Float messageFontSize;
    private Float buttonFontSize;

    public DialogStyleOptions() {
    }

    public Integer getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            try {
                this.buttonColor = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                // Invalid color, ignore
            }
        }
    }

    public Integer getCancelButtonColor() {
        return cancelButtonColor;
    }

    public void setCancelButtonColor(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            try {
                this.cancelButtonColor = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                // Invalid color, ignore
            }
        }
    }

    public Float getMessageFontSize() {
        return messageFontSize;
    }

    public void setMessageFontSize(Double size) {
        if (size != null && size > 0) {
            this.messageFontSize = size.floatValue();
        }
    }

    public Float getButtonFontSize() {
        return buttonFontSize;
    }

    public void setButtonFontSize(Double size) {
        if (size != null && size > 0) {
            this.buttonFontSize = size.floatValue();
        }
    }

    public boolean hasStyles() {
        return buttonColor != null || cancelButtonColor != null ||
               messageFontSize != null || buttonFontSize != null;
    }
}
