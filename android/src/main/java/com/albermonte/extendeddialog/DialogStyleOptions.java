package com.albermonte.extendeddialog;

import android.graphics.Color;
import android.os.Bundle;

/**
 * Style options for dialog customization.
 */
public class DialogStyleOptions {

    private static final String KEY_BUTTON_COLOR = "buttonColor";
    private static final String KEY_CANCEL_BUTTON_COLOR = "cancelButtonColor";
    private static final String KEY_TITLE_COLOR = "titleColor";
    private static final String KEY_MESSAGE_COLOR = "messageColor";
    private static final String KEY_BACKGROUND_COLOR = "backgroundColor";
    private static final String KEY_TITLE_FONT_SIZE = "titleFontSize";
    private static final String KEY_MESSAGE_FONT_SIZE = "messageFontSize";
    private static final String KEY_BUTTON_FONT_SIZE = "buttonFontSize";
    private static final String KEY_CONTENT_BUTTON_SPACING = "contentButtonSpacing";
    private static final String KEY_HEADER_LOGO_CORNER_RADIUS = "headerLogoCornerRadius";

    private Integer buttonColor;
    private Integer cancelButtonColor;
    private Integer titleColor;
    private Integer messageColor;
    private Integer backgroundColor;
    private Float titleFontSize;
    private Float messageFontSize;
    private Float buttonFontSize;
    private Float contentButtonSpacing;
    private Float headerLogoCornerRadius;

    public DialogStyleOptions() {}

    private static Integer parseColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return null;
        }
        try {
            return Color.parseColor(hexColor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Float parseSize(Double size) {
        return (size != null && size > 0) ? size.floatValue() : null;
    }

    private static Float parseSpacing(Double size) {
        return (size != null && size >= 0) ? size.floatValue() : null;
    }

    public Integer getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(String hexColor) {
        this.buttonColor = parseColor(hexColor);
    }

    public Integer getCancelButtonColor() {
        return cancelButtonColor;
    }

    public void setCancelButtonColor(String hexColor) {
        this.cancelButtonColor = parseColor(hexColor);
    }

    public Integer getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String hexColor) {
        this.titleColor = parseColor(hexColor);
    }

    public Integer getMessageColor() {
        return messageColor;
    }

    public void setMessageColor(String hexColor) {
        this.messageColor = parseColor(hexColor);
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String hexColor) {
        this.backgroundColor = parseColor(hexColor);
    }

    public Float getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(Double size) {
        this.titleFontSize = parseSize(size);
    }

    public Float getMessageFontSize() {
        return messageFontSize;
    }

    public void setMessageFontSize(Double size) {
        this.messageFontSize = parseSize(size);
    }

    public Float getButtonFontSize() {
        return buttonFontSize;
    }

    public void setButtonFontSize(Double size) {
        this.buttonFontSize = parseSize(size);
    }

    public Float getContentButtonSpacing() {
        return contentButtonSpacing;
    }

    public void setContentButtonSpacing(Double size) {
        this.contentButtonSpacing = parseSpacing(size);
    }

    public Float getHeaderLogoCornerRadius() {
        return headerLogoCornerRadius;
    }

    public void setHeaderLogoCornerRadius(Double radius) {
        // Allow any value: positive for custom radius, 0 for square, -1 for circle
        this.headerLogoCornerRadius = radius != null ? radius.floatValue() : null;
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
            buttonFontSize != null ||
            contentButtonSpacing != null ||
            headerLogoCornerRadius != null
        );
    }

    public void writeToBundle(Bundle bundle) {
        if (buttonColor != null) bundle.putInt(KEY_BUTTON_COLOR, buttonColor);
        if (cancelButtonColor != null) bundle.putInt(KEY_CANCEL_BUTTON_COLOR, cancelButtonColor);
        if (titleColor != null) bundle.putInt(KEY_TITLE_COLOR, titleColor);
        if (messageColor != null) bundle.putInt(KEY_MESSAGE_COLOR, messageColor);
        if (backgroundColor != null) bundle.putInt(KEY_BACKGROUND_COLOR, backgroundColor);
        if (titleFontSize != null) bundle.putFloat(KEY_TITLE_FONT_SIZE, titleFontSize);
        if (messageFontSize != null) bundle.putFloat(KEY_MESSAGE_FONT_SIZE, messageFontSize);
        if (buttonFontSize != null) bundle.putFloat(KEY_BUTTON_FONT_SIZE, buttonFontSize);
        if (contentButtonSpacing != null) bundle.putFloat(KEY_CONTENT_BUTTON_SPACING, contentButtonSpacing);
        if (headerLogoCornerRadius != null) bundle.putFloat(KEY_HEADER_LOGO_CORNER_RADIUS, headerLogoCornerRadius);
    }

    public static DialogStyleOptions readFromBundle(Bundle bundle) {
        DialogStyleOptions options = new DialogStyleOptions();
        options.buttonColor = bundle.containsKey(KEY_BUTTON_COLOR) ? bundle.getInt(KEY_BUTTON_COLOR) : null;
        options.cancelButtonColor = bundle.containsKey(KEY_CANCEL_BUTTON_COLOR) ? bundle.getInt(KEY_CANCEL_BUTTON_COLOR) : null;
        options.titleColor = bundle.containsKey(KEY_TITLE_COLOR) ? bundle.getInt(KEY_TITLE_COLOR) : null;
        options.messageColor = bundle.containsKey(KEY_MESSAGE_COLOR) ? bundle.getInt(KEY_MESSAGE_COLOR) : null;
        options.backgroundColor = bundle.containsKey(KEY_BACKGROUND_COLOR) ? bundle.getInt(KEY_BACKGROUND_COLOR) : null;
        options.titleFontSize = bundle.containsKey(KEY_TITLE_FONT_SIZE) ? bundle.getFloat(KEY_TITLE_FONT_SIZE) : null;
        options.messageFontSize = bundle.containsKey(KEY_MESSAGE_FONT_SIZE) ? bundle.getFloat(KEY_MESSAGE_FONT_SIZE) : null;
        options.buttonFontSize = bundle.containsKey(KEY_BUTTON_FONT_SIZE) ? bundle.getFloat(KEY_BUTTON_FONT_SIZE) : null;
        options.contentButtonSpacing = bundle.containsKey(KEY_CONTENT_BUTTON_SPACING) ? bundle.getFloat(KEY_CONTENT_BUTTON_SPACING) : null;
        options.headerLogoCornerRadius = bundle.containsKey(KEY_HEADER_LOGO_CORNER_RADIUS) ? bundle.getFloat(KEY_HEADER_LOGO_CORNER_RADIUS) : null;
        return options;
    }
}
