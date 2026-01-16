package com.albermonte.extendeddialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtendedDialog {

    /**
     * Wraps the given context with a Material3 theme overlay.
     * This ensures MaterialAlertDialogBuilder works regardless of the app's base theme.
     */
    private Context getThemedContext(Context context) {
        return new ContextThemeWrapper(context, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog);
    }

    /**
     * Applies style options to an AlertDialog after it's shown.
     */
    private void applyDialogStyles(AlertDialog dialog, DialogStyleOptions styleOptions) {
        if (styleOptions == null || !styleOptions.hasStyles()) {
            return;
        }

        // Style positive button
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            if (styleOptions.getButtonColor() != null) {
                positiveButton.setTextColor(styleOptions.getButtonColor());
            }
            if (styleOptions.getButtonFontSize() != null) {
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
            }
        }

        // Style negative button
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            if (styleOptions.getCancelButtonColor() != null) {
                negativeButton.setTextColor(styleOptions.getCancelButtonColor());
            }
            if (styleOptions.getButtonFontSize() != null) {
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
            }
        }

        // Style message text
        if (styleOptions.getMessageFontSize() != null) {
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
            }
        }
    }

    public interface AlertCallback {
        void onDismiss();
    }

    public interface ConfirmCallback {
        void onResult(boolean value);
    }

    public interface PromptCallback {
        void onResult(String value, boolean cancelled);
    }

    public interface SingleSelectCallback {
        void onResult(String value, boolean cancelled);
    }

    public interface MultiSelectCallback {
        void onResult(String[] values, boolean cancelled);
    }

    public void showAlert(Activity activity, String title, String message, String buttonTitle,
                          boolean fullscreen, DialogStyleOptions styleOptions, AlertCallback callback) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenAlert((FragmentActivity) activity, title, message, buttonTitle, styleOptions, callback);
        } else {
            showBasicAlert(activity, title, message, buttonTitle, styleOptions, callback);
        }
    }

    private void showBasicAlert(Activity activity, String title, String message,
                                String buttonTitle, DialogStyleOptions styleOptions, AlertCallback callback) {
        activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getThemedContext(activity));

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);
            builder.setPositiveButton(buttonTitle != null ? buttonTitle : "OK",
                (dialog, which) -> callback.onDismiss());
            builder.setOnCancelListener(dialog -> callback.onDismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenAlert(FragmentActivity activity, String title, String message,
                                     String buttonTitle, DialogStyleOptions styleOptions, AlertCallback callback) {
        activity.runOnUiThread(() -> {
            FullScreenDialogFragment fragment = FullScreenDialogFragment.newInstance(
                FullScreenDialogFragment.DialogType.ALERT,
                title,
                message,
                buttonTitle != null ? buttonTitle : "OK",
                null,
                null,
                null,
                null,
                styleOptions
            );
            fragment.setAlertCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_alert");
        });
    }

    public void showConfirm(Activity activity, String title, String message,
                           String okButtonTitle, String cancelButtonTitle,
                           boolean fullscreen, DialogStyleOptions styleOptions, ConfirmCallback callback) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenConfirm((FragmentActivity) activity, title, message,
                okButtonTitle, cancelButtonTitle, styleOptions, callback);
        } else {
            showBasicConfirm(activity, title, message, okButtonTitle, cancelButtonTitle, styleOptions, callback);
        }
    }

    private void showBasicConfirm(Activity activity, String title, String message,
                                  String okButtonTitle, String cancelButtonTitle,
                                  DialogStyleOptions styleOptions, ConfirmCallback callback) {
        activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getThemedContext(activity));

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);
            builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK",
                (dialog, which) -> callback.onResult(true));
            builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                (dialog, which) -> callback.onResult(false));
            builder.setOnCancelListener(dialog -> callback.onResult(false));

            AlertDialog dialog = builder.create();
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenConfirm(FragmentActivity activity, String title, String message,
                                       String okButtonTitle, String cancelButtonTitle,
                                       DialogStyleOptions styleOptions, ConfirmCallback callback) {
        activity.runOnUiThread(() -> {
            FullScreenDialogFragment fragment = FullScreenDialogFragment.newInstance(
                FullScreenDialogFragment.DialogType.CONFIRM,
                title,
                message,
                okButtonTitle != null ? okButtonTitle : "OK",
                cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                null,
                null,
                null,
                styleOptions
            );
            fragment.setConfirmCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_confirm");
        });
    }

    public void showPrompt(Activity activity, String title, String message,
                          String okButtonTitle, String cancelButtonTitle,
                          String inputPlaceholder, String inputText,
                          boolean fullscreen, DialogStyleOptions styleOptions, PromptCallback callback) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenPrompt((FragmentActivity) activity, title, message,
                okButtonTitle, cancelButtonTitle, inputPlaceholder, inputText, styleOptions, callback);
        } else {
            showBasicPrompt(activity, title, message, okButtonTitle, cancelButtonTitle,
                inputPlaceholder, inputText, styleOptions, callback);
        }
    }

    private void showBasicPrompt(Activity activity, String title, String message,
                                 String okButtonTitle, String cancelButtonTitle,
                                 String inputPlaceholder, String inputText,
                                 DialogStyleOptions styleOptions, PromptCallback callback) {
        activity.runOnUiThread(() -> {
            Context themedContext = getThemedContext(activity);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(themedContext);

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);

            final EditText input = new EditText(themedContext);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            if (inputPlaceholder != null) {
                input.setHint(inputPlaceholder);
            }
            if (inputText != null) {
                input.setText(inputText);
                input.setSelection(inputText.length());
            }

            LinearLayout container = new LinearLayout(themedContext);
            container.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (20 * activity.getResources().getDisplayMetrics().density);
            container.setPadding(padding, 0, padding, 0);
            container.addView(input);
            builder.setView(container);

            builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK",
                (dialog, which) -> callback.onResult(input.getText().toString(), false));
            builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                (dialog, which) -> callback.onResult("", true));
            builder.setOnCancelListener(dialog -> callback.onResult("", true));

            AlertDialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenPrompt(FragmentActivity activity, String title, String message,
                                      String okButtonTitle, String cancelButtonTitle,
                                      String inputPlaceholder, String inputText,
                                      DialogStyleOptions styleOptions, PromptCallback callback) {
        activity.runOnUiThread(() -> {
            FullScreenDialogFragment fragment = FullScreenDialogFragment.newInstance(
                FullScreenDialogFragment.DialogType.PROMPT,
                title,
                message,
                okButtonTitle != null ? okButtonTitle : "OK",
                cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                inputPlaceholder,
                inputText,
                null,
                styleOptions
            );
            fragment.setPromptCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_prompt");
        });
    }

    public void showSingleSelect(Activity activity, String title, String message,
                                 JSONArray options, String selectedValue,
                                 String okButtonTitle, String cancelButtonTitle,
                                 boolean fullscreen, DialogStyleOptions styleOptions, SingleSelectCallback callback) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenSingleSelect((FragmentActivity) activity, title, message,
                options, selectedValue, okButtonTitle, cancelButtonTitle, styleOptions, callback);
        } else {
            showBasicSingleSelect(activity, title, message, options, selectedValue,
                okButtonTitle, cancelButtonTitle, styleOptions, callback);
        }
    }

    private void showBasicSingleSelect(Activity activity, String title, String message,
                                       JSONArray options, String selectedValue,
                                       String okButtonTitle, String cancelButtonTitle,
                                       DialogStyleOptions styleOptions, SingleSelectCallback callback) {
        activity.runOnUiThread(() -> {
            try {
                List<String> labels = new ArrayList<>();
                List<String> values = new ArrayList<>();
                int checkedItem = -1;

                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    String label = option.getString("label");
                    String value = option.getString("value");
                    labels.add(label);
                    values.add(value);
                    if (selectedValue != null && selectedValue.equals(value)) {
                        checkedItem = i;
                    }
                }

                final int[] selectedIndex = {checkedItem};

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getThemedContext(activity));

                if (title != null && !title.isEmpty()) {
                    builder.setTitle(title);
                }

                builder.setSingleChoiceItems(
                    labels.toArray(new String[0]),
                    checkedItem,
                    (dialog, which) -> selectedIndex[0] = which
                );

                builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK",
                    (dialog, which) -> {
                        if (selectedIndex[0] >= 0 && selectedIndex[0] < values.size()) {
                            callback.onResult(values.get(selectedIndex[0]), false);
                        } else {
                            callback.onResult(null, false);
                        }
                    });
                builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                    (dialog, which) -> callback.onResult(null, true));
                builder.setOnCancelListener(dialog -> callback.onResult(null, true));

                AlertDialog dialog = builder.create();
                dialog.show();
                applyDialogStyles(dialog, styleOptions);
            } catch (JSONException e) {
                callback.onResult(null, true);
            }
        });
    }

    private void showFullScreenSingleSelect(FragmentActivity activity, String title, String message,
                                            JSONArray options, String selectedValue,
                                            String okButtonTitle, String cancelButtonTitle,
                                            DialogStyleOptions styleOptions, SingleSelectCallback callback) {
        activity.runOnUiThread(() -> {
            FullScreenDialogFragment fragment = FullScreenDialogFragment.newInstance(
                FullScreenDialogFragment.DialogType.SINGLE_SELECT,
                title,
                message,
                okButtonTitle != null ? okButtonTitle : "OK",
                cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                null,
                selectedValue,
                options.toString(),
                styleOptions
            );
            fragment.setSingleSelectCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_single_select");
        });
    }

    public void showMultiSelect(Activity activity, String title, String message,
                                JSONArray options, JSONArray selectedValues,
                                String okButtonTitle, String cancelButtonTitle,
                                boolean fullscreen, DialogStyleOptions styleOptions, MultiSelectCallback callback) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenMultiSelect((FragmentActivity) activity, title, message,
                options, selectedValues, okButtonTitle, cancelButtonTitle, styleOptions, callback);
        } else {
            showBasicMultiSelect(activity, title, message, options, selectedValues,
                okButtonTitle, cancelButtonTitle, styleOptions, callback);
        }
    }

    private void showBasicMultiSelect(Activity activity, String title, String message,
                                      JSONArray options, JSONArray selectedValues,
                                      String okButtonTitle, String cancelButtonTitle,
                                      DialogStyleOptions styleOptions, MultiSelectCallback callback) {
        activity.runOnUiThread(() -> {
            try {
                List<String> labels = new ArrayList<>();
                List<String> values = new ArrayList<>();
                Set<String> selectedSet = new HashSet<>();

                if (selectedValues != null) {
                    for (int i = 0; i < selectedValues.length(); i++) {
                        selectedSet.add(selectedValues.getString(i));
                    }
                }

                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    labels.add(option.getString("label"));
                    values.add(option.getString("value"));
                }

                boolean[] checkedItems = new boolean[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    checkedItems[i] = selectedSet.contains(values.get(i));
                }

                final Set<String> resultSet = new HashSet<>(selectedSet);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getThemedContext(activity));

                if (title != null && !title.isEmpty()) {
                    builder.setTitle(title);
                }

                builder.setMultiChoiceItems(
                    labels.toArray(new String[0]),
                    checkedItems,
                    (dialog, which, isChecked) -> {
                        if (isChecked) {
                            resultSet.add(values.get(which));
                        } else {
                            resultSet.remove(values.get(which));
                        }
                    }
                );

                builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK",
                    (dialog, which) -> callback.onResult(resultSet.toArray(new String[0]), false));
                builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                    (dialog, which) -> callback.onResult(new String[0], true));
                builder.setOnCancelListener(dialog -> callback.onResult(new String[0], true));

                AlertDialog dialog = builder.create();
                dialog.show();
                applyDialogStyles(dialog, styleOptions);
            } catch (JSONException e) {
                callback.onResult(new String[0], true);
            }
        });
    }

    private void showFullScreenMultiSelect(FragmentActivity activity, String title, String message,
                                           JSONArray options, JSONArray selectedValues,
                                           String okButtonTitle, String cancelButtonTitle,
                                           DialogStyleOptions styleOptions, MultiSelectCallback callback) {
        activity.runOnUiThread(() -> {
            String selectedValuesStr = selectedValues != null ? selectedValues.toString() : null;
            FullScreenDialogFragment fragment = FullScreenDialogFragment.newInstance(
                FullScreenDialogFragment.DialogType.MULTI_SELECT,
                title,
                message,
                okButtonTitle != null ? okButtonTitle : "OK",
                cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                null,
                selectedValuesStr,
                options.toString(),
                styleOptions
            );
            fragment.setMultiSelectCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_multi_select");
        });
    }
}
