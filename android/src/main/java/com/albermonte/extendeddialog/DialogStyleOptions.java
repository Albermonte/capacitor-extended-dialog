package com.albermonte.extendeddialog;

import android.graphics.Color;

/**
 * Style options for dialog customization.
 */
public class DialogStyleOptions {

    private Integer buttonColor;
    private Integer cancelButtonColor;
    private Integer titleColor;
    private Integer messageColor;
    private Integer backgroundColor;
    private Float titleFontSize;
    private Float messageFontSize;
    private Float buttonFontSize;

    public DialogStyleOptions() {}

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

    public Integer getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            try {
                this.titleColor = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                // Invalid color, ignore
            }
        }
    }

    public Integer getMessageColor() {
        return messageColor;
    }

    public void setMessageColor(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            try {
                this.messageColor = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                // Invalid color, ignore
            }
        }
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            try {
                this.backgroundColor = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                // Invalid color, ignore
            }
        }
    }

    public Float getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(Double size) {
        if (size != null && size > 0) {
            this.titleFontSize = size.floatValue();
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
        return (
            buttonColor != null ||
            cancelButtonColor != null ||
            titleColor != null ||
            messageColor != null ||
            backgroundColor != null ||
            titleFontSize != null ||
            messageFontSize != null ||
            buttonFontSize != null
        );
    }
}
