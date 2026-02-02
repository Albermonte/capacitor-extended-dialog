package com.albermonte.extendeddialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     * When no custom styles are provided, M3 typography is applied by default.
     */
    private void applyDialogStyles(AlertDialog dialog, DialogStyleOptions styleOptions) {
        // Apply M3 typography defaults first
        applyM3Typography(dialog);

        if (styleOptions == null || !styleOptions.hasStyles()) {
            return;
        }

        // Style positive button
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            if (styleOptions.getButtonColor() != null) {
                positiveButton.setTextColor(ColorStateList.valueOf(styleOptions.getButtonColor()));
            }
            if (styleOptions.getButtonFontSize() != null) {
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
            }
        }

        // Style negative button
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            if (styleOptions.getCancelButtonColor() != null) {
                negativeButton.setTextColor(ColorStateList.valueOf(styleOptions.getCancelButtonColor()));
            }
            if (styleOptions.getButtonFontSize() != null) {
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
            }
        }

        // Style title text
        // MaterialAlertDialogBuilder uses appcompat's alertTitle, not android's
        TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (titleView != null) {
            if (styleOptions.getTitleColor() != null) {
                titleView.setTextColor(styleOptions.getTitleColor());
            }
            if (styleOptions.getTitleFontSize() != null) {
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getTitleFontSize());
            }
        }

        // Style message text
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            if (styleOptions.getMessageFontSize() != null) {
                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
            }
            if (styleOptions.getMessageColor() != null) {
                messageView.setTextColor(styleOptions.getMessageColor());
            }
        }

        // Style background
        if (styleOptions.getBackgroundColor() != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(styleOptions.getBackgroundColor()));
        }
    }

    /**
     * Applies Material 3 typography and colors to dialog title, message, and buttons.
     * M3 dialog specs: Title uses onSurface (#1D1B20), Message uses onSurfaceVariant (#49454F),
     * Buttons use colorPrimary (#6750A4 baseline)
     */
    private void applyM3Typography(AlertDialog dialog) {
        Context ctx = dialog.getContext();

        // Apply M3 HeadlineSmall to title with onSurface color
        // MaterialAlertDialogBuilder uses appcompat's alertTitle, not android's
        TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (titleView != null) {
            TextViewCompat.setTextAppearance(titleView, com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall);
            // M3 dialog headline color: onSurface (#1D1B20)
            // Use MaterialColors for proper theme attribute resolution across different R class configurations
            int onSurfaceColor = MaterialColors.getColor(ctx, android.R.attr.textColorPrimary, 0xFF1D1B20);
            titleView.setTextColor(onSurfaceColor);
        }

        // Apply M3 BodyMedium to message with onSurfaceVariant color
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            TextViewCompat.setTextAppearance(messageView, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            // M3 dialog supporting text color: onSurfaceVariant (#49454F)
            // Use MaterialColors for proper theme attribute resolution
            int onSurfaceVariantColor = MaterialColors.getColor(ctx, android.R.attr.textColorSecondary, 0xFF49454F);
            messageView.setTextColor(onSurfaceVariantColor);
        }

        // Apply M3 colorPrimary to dialog buttons
        // This ensures consistent button colors across all dialogs regardless of app theme
        // Use MaterialColors.getColor with android.R.attr.colorPrimary for better compatibility
        int primaryColor = MaterialColors.getColor(ctx, android.R.attr.colorPrimary, 0xFF6750A4);
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ColorStateList.valueOf(primaryColor));
        }
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(ColorStateList.valueOf(primaryColor));
        }
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (neutralButton != null) {
            neutralButton.setTextColor(ColorStateList.valueOf(primaryColor));
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

    public interface SheetCallback {
        void onResult(boolean confirmed);
    }

    public void showAlert(
        Activity activity,
        String title,
        String message,
        String buttonTitle,
        boolean fullscreen,
        DialogStyleOptions styleOptions,
        AlertCallback callback
    ) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenAlert((FragmentActivity) activity, title, message, buttonTitle, styleOptions, callback);
        } else {
            showBasicAlert(activity, title, message, buttonTitle, styleOptions, callback);
        }
    }

    private void showBasicAlert(
        Activity activity,
        String title,
        String message,
        String buttonTitle,
        DialogStyleOptions styleOptions,
        AlertCallback callback
    ) {
        activity.runOnUiThread(() -> {
            Context themedContext = getThemedContext(activity);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                themedContext,
                R.style.ThemeOverlay_ExtendedDialog_MaterialAlertDialog
            );

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);
            builder.setPositiveButton(buttonTitle != null ? buttonTitle : "OK", (dialog, which) -> callback.onDismiss());
            builder.setOnCancelListener((dialog) -> callback.onDismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenAlert(
        FragmentActivity activity,
        String title,
        String message,
        String buttonTitle,
        DialogStyleOptions styleOptions,
        AlertCallback callback
    ) {
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
                false,
                styleOptions
            );
            fragment.setAlertCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_alert");
        });
    }

    public void showConfirm(
        Activity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        boolean fullscreen,
        DialogStyleOptions styleOptions,
        ConfirmCallback callback
    ) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenConfirm((FragmentActivity) activity, title, message, okButtonTitle, cancelButtonTitle, styleOptions, callback);
        } else {
            showBasicConfirm(activity, title, message, okButtonTitle, cancelButtonTitle, styleOptions, callback);
        }
    }

    private void showBasicConfirm(
        Activity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        ConfirmCallback callback
    ) {
        activity.runOnUiThread(() -> {
            Context themedContext = getThemedContext(activity);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                themedContext,
                R.style.ThemeOverlay_ExtendedDialog_MaterialAlertDialog
            );

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);
            builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK", (dialog, which) -> callback.onResult(true));
            builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel", (dialog, which) ->
                callback.onResult(false)
            );
            builder.setOnCancelListener((dialog) -> callback.onResult(false));

            AlertDialog dialog = builder.create();
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenConfirm(
        FragmentActivity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        ConfirmCallback callback
    ) {
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
                false,
                styleOptions
            );
            fragment.setConfirmCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_confirm");
        });
    }

    public void showPrompt(
        Activity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        String inputPlaceholder,
        String inputText,
        boolean fullscreen,
        boolean focusInput,
        DialogStyleOptions styleOptions,
        PromptCallback callback
    ) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenPrompt(
                (FragmentActivity) activity,
                title,
                message,
                okButtonTitle,
                cancelButtonTitle,
                inputPlaceholder,
                inputText,
                focusInput,
                styleOptions,
                callback
            );
        } else {
            showBasicPrompt(
                activity,
                title,
                message,
                okButtonTitle,
                cancelButtonTitle,
                inputPlaceholder,
                inputText,
                focusInput,
                styleOptions,
                callback
            );
        }
    }

    private void showBasicPrompt(
        Activity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        String inputPlaceholder,
        String inputText,
        boolean focusInput,
        DialogStyleOptions styleOptions,
        PromptCallback callback
    ) {
        activity.runOnUiThread(() -> {
            Context themedContext = getThemedContext(activity);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                themedContext,
                R.style.ThemeOverlay_ExtendedDialog_MaterialAlertDialog
            );

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            builder.setMessage(message);

            // Use Material 3 TextInputLayout with outlined style
            TextInputLayout textInputLayout = new TextInputLayout(themedContext);
            textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

            final TextInputEditText input = new TextInputEditText(textInputLayout.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            if (inputPlaceholder != null) {
                textInputLayout.setHint(inputPlaceholder);
            }
            if (inputText != null) {
                input.setText(inputText);
                input.setSelection(inputText.length());
            }
            textInputLayout.addView(input);

            LinearLayout container = new LinearLayout(themedContext);
            container.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (20 * activity.getResources().getDisplayMetrics().density);
            container.setPadding(padding, padding / 2, padding, 0);
            container.addView(textInputLayout);
            builder.setView(container);

            builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK", (dialog, which) ->
                callback.onResult(input.getText().toString(), false)
            );
            builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel", (dialog, which) ->
                callback.onResult("", true)
            );
            builder.setOnCancelListener((dialog) -> callback.onResult("", true));

            AlertDialog dialog = builder.create();
            if (focusInput) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                input.requestFocus();
            } else {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
            dialog.show();
            applyDialogStyles(dialog, styleOptions);
        });
    }

    private void showFullScreenPrompt(
        FragmentActivity activity,
        String title,
        String message,
        String okButtonTitle,
        String cancelButtonTitle,
        String inputPlaceholder,
        String inputText,
        boolean focusInput,
        DialogStyleOptions styleOptions,
        PromptCallback callback
    ) {
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
                focusInput,
                styleOptions
            );
            fragment.setPromptCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_prompt");
        });
    }

    public void showSingleSelect(
        Activity activity,
        String title,
        String message,
        JSONArray options,
        String selectedValue,
        String okButtonTitle,
        String cancelButtonTitle,
        boolean fullscreen,
        DialogStyleOptions styleOptions,
        SingleSelectCallback callback
    ) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenSingleSelect(
                (FragmentActivity) activity,
                title,
                message,
                options,
                selectedValue,
                okButtonTitle,
                cancelButtonTitle,
                styleOptions,
                callback
            );
        } else {
            showBasicSingleSelect(
                activity,
                title,
                message,
                options,
                selectedValue,
                okButtonTitle,
                cancelButtonTitle,
                styleOptions,
                callback
            );
        }
    }

    private void showBasicSingleSelect(
        Activity activity,
        String title,
        String message,
        JSONArray options,
        String selectedValue,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        SingleSelectCallback callback
    ) {
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

                final int[] selectedIndex = { checkedItem };

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                    getThemedContext(activity),
                    R.style.ThemeOverlay_ExtendedDialog_MaterialAlertDialog
                );

                if (title != null && !title.isEmpty()) {
                    builder.setTitle(title);
                }

                builder.setSingleChoiceItems(labels.toArray(new String[0]), checkedItem, (dialog, which) -> {
                    selectedIndex[0] = which;
                    // M3 guideline: Enable confirming action when a choice is made
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                });

                builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK", (dialog, which) -> {
                    if (selectedIndex[0] >= 0 && selectedIndex[0] < values.size()) {
                        callback.onResult(values.get(selectedIndex[0]), false);
                    } else {
                        callback.onResult(null, false);
                    }
                });
                builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel", (dialog, which) ->
                    callback.onResult(null, true)
                );
                builder.setOnCancelListener((dialog) -> callback.onResult(null, true));

                AlertDialog dialog = builder.create();
                dialog.show();
                applyDialogStyles(dialog, styleOptions);

                // M3 guideline: Disable confirming action until a choice is made
                if (checkedItem < 0) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            } catch (JSONException e) {
                callback.onResult(null, true);
            }
        });
    }

    private void showFullScreenSingleSelect(
        FragmentActivity activity,
        String title,
        String message,
        JSONArray options,
        String selectedValue,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        SingleSelectCallback callback
    ) {
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
                false,
                styleOptions
            );
            fragment.setSingleSelectCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_single_select");
        });
    }

    public void showMultiSelect(
        Activity activity,
        String title,
        String message,
        JSONArray options,
        JSONArray selectedValues,
        String okButtonTitle,
        String cancelButtonTitle,
        boolean fullscreen,
        DialogStyleOptions styleOptions,
        MultiSelectCallback callback
    ) {
        if (fullscreen && activity instanceof FragmentActivity) {
            showFullScreenMultiSelect(
                (FragmentActivity) activity,
                title,
                message,
                options,
                selectedValues,
                okButtonTitle,
                cancelButtonTitle,
                styleOptions,
                callback
            );
        } else {
            showBasicMultiSelect(
                activity,
                title,
                message,
                options,
                selectedValues,
                okButtonTitle,
                cancelButtonTitle,
                styleOptions,
                callback
            );
        }
    }

    private void showBasicMultiSelect(
        Activity activity,
        String title,
        String message,
        JSONArray options,
        JSONArray selectedValues,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        MultiSelectCallback callback
    ) {
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

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                    getThemedContext(activity),
                    R.style.ThemeOverlay_ExtendedDialog_MaterialAlertDialog
                );

                if (title != null && !title.isEmpty()) {
                    builder.setTitle(title);
                }

                builder.setMultiChoiceItems(labels.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        resultSet.add(values.get(which));
                    } else {
                        resultSet.remove(values.get(which));
                    }
                    // M3 guideline: Enable confirming action when a choice is made
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!resultSet.isEmpty());
                });

                builder.setPositiveButton(okButtonTitle != null ? okButtonTitle : "OK", (dialog, which) ->
                    callback.onResult(resultSet.toArray(new String[0]), false)
                );
                builder.setNegativeButton(cancelButtonTitle != null ? cancelButtonTitle : "Cancel", (dialog, which) ->
                    callback.onResult(new String[0], true)
                );
                builder.setOnCancelListener((dialog) -> callback.onResult(new String[0], true));

                AlertDialog dialog = builder.create();
                dialog.show();
                applyDialogStyles(dialog, styleOptions);

                // M3 guideline: Disable confirming action until a choice is made
                if (resultSet.isEmpty()) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            } catch (JSONException e) {
                callback.onResult(new String[0], true);
            }
        });
    }

    private void showFullScreenMultiSelect(
        FragmentActivity activity,
        String title,
        String message,
        JSONArray options,
        JSONArray selectedValues,
        String okButtonTitle,
        String cancelButtonTitle,
        DialogStyleOptions styleOptions,
        MultiSelectCallback callback
    ) {
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
                false,
                styleOptions
            );
            fragment.setMultiSelectCallback(callback);
            fragment.show(activity.getSupportFragmentManager(), "fullscreen_multi_select");
        });
    }

    // MARK: - Sheet

    public void showSheet(
        Activity activity,
        String title,
        String headerLogo,
        JSONArray rows,
        String confirmButtonTitle,
        String cancelButtonTitle,
        boolean fullscreen,
        DialogStyleOptions styleOptions,
        SheetCallback callback
    ) {
        if (!(activity instanceof FragmentActivity)) {
            callback.onResult(false);
            return;
        }
        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        fragmentActivity.runOnUiThread(() -> {
            SheetBottomDialogFragment fragment = SheetBottomDialogFragment.newInstance(
                title,
                headerLogo,
                rows.toString(),
                confirmButtonTitle != null ? confirmButtonTitle : "Confirm",
                cancelButtonTitle != null ? cancelButtonTitle : "Cancel",
                fullscreen,
                styleOptions
            );
            fragment.setSheetCallback(callback);
            fragment.show(fragmentActivity.getSupportFragmentManager(), "bottom_sheet");
        });
    }
}
