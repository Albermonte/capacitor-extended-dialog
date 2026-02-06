package com.albermonte.extendeddialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_BUTTON = "confirmButton";
    private static final String ARG_CANCEL_BUTTON = "cancelButton";
    private static final String ARG_FULLSCREEN = "fullscreen";
    private static final String ARG_IS_MESSAGE_SHEET = "isMessageSheet";

    private ExtendedDialog.SheetCallback sheetCallback;
    private boolean dismissed = false;
    private Context themedContext;
    private LinearLayout rootLayout;
    private ScrollView scrollView;
    private LinearLayout headerLayout;
    private LinearLayout bodyLayout;
    private LinearLayout buttonContainer;
    private int buttonHorizontalPaddingPx;
    private int buttonTopPaddingPx;
    private int buttonBottomBasePaddingPx;
    private int buttonBottomInsetsPx;
    private int buttonExpandedExtraPaddingPx;
    private boolean applyExpandedButtonOffset;

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
        args.putBoolean(ARG_IS_MESSAGE_SHEET, false);
        if (styleOptions != null) {
            styleOptions.writeToBundle(args);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static SheetBottomDialogFragment newMessageInstance(
        String title,
        String headerLogo,
        String message,
        String confirmButton,
        String cancelButton,
        boolean fullscreen,
        DialogStyleOptions styleOptions
    ) {
        SheetBottomDialogFragment fragment = new SheetBottomDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_HEADER_LOGO, headerLogo);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_BUTTON, confirmButton);
        args.putString(ARG_CANCEL_BUTTON, cancelButton);
        args.putBoolean(ARG_FULLSCREEN, fullscreen);
        args.putBoolean(ARG_IS_MESSAGE_SHEET, true);
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
        String message = args.getString(ARG_MESSAGE, "");
        String confirmButton = args.getString(ARG_CONFIRM_BUTTON, "Confirm");
        String cancelButton = args.getString(ARG_CANCEL_BUTTON, "Cancel");
        boolean fullscreen = args.getBoolean(ARG_FULLSCREEN, false);
        boolean isMessageSheet = args.getBoolean(ARG_IS_MESSAGE_SHEET, false);
        DialogStyleOptions styleOptions = DialogStyleOptions.readFromBundle(args);

        Context ctx = getThemedContext();
        float density = getResources().getDisplayMetrics().density;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int initialRootHeight = fullscreen ? screenHeight : (int) (screenHeight * 0.5f);

        // Root layout
        rootLayout = new LinearLayout(ctx);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, initialRootHeight));

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
            rootLayout.setBackground(shapeDrawable);
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
            rootLayout.addView(dragHandle);
        }

        int horizontalPadding = (int) (24 * density);
        int verticalPadding = (int) (16 * density);

        // Header (logo + title) - not scrollable
        headerLayout = new LinearLayout(ctx);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, (int) (8 * density));

        // Header logo
        if (headerLogo != null && !headerLogo.isEmpty()) {
            ImageView logoView = new ImageView(ctx);
            int logoSize = fullscreen ? (int) (64 * density) : (int) (48 * density);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
            logoParams.gravity = Gravity.CENTER_HORIZONTAL;
            logoParams.bottomMargin = (int) (12 * density);
            logoView.setLayoutParams(logoParams);

            // Apply corner radius (default 8dp, -1 for circle)
            Float cornerRadius = styleOptions.getHeaderLogoCornerRadius();
            float radiusDp = cornerRadius != null ? cornerRadius : 8f;
            float radiusPx;
            if (radiusDp < 0) {
                // -1 means full circle: radius = half of logo size
                radiusPx = logoSize / 2f;
            } else {
                radiusPx = radiusDp * density;
            }
            if (radiusPx > 0) {
                logoView.setClipToOutline(true);
                logoView.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
                    }
                });
            }

            headerLayout.addView(logoView);
            loadImageAsync(logoView, headerLogo);
        }

        // Title
        if (title != null && !title.isEmpty()) {
            TextView titleView = new TextView(ctx);
            titleView.setText(title);
            TextViewCompat.setTextAppearance(titleView, com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall);
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
            headerLayout.addView(titleView);
        }
        rootLayout.addView(headerLayout);

        // ScrollView for body content (rows/message only)
        scrollView = new ScrollView(ctx);
        scrollView.setFillViewport(true);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bodyLayout = new LinearLayout(ctx);
        bodyLayout.setOrientation(LinearLayout.VERTICAL);
        bodyLayout.setPadding(horizontalPadding, 0, horizontalPadding, verticalPadding);

        // Message (for message sheet)
        if (isMessageSheet && message != null && !message.isEmpty()) {
            LinearLayout messageContainer = new LinearLayout(ctx);
            messageContainer.setOrientation(LinearLayout.VERTICAL);
            int messagePadding = (int) (16 * density);
            messageContainer.setPadding(messagePadding, messagePadding, messagePadding, messagePadding);

            ShapeAppearanceModel messageShape = ShapeAppearanceModel.builder()
                .setAllCornerSizes(12 * density)
                .build();
            MaterialShapeDrawable messageBackground = new MaterialShapeDrawable(messageShape);
            int surfaceVariant = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorSurfaceVariant, 0xFFE7E0EC);
            messageBackground.setFillColor(ColorStateList.valueOf(surfaceVariant));
            messageContainer.setBackground(messageBackground);

            TextView messageView = new TextView(ctx);
            messageView.setText(message);
            messageView.setLineSpacing(0, 1.2f);
            TextViewCompat.setTextAppearance(messageView, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            if (styleOptions.getMessageColor() != null) {
                messageView.setTextColor(styleOptions.getMessageColor());
            } else {
                int onSurfaceVariantColor = MaterialColors.getColor(ctx, android.R.attr.textColorSecondary, 0xFF49454F);
                messageView.setTextColor(onSurfaceVariantColor);
            }
            if (styleOptions.getMessageFontSize() != null) {
                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, styleOptions.getMessageFontSize());
            }
            messageContainer.addView(messageView);

            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            messageParams.bottomMargin = (int) (24 * density);
            messageContainer.setLayoutParams(messageParams);
            bodyLayout.addView(messageContainer);
        }

        // Rows
        if (!isMessageSheet && rowsJson != null) {
            try {
                JSONArray rows = new JSONArray(rowsJson);
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    LinearLayout rowLayout = createSheetRow(ctx, row, styleOptions, density, i < rows.length() - 1);
                    bodyLayout.addView(rowLayout);
                }
            } catch (JSONException e) {
                // Handle error
            }
        }

        scrollView.addView(bodyLayout);
        rootLayout.addView(scrollView);

        // Button container pinned at bottom
        buttonContainer = new LinearLayout(ctx);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        float topSpacing = styleOptions.getContentButtonSpacing() != null ? styleOptions.getContentButtonSpacing() : 12f;
        int buttonTopPadding = (int) (topSpacing * density);
        int baseBottomPadding = (int) ((fullscreen ? 4f : 12f) * density);
        int nonFullscreenExtraBottomPadding = fullscreen ? 0 : (int) (4 * density);
        int bottomPaddingWithoutInsets = baseBottomPadding + nonFullscreenExtraBottomPadding;

        buttonHorizontalPaddingPx = horizontalPadding;
        buttonTopPaddingPx = buttonTopPadding;
        buttonBottomBasePaddingPx = bottomPaddingWithoutInsets;
        buttonBottomInsetsPx = 0;
        buttonExpandedExtraPaddingPx = (int) (48 * density);
        applyExpandedButtonOffset = fullscreen;

        applyButtonContainerPadding();

        ViewCompat.setOnApplyWindowInsetsListener(buttonContainer, (viewWithInsets, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets gestureInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            Insets tappableInsets = windowInsets.getInsets(WindowInsetsCompat.Type.tappableElement());
            buttonBottomInsetsPx = Math.max(systemBarInsets.bottom, Math.max(gestureInsets.bottom, tappableInsets.bottom));
            applyButtonContainerPadding();
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(buttonContainer);

        int primaryColor = MaterialColors.getColor(ctx, android.R.attr.colorPrimary, 0xFF6750A4);

        // M3 Expressive L-size button: 48dp height, 24dp horizontal padding
        int buttonHeight = (int) (48 * density);
        int buttonHorizontalPadding = (int) (24 * density);

        // Confirm button (filled style with purple background, white text)
        MaterialButton confirmBtn = new MaterialButton(ctx);
        confirmBtn.setText(confirmButton);
        confirmBtn.setOnClickListener((v) -> {
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        confirmBtn.setLayoutParams(confirmParams);
        buttonContainer.addView(confirmBtn);

        // Cancel button (text-only style, no background fill)
        MaterialButton cancelBtn = new MaterialButton(ctx, null, android.R.attr.borderlessButtonStyle);
        cancelBtn.setText(cancelButton);
        cancelBtn.setOnClickListener((v) -> {
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cancelBtn.setLayoutParams(cancelParams);
        buttonContainer.addView(cancelBtn);

        rootLayout.addView(buttonContainer);

        return rootLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean fullscreen = args != null && args.getBoolean(ARG_FULLSCREEN, false);

        // Get the bottom sheet view from the dialog
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        dialog.setOnShowListener((dialogInterface) -> {
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
                setExpandedButtonOffset(true);
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View sheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            setExpandedButtonOffset(true);
                            setRootHeight(getVisibleSheetHeight(sheet));
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View sheet, float slideOffset) {
                        setExpandedButtonOffset(true);
                        setRootHeight(getVisibleSheetHeight(sheet));
                    }
                });
                bottomSheet.post(() -> setRootHeight(getVisibleSheetHeight(bottomSheet)));
            } else {
                // Multi-stop bottom sheet: drag up to fullscreen, drag down to dismiss
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                behavior.setFitToContents(false);
                behavior.setSkipCollapsed(true);
                behavior.setHideable(true);
                behavior.setDraggable(true);

                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View sheet, int newState) {
                        if (
                            newState == BottomSheetBehavior.STATE_HALF_EXPANDED ||
                            newState == BottomSheetBehavior.STATE_EXPANDED
                        ) {
                            setExpandedButtonOffset(newState == BottomSheetBehavior.STATE_EXPANDED);
                            setRootHeight(getVisibleSheetHeight(sheet));
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View sheet, float slideOffset) {
                        setExpandedButtonOffset(slideOffset >= 0.9f);
                        setRootHeight(getVisibleSheetHeight(sheet));
                    }
                });

                bottomSheet.post(() -> {
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    int width = bottomSheet.getWidth() > 0
                        ? bottomSheet.getWidth()
                        : getResources().getDisplayMetrics().widthPixels;

                    int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
                    int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

                    int headerHeight = 0;
                    if (headerLayout != null) {
                        headerLayout.measure(widthSpec, heightSpec);
                        headerHeight = headerLayout.getMeasuredHeight();
                    }

                    int bodyHeight = 0;
                    if (bodyLayout != null) {
                        bodyLayout.measure(widthSpec, heightSpec);
                        bodyHeight = bodyLayout.getMeasuredHeight();
                    }

                    int btnHeight = 0;
                    if (buttonContainer != null) {
                        buttonContainer.measure(widthSpec, heightSpec);
                        btnHeight = buttonContainer.getMeasuredHeight();
                    }

                    // Add drag handle height (~28dp)
                    float density = getResources().getDisplayMetrics().density;
                    int dragHandleHeight = (int) (28 * density);

                    int contentRequiredHeight = headerHeight + bodyHeight + btnHeight + dragHandleHeight;
                    int desiredHeight = clampInitialSheetHeight(screenHeight, contentRequiredHeight);
                    float ratio = calculateHalfExpandedRatio(screenHeight, desiredHeight);

                    setRootHeight(desiredHeight);
                    behavior.setHalfExpandedRatio(ratio);
                    behavior.setPeekHeight(desiredHeight);
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    bottomSheet.post(() -> setRootHeight(getVisibleSheetHeight(bottomSheet)));
                });
            }
        });
    }

    static int clampInitialSheetHeight(int screenHeight, int contentRequiredHeight) {
        int minHeight = (int) (screenHeight * 0.5f);
        int maxHeight = (int) (screenHeight * 0.8f);
        int clampedToMin = Math.max(contentRequiredHeight, minHeight);
        return Math.min(clampedToMin, maxHeight);
    }

    static float calculateHalfExpandedRatio(int screenHeight, int desiredHeight) {
        if (screenHeight <= 0) {
            return 0.5f;
        }
        float ratio = (float) desiredHeight / screenHeight;
        return Math.max(0.5f, Math.min(ratio, 0.8f));
    }

    private int getVisibleSheetHeight(@NonNull View sheet) {
        View parent = sheet.getParent() instanceof View ? (View) sheet.getParent() : null;
        int parentHeight = parent != null ? parent.getHeight() : getResources().getDisplayMetrics().heightPixels;
        int visibleHeight = parentHeight - sheet.getTop();
        int nonNegativeHeight = Math.max(0, visibleHeight);
        // Clamp to parent bounds to avoid overshoot during spring/drag transitions.
        return Math.min(parentHeight, nonNegativeHeight);
    }

    private void setRootHeight(int targetHeight) {
        if (rootLayout == null || targetHeight <= 0) {
            return;
        }
        ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight);
        } else if (params.height == targetHeight) {
            return;
        }
        params.height = targetHeight;
        rootLayout.setLayoutParams(params);
    }

    private void setExpandedButtonOffset(boolean enabled) {
        if (applyExpandedButtonOffset == enabled) {
            return;
        }
        applyExpandedButtonOffset = enabled;
        applyButtonContainerPadding();
    }

    private void applyButtonContainerPadding() {
        if (buttonContainer == null) {
            return;
        }
        int expandedExtraBottomPadding = applyExpandedButtonOffset ? buttonExpandedExtraPaddingPx : 0;
        int resolvedBottomPadding = buttonBottomBasePaddingPx + buttonBottomInsetsPx + expandedExtraBottomPadding;
        buttonContainer.setPadding(
            buttonHorizontalPaddingPx,
            buttonTopPaddingPx,
            buttonHorizontalPaddingPx,
            resolvedBottomPadding
        );
    }

    private LinearLayout createSheetRow(
        Context context,
        JSONObject row,
        DialogStyleOptions styleOptions,
        float density,
        boolean showDivider
    ) throws JSONException {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
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
        TextViewCompat.setTextAppearance(rowTitle, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
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
            TextViewCompat.setTextAppearance(rowValue, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (1 * density)
            );
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
