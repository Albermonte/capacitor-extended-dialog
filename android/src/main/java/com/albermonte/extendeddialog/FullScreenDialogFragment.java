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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final String ARG_BUTTON_COLOR = "buttonColor";
    private static final String ARG_CANCEL_BUTTON_COLOR = "cancelButtonColor";
    private static final String ARG_MESSAGE_FONT_SIZE = "messageFontSize";
    private static final String ARG_BUTTON_FONT_SIZE = "buttonFontSize";

    private ExtendedDialog.AlertCallback alertCallback;
    private ExtendedDialog.ConfirmCallback confirmCallback;
    private ExtendedDialog.PromptCallback promptCallback;
    private ExtendedDialog.SingleSelectCallback singleSelectCallback;
    private ExtendedDialog.MultiSelectCallback multiSelectCallback;

    private EditText inputField;
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
            themedContext = new ContextThemeWrapper(requireContext(),
                com.google.android.material.R.style.Theme_Material3_DayNight_Dialog);
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

        // Store style options
        if (styleOptions != null) {
            if (styleOptions.getButtonColor() != null) {
                args.putInt(ARG_BUTTON_COLOR, styleOptions.getButtonColor());
            }
            if (styleOptions.getCancelButtonColor() != null) {
                args.putInt(ARG_CANCEL_BUTTON_COLOR, styleOptions.getCancelButtonColor());
            }
            if (styleOptions.getMessageFontSize() != null) {
                args.putFloat(ARG_MESSAGE_FONT_SIZE, styleOptions.getMessageFontSize());
            }
            if (styleOptions.getButtonFontSize() != null) {
                args.putFloat(ARG_BUTTON_FONT_SIZE, styleOptions.getButtonFontSize());
            }
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
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog);
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
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        // Get style options
        Integer buttonColor = args.containsKey(ARG_BUTTON_COLOR) ? args.getInt(ARG_BUTTON_COLOR) : null;
        Integer cancelButtonColor = args.containsKey(ARG_CANCEL_BUTTON_COLOR) ? args.getInt(ARG_CANCEL_BUTTON_COLOR) : null;
        Float messageFontSize = args.containsKey(ARG_MESSAGE_FONT_SIZE) ? args.getFloat(ARG_MESSAGE_FONT_SIZE) : null;
        Float buttonFontSize = args.containsKey(ARG_BUTTON_FONT_SIZE) ? args.getFloat(ARG_BUTTON_FONT_SIZE) : null;

        // Create root layout
        Context ctx = getThemedContext();
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(android.R.color.white, null));

        // Create toolbar
        MaterialToolbar toolbar = new MaterialToolbar(ctx);
        toolbar.setTitle(title != null && !title.isEmpty() ? title : "");
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_m3_chip_close);
        toolbar.setNavigationOnClickListener(v -> {
            handleCancel();
            dismiss();
        });

        root.addView(toolbar);

        // Create scroll view for content
        ScrollView scrollView = new ScrollView(ctx);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        ));

        LinearLayout contentLayout = new LinearLayout(ctx);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        contentLayout.setPadding(padding, padding, padding, padding);

        // Add message
        if (message != null && !message.isEmpty()) {
            TextView messageView = new TextView(ctx);
            messageView.setText(message);
            if (messageFontSize != null) {
                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageFontSize);
            } else {
                messageView.setTextSize(16);
            }
            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            messageParams.bottomMargin = padding;
            messageView.setLayoutParams(messageParams);
            contentLayout.addView(messageView);
        }

        // Add type-specific content
        switch (type) {
            case PROMPT:
                addPromptContent(contentLayout, inputPlaceholder, inputText);
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

        // Create button container
        LinearLayout buttonContainer = new LinearLayout(ctx);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(padding, padding / 2, padding, padding);
        buttonContainer.setGravity(android.view.Gravity.END);

        if (type != DialogType.ALERT) {
            // Cancel button
            MaterialButton cancelBtn = new MaterialButton(ctx, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
            cancelBtn.setText(cancelButton);
            cancelBtn.setOnClickListener(v -> {
                handleCancel();
                dismiss();
            });

            // Apply cancel button styles
            if (cancelButtonColor != null) {
                cancelBtn.setTextColor(cancelButtonColor);
                cancelBtn.setStrokeColor(ColorStateList.valueOf(cancelButtonColor));
            }
            if (buttonFontSize != null) {
                cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonFontSize);
            }

            LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cancelParams.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            cancelBtn.setLayoutParams(cancelParams);
            buttonContainer.addView(cancelBtn);
        }

        // OK button
        MaterialButton okBtn = new MaterialButton(ctx);
        okBtn.setText(okButton);
        okBtn.setOnClickListener(v -> {
            handleConfirm(type);
            dismiss();
        });

        // Apply OK button styles
        if (buttonColor != null) {
            okBtn.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        }
        if (buttonFontSize != null) {
            okBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonFontSize);
        }

        buttonContainer.addView(okBtn);

        root.addView(buttonContainer);

        return root;
    }

    private void addPromptContent(LinearLayout container, String placeholder, String text) {
        inputField = new EditText(getThemedContext());
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        if (placeholder != null) {
            inputField.setHint(placeholder);
        }
        if (text != null) {
            inputField.setText(text);
            inputField.setSelection(text.length());
        }
        inputField.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.addView(inputField);
    }

    private void addSingleSelectContent(LinearLayout container, String optionsJson, String selectedValueArg) {
        if (optionsJson == null) return;

        selectedValue = selectedValueArg;
        Context ctx = getThemedContext();

        try {
            JSONArray options = new JSONArray(optionsJson);
            RadioGroup radioGroup = new RadioGroup(ctx);

            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String label = option.getString("label");
                String value = option.getString("value");

                RadioButton radioButton = new RadioButton(ctx);
                radioButton.setText(label);
                radioButton.setId(View.generateViewId());
                radioButton.setPadding(0, 16, 0, 16);

                if (value.equals(selectedValue)) {
                    radioButton.setChecked(true);
                }

                final String finalValue = value;
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedValue = finalValue;
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

        try {
            JSONArray options = new JSONArray(optionsJson);

            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String label = option.getString("label");
                String value = option.getString("value");

                CheckBox checkBox = new CheckBox(ctx);
                checkBox.setText(label);
                checkBox.setPadding(0, 16, 0, 16);
                checkBox.setChecked(selectedValues.contains(value));

                final String finalValue = value;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedValues.add(finalValue);
                    } else {
                        selectedValues.remove(finalValue);
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
