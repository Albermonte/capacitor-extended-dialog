package com.albermonte.extendeddialog;

import android.content.DialogInterface;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.Dialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SheetBottomDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_HEADER_LOGO = "headerLogo";
    private static final String ARG_ROWS = "rows";
    private static final String ARG_CONFIRM_BUTTON = "confirmButton";
    private static final String ARG_CANCEL_BUTTON = "cancelButton";
    private static final String ARG_FULLSCREEN = "fullscreen";

    private ExtendedDialog.SheetCallback sheetCallback;
    private boolean dismissed = false;
    private Context themedContext;

    public static SheetBottomDialogFragment newInstance(
        String title,
        String headerLogo,
        String rowsJson,
        String confirmButton,
        String cancelButton,
        boolean fullscreen,
        DialogStyleOptions styleOptions
    ) {
        SheetBottomDialogFragment fragment = new SheetBottomDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_HEADER_LOGO, headerLogo);
        args.putString(ARG_ROWS, rowsJson);
        args.putString(ARG_CONFIRM_BUTTON, confirmButton);
        args.putString(ARG_CANCEL_BUTTON, cancelButton);
        args.putBoolean(ARG_FULLSCREEN, fullscreen);
        if (styleOptions != null) {
            styleOptions.writeToBundle(args);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setSheetCallback(ExtendedDialog.SheetCallback callback) {
        this.sheetCallback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context m3Context = getThemedContext();
        return new BottomSheetDialog(m3Context, R.style.ThemeOverlay_ExtendedDialog_BottomSheetDialog);
    }

    private Context getThemedContext() {
        if (themedContext == null) {
            themedContext = new ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Theme_Material3_DayNight_Dialog);
        }
        return themedContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return null;
        }

        String title = args.getString(ARG_TITLE, "");
        String headerLogo = args.getString(ARG_HEADER_LOGO);
        String rowsJson = args.getString(ARG_ROWS);
        String confirmButton = args.getString(ARG_CONFIRM_BUTTON, "Confirm");
        String cancelButton = args.getString(ARG_CANCEL_BUTTON, "Cancel");
        boolean fullscreen = args.getBoolean(ARG_FULLSCREEN, false);
        DialogStyleOptions styleOptions = DialogStyleOptions.readFromBundle(args);

        Context ctx = getThemedContext();
        float density = getResources().getDisplayMetrics().density;

        // Root layout
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);

        // Apply background color
        if (styleOptions.getBackgroundColor() != null) {
            // Use MaterialShapeDrawable to preserve rounded corners
            ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder()
                .setTopLeftCornerSize(28 * density)
                .setTopRightCornerSize(28 * density)
                .setBottomLeftCornerSize(0)
                .setBottomRightCornerSize(0)
                .build();
            MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeModel);
            shapeDrawable.setFillColor(ColorStateList.valueOf(styleOptions.getBackgroundColor()));
            root.setBackground(shapeDrawable);
        }

        // Drag handle (only shown in basic/non-fullscreen mode)
        if (!fullscreen) {
            View dragHandle = new View(ctx);
            int handleWidth = (int) (32 * density);
            int handleHeight = (int) (4 * density);
            LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(handleWidth, handleHeight);
            handleParams.gravity = Gravity.CENTER_HORIZONTAL;
            handleParams.topMargin = (int) (16 * density);
            handleParams.bottomMargin = (int) (8 * density);
            dragHandle.setLayoutParams(handleParams);
            int outlineVariantColor = MaterialColors.getColor(ctx, android.R.attr.textColorSecondary, 0xFFCAC4D0);
            android.graphics.drawable.GradientDrawable handleDrawable = new android.graphics.drawable.GradientDrawable();
            handleDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            handleDrawable.setCornerRadius(handleHeight / 2f);
            handleDrawable.setColor(outlineVariantColor);
            dragHandle.setBackground(handleDrawable);
            root.addView(dragHandle);
        }

        // ScrollView for content
        ScrollView scrollView = new ScrollView(ctx);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout contentLayout = new LinearLayout(ctx);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        int horizontalPadding = (int) (24 * density);
        int verticalPadding = (int) (16 * density);
        contentLayout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        // Header logo
        if (headerLogo != null && !headerLogo.isEmpty()) {
            ImageView logoView = new ImageView(ctx);
            int logoSize = fullscreen ? (int) (64 * density) : (int) (48 * density);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
            logoParams.gravity = Gravity.CENTER_HORIZONTAL;
            logoParams.bottomMargin = (int) (12 * density);
            logoView.setLayoutParams(logoParams);
            contentLayout.addView(logoView);
            loadImageAsync(logoView, headerLogo);
        }

        // Title
        if (title != null && !title.isEmpty()) {
            TextView titleView = new TextView(ctx);
            titleView.setText(title);
            TextViewCompat.setTextAppearance(titleView,
                com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall);
            if (styleOptions.getTitleColor() != null) {
                titleView.setTextColor(styleOptions.getTitleColor());
            } else {
                int onSurfaceColor = MaterialColors.getColor(ctx, android.R.attr.textColorPrimary, 0xFF1D1B20);
                titleView.setTextColor(onSurfaceColor);
            }
            if (styleOptions.getTitleFontSize() != null) {
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getTitleFontSize());
            }
            titleView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.bottomMargin = (int) (20 * density);
            titleView.setLayoutParams(titleParams);
            contentLayout.addView(titleView);
        }

        // Rows
        if (rowsJson != null) {
            try {
                JSONArray rows = new JSONArray(rowsJson);
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    LinearLayout rowLayout = createSheetRow(ctx, row, styleOptions, density, i < rows.length() - 1);
                    contentLayout.addView(rowLayout);
                }
            } catch (JSONException e) {
                // Handle error
            }
        }

        scrollView.addView(contentLayout);
        root.addView(scrollView);

        // Button container pinned at bottom
        LinearLayout buttonContainer = new LinearLayout(ctx);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(horizontalPadding, (int) (12 * density), horizontalPadding, (int) (4 * density));

        int primaryColor = MaterialColors.getColor(ctx, android.R.attr.colorPrimary, 0xFF6750A4);

        // M3 Expressive L-size button: 48dp height, 24dp horizontal padding
        int buttonHeight = (int) (48 * density);
        int buttonHorizontalPadding = (int) (24 * density);

        // Confirm button (filled style with purple background, white text)
        MaterialButton confirmBtn = new MaterialButton(ctx);
        confirmBtn.setText(confirmButton);
        confirmBtn.setOnClickListener(v -> {
            handleConfirm();
            dismiss();
        });
        if (styleOptions.getButtonColor() != null) {
            confirmBtn.setBackgroundTintList(ColorStateList.valueOf(styleOptions.getButtonColor()));
        } else {
            confirmBtn.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
        int onPrimaryColor = 0xFFFFFFFF;
        confirmBtn.setTextColor(ColorStateList.valueOf(onPrimaryColor));
        confirmBtn.setCornerRadius((int) (24 * density));
        confirmBtn.setMinHeight(buttonHeight);
        confirmBtn.setPadding(buttonHorizontalPadding, 0, buttonHorizontalPadding, 0);
        if (styleOptions.getButtonFontSize() != null) {
            confirmBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
        }
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmBtn.setLayoutParams(confirmParams);
        buttonContainer.addView(confirmBtn);

        // Cancel button (text-only style, no background fill)
        MaterialButton cancelBtn = new MaterialButton(ctx, null, android.R.attr.borderlessButtonStyle);
        cancelBtn.setText(cancelButton);
        cancelBtn.setOnClickListener(v -> {
            handleCancel();
            dismiss();
        });
        cancelBtn.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        cancelBtn.setRippleColor(ColorStateList.valueOf(primaryColor & 0x1FFFFFFF));
        if (styleOptions.getCancelButtonColor() != null) {
            cancelBtn.setTextColor(ColorStateList.valueOf(styleOptions.getCancelButtonColor()));
        } else {
            cancelBtn.setTextColor(ColorStateList.valueOf(primaryColor));
        }
        cancelBtn.setMinHeight(buttonHeight);
        cancelBtn.setPadding(buttonHorizontalPadding, 0, buttonHorizontalPadding, 0);
        if (styleOptions.getButtonFontSize() != null) {
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getButtonFontSize());
        }
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancelBtn.setLayoutParams(cancelParams);
        buttonContainer.addView(cancelBtn);

        root.addView(buttonContainer);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean fullscreen = args != null && args.getBoolean(ARG_FULLSCREEN, false);

        // Get the bottom sheet view from the dialog
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;

            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

            if (fullscreen) {
                // Make the bottom sheet fill the entire screen
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
                behavior.setFitToContents(false);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // Prevent dragging down in fullscreen mode
                behavior.setDraggable(false);
            } else {
                behavior.setFitToContents(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
    }

    private LinearLayout createSheetRow(Context context, JSONObject row, DialogStyleOptions styleOptions, float density, boolean showDivider) throws JSONException {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLayout.setLayoutParams(rowParams);
        rowLayout.setPadding(0, (int) (8 * density), 0, (int) (8 * density));

        // Row logo
        if (row.has("logo") && !row.getString("logo").isEmpty()) {
            ImageView rowLogoView = new ImageView(context);
            int logoSize = (int) (24 * density);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
            logoParams.rightMargin = (int) (12 * density);
            rowLogoView.setLayoutParams(logoParams);
            rowLayout.addView(rowLogoView);
            loadImageAsync(rowLogoView, row.getString("logo"));
        }

        // Row title
        TextView rowTitle = new TextView(context);
        rowTitle.setText(row.getString("title"));
        TextViewCompat.setTextAppearance(rowTitle,
            com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        if (styleOptions.getMessageColor() != null) {
            rowTitle.setTextColor(styleOptions.getMessageColor());
        } else {
            int onSurfaceColor = MaterialColors.getColor(context, android.R.attr.textColorPrimary, 0xFF1D1B20);
            rowTitle.setTextColor(onSurfaceColor);
        }
        if (styleOptions.getMessageFontSize() != null) {
            rowTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
        }
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        rowTitle.setLayoutParams(titleParams);
        rowLayout.addView(rowTitle);

        // Row value
        if (row.has("value") && !row.getString("value").isEmpty()) {
            TextView rowValue = new TextView(context);
            rowValue.setText(row.getString("value"));
            TextViewCompat.setTextAppearance(rowValue,
                com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            int onSurfaceVariantColor = MaterialColors.getColor(context, android.R.attr.textColorSecondary, 0xFF49454F);
            rowValue.setTextColor(onSurfaceVariantColor);
            rowValue.setGravity(Gravity.END);
            if (styleOptions.getMessageFontSize() != null) {
                rowValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
            }
            rowValue.setEllipsize(android.text.TextUtils.TruncateAt.END);
            rowValue.setMaxLines(2);
            LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            rowValue.setLayoutParams(valueParams);
            rowLayout.addView(rowValue);
        }

        // Wrap in container with optional divider
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(rowLayout);

        if (showDivider) {
            View divider = new View(context);
            divider.setBackgroundColor(MaterialColors.getColor(context, android.R.attr.listDivider, 0xFFE0E0E0));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * density));
            divider.setLayoutParams(dividerParams);
            container.addView(divider);
        }

        return container;
    }

    private void loadImageAsync(ImageView imageView, String imageUrl) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Bitmap bitmap;
                if (imageUrl.startsWith("data:")) {
                    String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                } else {
                    InputStream inputStream = new URL(imageUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }
                if (bitmap != null) {
                    imageView.post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                // Silently fail - image won't be displayed
            }
        });
    }

    private void handleConfirm() {
        if (dismissed) return;
        dismissed = true;
        if (sheetCallback != null) {
            sheetCallback.onResult(true);
        }
    }

    private void handleCancel() {
        if (dismissed) return;
        dismissed = true;
        if (sheetCallback != null) {
            sheetCallback.onResult(false);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        handleCancel();
    }
}
