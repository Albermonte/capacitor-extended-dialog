package com.albermonte.extendeddialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FullScreenDialogFragment extends DialogFragment {

    public enum DialogType {
        ALERT,
        CONFIRM,
        PROMPT,
        SINGLE_SELECT,
        MULTI_SELECT
    }

    private static final String ARG_TYPE = "type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_OK_BUTTON = "okButton";
    private static final String ARG_CANCEL_BUTTON = "cancelButton";
    private static final String ARG_INPUT_PLACEHOLDER = "inputPlaceholder";
    private static final String ARG_INPUT_TEXT = "inputText";
    private static final String ARG_OPTIONS = "options";
    private static final String ARG_FOCUS_INPUT = "focusInput";

    private ExtendedDialog.AlertCallback alertCallback;
    private ExtendedDialog.ConfirmCallback confirmCallback;
    private ExtendedDialog.PromptCallback promptCallback;
    private ExtendedDialog.SingleSelectCallback singleSelectCallback;
    private ExtendedDialog.MultiSelectCallback multiSelectCallback;

    private TextInputEditText inputField;
    private MaterialButton okBtn;
    private String selectedValue;
    private Set<String> selectedValues = new HashSet<>();
    private boolean dismissed = false;
    private Context themedContext;

    /**
     * Returns a context wrapped with Material3 theme.
     * This ensures Material components work regardless of the app's base theme.
     */
    private Context getThemedContext() {
        if (themedContext == null) {
            themedContext = new ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Theme_Material3_DayNight_Dialog);
        }
        return themedContext;
    }

    public static FullScreenDialogFragment newInstance(
        DialogType type,
        String title,
        String message,
        String okButton,
        String cancelButton,
        String inputPlaceholder,
        String inputText,
        String optionsJson,
        boolean focusInput,
        DialogStyleOptions styleOptions
    ) {
        FullScreenDialogFragment fragment = new FullScreenDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type.name());
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_OK_BUTTON, okButton);
        args.putString(ARG_CANCEL_BUTTON, cancelButton);
        args.putString(ARG_INPUT_PLACEHOLDER, inputPlaceholder);
        args.putString(ARG_INPUT_TEXT, inputText);
        args.putString(ARG_OPTIONS, optionsJson);
        args.putBoolean(ARG_FOCUS_INPUT, focusInput);
        if (styleOptions != null) {
            styleOptions.writeToBundle(args);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setAlertCallback(ExtendedDialog.AlertCallback callback) {
        this.alertCallback = callback;
    }

    public void setConfirmCallback(ExtendedDialog.ConfirmCallback callback) {
        this.confirmCallback = callback;
    }

    public void setPromptCallback(ExtendedDialog.PromptCallback callback) {
        this.promptCallback = callback;
    }

    public void setSingleSelectCallback(ExtendedDialog.SingleSelectCallback callback) {
        this.singleSelectCallback = callback;
    }

    public void setMultiSelectCallback(ExtendedDialog.MultiSelectCallback callback) {
        this.multiSelectCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_ExtendedDialog_FullScreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            // Set window to fill entire screen
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setWindowAnimations(android.R.style.Animation_Dialog);
            // Enable edge-to-edge display
            window.setDecorFitsSystemWindows(false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            // Remove any background dim that might cause gaps
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return null;
        }

        DialogType type = DialogType.valueOf(args.getString(ARG_TYPE, DialogType.ALERT.name()));
        String title = args.getString(ARG_TITLE, "");
        String message = args.getString(ARG_MESSAGE, "");
        String okButton = args.getString(ARG_OK_BUTTON, "OK");
        String cancelButton = args.getString(ARG_CANCEL_BUTTON, "Cancel");
        String inputPlaceholder = args.getString(ARG_INPUT_PLACEHOLDER);
        String inputText = args.getString(ARG_INPUT_TEXT);
        String optionsJson = args.getString(ARG_OPTIONS);
        boolean focusInput = args.getBoolean(ARG_FOCUS_INPUT, false);
        DialogStyleOptions styleOptions = DialogStyleOptions.readFromBundle(args);

        // Create root layout
        Context ctx = getThemedContext();
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        if (styleOptions.getBackgroundColor() != null) {
            root.setBackgroundColor(styleOptions.getBackgroundColor());
        } else {
            // Use Material 3 surface color or fallback to white
            // Use android.R.attr.colorBackground which maps to surface in M3 themes
            int surfaceColor = MaterialColors.getColor(ctx, android.R.attr.colorBackground, 0xFFFFFFFF);
            root.setBackgroundColor(surfaceColor);
        }

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Create toolbar
        MaterialToolbar toolbar = new MaterialToolbar(ctx);
        toolbar.setTitle(title != null && !title.isEmpty() ? title : "");
        if (styleOptions.getTitleColor() != null) {
            toolbar.setTitleTextColor(styleOptions.getTitleColor());
        }
        if (styleOptions.getTitleFontSize() != null) {
            toolbar.setTitleTextAppearance(ctx, com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        }
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_m3_chip_close);

        // Tint navigation icon for proper visibility in dark/light themes
        // Use android.R.attr.textColorPrimary which maps to onSurface in M3 themes
        int colorOnSurface = MaterialColors.getColor(ctx, android.R.attr.textColorPrimary, 0xFF1D1B20);
        toolbar.setNavigationIconTint(colorOnSurface);

        toolbar.setNavigationOnClickListener((v) -> {
            handleCancel();
            dismiss();
        });

        root.addView(toolbar);

        float density = getResources().getDisplayMetrics().density;

        // Create scroll view for content
        ScrollView scrollView = new ScrollView(ctx);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout contentLayout = new LinearLayout(ctx);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        int horizontalPadding = (int) (32 * density);
        int verticalPadding = (int) (24 * density);
        contentLayout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        // Add message
        if (message != null && !message.isEmpty()) {
            TextView messageView = new TextView(ctx);
            messageView.setText(message);
            if (styleOptions.getMessageFontSize() != null) {
                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
            } else {
                // M3 dialog supporting text: 14sp, weight 400, tracking 0.25sp
                TextViewCompat.setTextAppearance(messageView,
                    com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            }
            if (styleOptions.getMessageColor() != null) {
                messageView.setTextColor(styleOptions.getMessageColor());
            } else {
                // M3 dialog supporting text color: onSurfaceVariant (#49454F)
                // Use android.R.attr.textColorSecondary which maps to onSurfaceVariant in M3 themes
                int onSurfaceVariantColor = MaterialColors.getColor(ctx, android.R.attr.textColorSecondary, 0xFF49454F);
                messageView.setTextColor(onSurfaceVariantColor);
            }
            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            messageParams.bottomMargin = (int) (32 * density);
            messageView.setLayoutParams(messageParams);
            contentLayout.addView(messageView);
        }

        // Add type-specific content
        switch (type) {
            case PROMPT:
                addPromptContent(contentLayout, inputPlaceholder, inputText, focusInput);
                break;
            case SINGLE_SELECT:
                addSingleSelectContent(contentLayout, optionsJson, inputText);
                break;
            case MULTI_SELECT:
                addMultiSelectContent(contentLayout, optionsJson, inputText);
                break;
        }

        scrollView.addView(contentLayout);
        root.addView(scrollView);

        // Button container at the bottom
        int primaryColorValue = MaterialColors.getColor(ctx, android.R.attr.colorPrimary, 0xFF6750A4);

        LinearLayout buttonContainer = new LinearLayout(ctx);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.END);
        int buttonPadding = (int) (16 * density);
        buttonContainer.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);

        // Cancel button (not shown for ALERT type)
        if (type != DialogType.ALERT) {
            MaterialButton cancelBtn = new MaterialButton(ctx, null,
                com.google.android.material.R.attr.borderlessButtonStyle);
            cancelBtn.setText(cancelButton);
            cancelBtn.setOnClickListener((v) -> {
                handleCancel();
                dismiss();
            });
            if (styleOptions.getCancelButtonColor() != null) {
                cancelBtn.setTextColor(ColorStateList.valueOf(styleOptions.getCancelButtonColor()));
            } else {
                cancelBtn.setTextColor(ColorStateList.valueOf(primaryColorValue));
            }
            if (styleOptions.getButtonFontSize() != null) {
                cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
            }
            buttonContainer.addView(cancelBtn);
        }

        // OK button
        okBtn = new MaterialButton(ctx, null,
            com.google.android.material.R.attr.borderlessButtonStyle);
        okBtn.setText(okButton);
        okBtn.setOnClickListener((v) -> {
            handleConfirm(type);
            dismiss();
        });
        if (styleOptions.getButtonColor() != null) {
            okBtn.setTextColor(ColorStateList.valueOf(styleOptions.getButtonColor()));
        } else {
            okBtn.setTextColor(ColorStateList.valueOf(primaryColorValue));
        }
        if (styleOptions.getButtonFontSize() != null) {
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
        }

        // Disable confirming action until a choice is made for selection dialogs
        if (type == DialogType.SINGLE_SELECT && selectedValue == null) {
            okBtn.setEnabled(false);
        } else if (type == DialogType.MULTI_SELECT && selectedValues.isEmpty()) {
            okBtn.setEnabled(false);
        }

        buttonContainer.addView(okBtn);
        root.addView(buttonContainer);

        return root;
    }

    private void addPromptContent(LinearLayout container, String placeholder, String text, boolean focusInput) {
        Context ctx = getThemedContext();

        // Use Material 3 TextInputLayout with outlined style
        TextInputLayout textInputLayout = new TextInputLayout(ctx, null,
            com.google.android.material.R.attr.textInputOutlinedStyle);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        inputField = new TextInputEditText(textInputLayout.getContext());
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        if (placeholder != null) {
            textInputLayout.setHint(placeholder);
        }
        if (text != null) {
            inputField.setText(text);
            inputField.setSelection(text.length());
        }
        textInputLayout.addView(inputField);
        container.addView(textInputLayout);

        if (focusInput) {
            inputField.requestFocus();
            // Show keyboard when dialog is shown
            inputField.postDelayed(() -> {
                if (getContext() != null) {
                    android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(inputField, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 200);
        }
    }

    private void addSingleSelectContent(LinearLayout container, String optionsJson, String selectedValueArg) {
        if (optionsJson == null) return;

        selectedValue = selectedValueArg;
        Context ctx = getThemedContext();
        float density = getResources().getDisplayMetrics().density;
        int itemPadding = (int) (20 * density);

        try {
            JSONArray options = new JSONArray(optionsJson);
            RadioGroup radioGroup = new RadioGroup(ctx);
            radioGroup.setPadding(0, itemPadding / 2, 0, 0);

            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String label = option.getString("label");
                String value = option.getString("value");

                RadioButton radioButton = new RadioButton(ctx);
                radioButton.setText(label);
                TextViewCompat.setTextAppearance(radioButton,
                    com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                // M3 list item text color: onSurface (#1D1B20)
                // Use android.R.attr.textColorPrimary which maps to onSurface in M3 themes
                int onSurfaceColor = MaterialColors.getColor(ctx, android.R.attr.textColorPrimary, 0xFF1D1B20);
                radioButton.setTextColor(onSurfaceColor);
                radioButton.setId(View.generateViewId());
                radioButton.setPadding((int) (8 * density), itemPadding, 0, itemPadding);

                if (value.equals(selectedValue)) {
                    radioButton.setChecked(true);
                }

                final String finalValue = value;
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedValue = finalValue;
                        // M3 guideline: Enable confirming action when a choice is made
                        if (okBtn != null) {
                            okBtn.setEnabled(true);
                        }
                    }
                });

                radioGroup.addView(radioButton);
            }

            container.addView(radioGroup);
        } catch (JSONException e) {
            // Handle error
        }
    }

    private void addMultiSelectContent(LinearLayout container, String optionsJson, String selectedValuesJson) {
        if (optionsJson == null) return;

        // Parse selected values
        if (selectedValuesJson != null) {
            try {
                JSONArray selected = new JSONArray(selectedValuesJson);
                for (int i = 0; i < selected.length(); i++) {
                    selectedValues.add(selected.getString(i));
                }
            } catch (JSONException e) {
                // Ignore parsing errors
            }
        }

        Context ctx = getThemedContext();
        float density = getResources().getDisplayMetrics().density;
        int itemPadding = (int) (20 * density);

        try {
            JSONArray options = new JSONArray(optionsJson);

            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String label = option.getString("label");
                String value = option.getString("value");

                CheckBox checkBox = new CheckBox(ctx);
                checkBox.setText(label);
                TextViewCompat.setTextAppearance(checkBox,
                    com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                // M3 list item text color: onSurface (#1D1B20)
                // Use android.R.attr.textColorPrimary which maps to onSurface in M3 themes
                int onSurfaceColor = MaterialColors.getColor(ctx, android.R.attr.textColorPrimary, 0xFF1D1B20);
                checkBox.setTextColor(onSurfaceColor);
                checkBox.setPadding((int) (8 * density), itemPadding, 0, itemPadding);
                checkBox.setChecked(selectedValues.contains(value));

                final String finalValue = value;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedValues.add(finalValue);
                    } else {
                        selectedValues.remove(finalValue);
                    }
                    // M3 guideline: Enable confirming action when a choice is made
                    if (okBtn != null) {
                        okBtn.setEnabled(!selectedValues.isEmpty());
                    }
                });

                container.addView(checkBox);
            }
        } catch (JSONException e) {
            // Handle error
        }
    }

    private void handleConfirm(DialogType type) {
        dismissed = true;
        switch (type) {
            case ALERT:
                if (alertCallback != null) {
                    alertCallback.onDismiss();
                }
                break;
            case CONFIRM:
                if (confirmCallback != null) {
                    confirmCallback.onResult(true);
                }
                break;
            case PROMPT:
                if (promptCallback != null) {
                    String value = inputField != null ? inputField.getText().toString() : "";
                    promptCallback.onResult(value, false);
                }
                break;
            case SINGLE_SELECT:
                if (singleSelectCallback != null) {
                    singleSelectCallback.onResult(selectedValue, false);
                }
                break;
            case MULTI_SELECT:
                if (multiSelectCallback != null) {
                    multiSelectCallback.onResult(selectedValues.toArray(new String[0]), false);
                }
                break;
        }
    }

    private void handleCancel() {
        if (dismissed) return;
        dismissed = true;

        Bundle args = getArguments();
        if (args == null) return;

        DialogType type = DialogType.valueOf(args.getString(ARG_TYPE, DialogType.ALERT.name()));

        switch (type) {
            case ALERT:
                if (alertCallback != null) {
                    alertCallback.onDismiss();
                }
                break;
            case CONFIRM:
                if (confirmCallback != null) {
                    confirmCallback.onResult(false);
                }
                break;
            case PROMPT:
                if (promptCallback != null) {
                    promptCallback.onResult("", true);
                }
                break;
            case SINGLE_SELECT:
                if (singleSelectCallback != null) {
                    singleSelectCallback.onResult(null, true);
                }
                break;
            case MULTI_SELECT:
                if (multiSelectCallback != null) {
                    multiSelectCallback.onResult(new String[0], true);
                }
                break;
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        handleCancel();
    }
}
