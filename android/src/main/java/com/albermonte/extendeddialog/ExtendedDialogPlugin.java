package com.albermonte.extendeddialog;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;
import org.json.JSONException;

@CapacitorPlugin(name = "ExtendedDialog")
public class ExtendedDialogPlugin extends Plugin {

    private final ExtendedDialog implementation = new ExtendedDialog();

    private DialogStyleOptions extractStyleOptions(PluginCall call) {
        DialogStyleOptions styleOptions = new DialogStyleOptions();
        styleOptions.setButtonColor(call.getString("buttonColor"));
        styleOptions.setCancelButtonColor(call.getString("cancelButtonColor"));
        styleOptions.setTitleColor(call.getString("titleColor"));
        styleOptions.setMessageColor(call.getString("messageColor"));
        styleOptions.setBackgroundColor(call.getString("backgroundColor"));
        styleOptions.setTitleFontSize(call.getDouble("titleFontSize"));
        styleOptions.setMessageFontSize(call.getDouble("messageFontSize"));
        styleOptions.setButtonFontSize(call.getDouble("buttonFontSize"));
        return styleOptions;
    }

    @PluginMethod
    public void alert(PluginCall call) {
        String title = call.getString("title");
        String message = call.getString("message");
        String buttonTitle = call.getString("buttonTitle");
        String mode = call.getString("mode", "basic");
        boolean fullscreen = "fullscreen".equals(mode);
        DialogStyleOptions styleOptions = extractStyleOptions(call);

        if (message == null) {
            call.reject("message is required");
            return;
        }

        implementation.showAlert(getActivity(), title, message, buttonTitle, fullscreen, styleOptions, () -> call.resolve());
    }

    @PluginMethod
    public void confirm(PluginCall call) {
        String title = call.getString("title");
        String message = call.getString("message");
        String okButtonTitle = call.getString("okButtonTitle");
        String cancelButtonTitle = call.getString("cancelButtonTitle");
        String mode = call.getString("mode", "basic");
        boolean fullscreen = "fullscreen".equals(mode);
        DialogStyleOptions styleOptions = extractStyleOptions(call);

        if (message == null) {
            call.reject("message is required");
            return;
        }

        implementation.showConfirm(getActivity(), title, message, okButtonTitle, cancelButtonTitle, fullscreen, styleOptions, (value) -> {
            JSObject result = new JSObject();
            result.put("value", value);
            call.resolve(result);
        });
    }

    @PluginMethod
    public void prompt(PluginCall call) {
        String title = call.getString("title");
        String message = call.getString("message");
        String okButtonTitle = call.getString("okButtonTitle");
        String cancelButtonTitle = call.getString("cancelButtonTitle");
        String inputPlaceholder = call.getString("inputPlaceholder");
        String inputText = call.getString("inputText");
        String mode = call.getString("mode", "basic");
        boolean fullscreen = "fullscreen".equals(mode);
        DialogStyleOptions styleOptions = extractStyleOptions(call);

        if (message == null) {
            call.reject("message is required");
            return;
        }

        implementation.showPrompt(
            getActivity(),
            title,
            message,
            okButtonTitle,
            cancelButtonTitle,
            inputPlaceholder,
            inputText,
            fullscreen,
            styleOptions,
            (value, cancelled) -> {
                JSObject result = new JSObject();
                result.put("value", value != null ? value : "");
                result.put("cancelled", cancelled);
                call.resolve(result);
            }
        );
    }

    @PluginMethod
    public void singleSelect(PluginCall call) {
        String title = call.getString("title");
        String message = call.getString("message");
        JSArray optionsArray = call.getArray("options");
        String selectedValue = call.getString("selectedValue");
        String okButtonTitle = call.getString("okButtonTitle");
        String cancelButtonTitle = call.getString("cancelButtonTitle");
        String mode = call.getString("mode", "basic");
        boolean fullscreen = "fullscreen".equals(mode);
        DialogStyleOptions styleOptions = extractStyleOptions(call);

        if (message == null) {
            call.reject("message is required");
            return;
        }

        if (optionsArray == null) {
            call.reject("options is required");
            return;
        }

        try {
            JSONArray options = new JSONArray(optionsArray.toString());

            implementation.showSingleSelect(
                getActivity(),
                title,
                message,
                options,
                selectedValue,
                okButtonTitle,
                cancelButtonTitle,
                fullscreen,
                styleOptions,
                (value, cancelled) -> {
                    JSObject result = new JSObject();
                    result.put("value", value);
                    result.put("cancelled", cancelled);
                    call.resolve(result);
                }
            );
        } catch (JSONException e) {
            call.reject("Invalid options format");
        }
    }

    @PluginMethod
    public void multiSelect(PluginCall call) {
        String title = call.getString("title");
        String message = call.getString("message");
        JSArray optionsArray = call.getArray("options");
        JSArray selectedValuesArray = call.getArray("selectedValues");
        String okButtonTitle = call.getString("okButtonTitle");
        String cancelButtonTitle = call.getString("cancelButtonTitle");
        String mode = call.getString("mode", "basic");
        boolean fullscreen = "fullscreen".equals(mode);
        DialogStyleOptions styleOptions = extractStyleOptions(call);

        if (message == null) {
            call.reject("message is required");
            return;
        }

        if (optionsArray == null) {
            call.reject("options is required");
            return;
        }

        try {
            JSONArray options = new JSONArray(optionsArray.toString());
            JSONArray selectedValues = selectedValuesArray != null ? new JSONArray(selectedValuesArray.toString()) : null;

            implementation.showMultiSelect(
                getActivity(),
                title,
                message,
                options,
                selectedValues,
                okButtonTitle,
                cancelButtonTitle,
                fullscreen,
                styleOptions,
                (values, cancelled) -> {
                    JSObject result = new JSObject();
                    JSArray valuesResult = new JSArray();
                    if (values != null) {
                        for (String value : values) {
                            valuesResult.put(value);
                        }
                    }
                    result.put("values", valuesResult);
                    result.put("cancelled", cancelled);
                    call.resolve(result);
                }
            );
        } catch (JSONException e) {
            call.reject("Invalid options format");
        }
    }
}
