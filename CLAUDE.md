# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
npm run build          # Clean, generate docs, compile TypeScript, bundle with Rollup
npm run verify         # Verify all platforms (iOS, Android, web)
npm run verify:ios     # Build iOS with xcodebuild
npm run verify:android # Build Android with Gradle
npm run lint           # Run ESLint, Prettier, and SwiftLint
npm run fmt            # Auto-fix linting issues
npm run docgen         # Regenerate API docs in README.md
```

## Architecture

This is a Capacitor 6 plugin providing native dialogs with Material 3 (Android) and Liquid Glass (iOS) styling.

### TypeScript Layer (`src/`)
- `definitions.ts` - Plugin API interfaces and types including `SheetOptions`, `SheetRow`, `SheetResult`
- `web.ts` - Browser fallback implementation:
  - alert/confirm/prompt use native `window.alert()`, `window.confirm()`, `window.prompt()`
  - singleSelect/multiSelect/sheet use custom DOM-based modal overlays with styled buttons
  - `createDialogContainer()` / `createSheetDialogContainer()` - Build dialog DOM with style option support
  - `createButton()` - Creates styled buttons; primary buttons use filled background, secondary use transparent with text color
  - Sheet overlay: header logo image, centered title, rows with optional logos/values/separators
- `index.ts` - Plugin registration with Capacitor

### Android (`android/src/main/java/com/albermonte/extendeddialog/`)
- `ExtendedDialogPlugin.java` - Capacitor plugin bridge, handles `@PluginMethod` annotations
  - `extractStyleOptions()` shared helper constructs `DialogStyleOptions` from `PluginCall` parameters
  - Supports all six dialog methods: alert, confirm, prompt, singleSelect, multiSelect, sheet
  - Sheet method requires `title` and `rows`; extracts `headerLogo` as optional string
- `DialogStyleOptions.java` - Style options model class with Bundle serialization
  - Parses hex color strings to `Integer` using `Color.parseColor()`
  - Validates font sizes (must be > 0) and converts to `Float`
  - `writeToBundle()` / `readFromBundle()` - Serializes options for passing to DialogFragment
  - `hasStyles()` - Returns true if any custom style is set
- `ExtendedDialog.java` - Dialog implementation using `MaterialAlertDialogBuilder`
  - `applyM3Typography()` - Applies M3 typography and colors using `MaterialColors.getColor()` to resolve theme attributes dynamically:
    - Title: `TextAppearance_Material3_HeadlineSmall` with `android.R.attr.textColorPrimary` (dynamic, #1D1B20 in light mode)
    - Message: `TextAppearance_Material3_BodyMedium` with `android.R.attr.textColorSecondary` (dynamic, #49454F in light mode)
    - Buttons: `android.R.attr.colorPrimary` (#6750A4 baseline) applied via `ColorStateList.valueOf()` to positive, negative, and neutral buttons
    - Uses `MaterialColors.getColor()` instead of direct R.attr access to avoid NoSuchFieldError in consumer apps
  - `applyDialogStyles()` - Called after `applyM3Typography()` to apply custom style overrides on top of M3 defaults
    - Custom button colors use `ColorStateList.valueOf()` for text color application
  - **Important**: MaterialAlertDialogBuilder uses AppCompat resource IDs. Title accessed via `androidx.appcompat.R.id.alertTitle`, NOT `android.R.id.alertTitle`
  - `showSheet()` delegates to `SheetBottomDialogFragment` for both basic and fullscreen modes
  - `loadImageAsync()` - Loads images from HTTP/HTTPS URLs or base64 data URLs into ImageViews on a background thread
  - `createSheetRow()` - Creates individual sheet rows with logo, title (BodyLarge), value (BodyMedium, right-aligned), and optional divider
- `SheetBottomDialogFragment.java` - Bottom sheet dialog for sheet type using `BottomSheetDialogFragment`:
  - Uses `BottomSheetDialog` with Material 3 theme overlay (`ThemeOverlay_ExtendedDialog_BottomSheetDialog`)
  - Basic mode: peek height set to content size, draggable and expandable
  - Fullscreen mode: expanded state with skip-collapsed behavior
  - `DialogStyleOptions.readFromBundle()` to deserialize style options from arguments
  - Content: header logo (64dp), centered title (HeadlineMedium), rows with optional logos/values/dividers
  - Confirm/Cancel `MaterialButton` with M3 styling; cancel dismissed callback fires on swipe-down
- `FullScreenDialogFragment.java` - Full-screen dialog mode using `DialogFragment` with M3 theme attribute resolution:
  - Uses `DialogStyleOptions.readFromBundle()` to deserialize style options from arguments
  - Edge-to-edge display: status bar and navigation bar made transparent, WindowInsetsCompat used for padding
  - Material components: `TextInputLayout` + `TextInputEditText` for prompt inputs
  - Message text: `android.R.attr.textColorSecondary` (#49454F fallback) for onSurfaceVariant styling
  - RadioButton/CheckBox text: `android.R.attr.textColorPrimary` (#1D1B20 fallback) for onSurface styling
  - Typography: `TextAppearance_Material3_BodyMedium` for messages, `TextAppearance_Material3_BodyLarge` for list items
  - Navigation icon tinted with `android.R.attr.textColorPrimary` for dark/light theme visibility
- Theme wrapper pattern: Both implementations use `getThemedContext()` to wrap context with `Theme.Material3.DayNight.Dialog`, ensuring Material components work regardless of app's base theme
- Automatic dark mode support via `DayNight` theme variant and dynamic theme attribute resolution

### iOS (`ios/Sources/ExtendedDialogPlugin/`)
- `ExtendedDialogPlugin.swift` - Capacitor plugin bridge with `CAPPluginMethod` definitions
  - Registers six methods: alert, confirm, prompt, singleSelect, multiSelect, sheet
  - `extractStyleOptions()` constructs `DialogStyleOptions` from `CAPPluginCall` parameters
- `ExtendedDialog.swift` - Dialog implementation using `UIAlertController`
  - `DialogStyleOptions` struct with hex color parsing (supports 6 and 8 digit hex including alpha)
  - `hasStyles` computed property to check if any custom style is set
  - `SelectOption` and `SheetRow` structs for option/row data
  - Basic single select uses `.actionSheet` style; basic multi select uses sheet presentation (`.pageSheet` with detents)
  - `applyLiquidGlassStyle()` - Applies Liquid Glass (iOS 26+) with `.continuous` corner curve; falls back to 28pt corner radius on older iOS
  - `applyMessageStyle()` - Sets attributed message with custom font size via KVC (`attributedMessage` key)
  - `presentAlert()` - Configures popover for iPad (centered source rect, no arrow)
  - `presentFullScreen()` - Presents with `.fullScreen` modal style
  - `presentBasicSheet()` - Presents with `.pageSheet`; iOS 16+ uses dynamic custom detent based on `preferredContentSize` (capped at 80% of container) with `.large()` fallback; iOS 15 uses `[.large()]` only; calls `loadViewIfNeeded()` before configuring detents; grabber visible
  - `getTopViewController()` - Walks the presentation chain, skipping view controllers that are being dismissed or have no window (fixes presentation on dismissed controllers)
  - Sheet dialogs: sets `headerLogo` and `sheetRows` properties on `FullScreenDialogViewController` before presenting
- `FullScreenDialogViewController.swift` - Full-screen dialog mode
  - `DialogType` enum: alert, confirm, prompt, singleSelect, multiSelect, sheet
  - Close button: iOS 26+ uses `UIButton.Configuration.glass()` with SF Symbol; pre-26 uses manual shadow/background styling
  - Close button hidden in basic sheet mode (`.pageSheet` presentation); visible in fullscreen sheet mode
  - OK button: iOS 26+ uses `.prominentGlass()` configuration; pre-26 uses filled background with corner radius
  - Cancel button: iOS 26+ uses `.glass()` configuration with capsule corner style; pre-26 uses bordered style. For sheets, `configureCancelButtonForSheet()` applies plain text style (no border)
  - Default background: `.systemGroupedBackground`; overridable via `backgroundColor` style option
  - Sheet content wrapped in `UIScrollView` for scrollable rows; non-sheet dialogs use direct constraints
  - Sheet content: header logo (48pt basic / 64pt fullscreen based on `modalPresentationStyle`), centered title (20pt semibold), rows in rounded container with `.secondarySystemGroupedBackground` (16pt corner radius on iOS 26+, 12pt otherwise)
  - Sheet button layout: vertical stack with OK button first, cancel below; total height 112pt when both present (two 50pt buttons + 12pt spacing)
  - Row layout: optional logo (24pt), title label (uses `messageFontSize` style), optional right-aligned value label (`.secondaryLabel`), thin separator (1px scaled) between rows
  - Image loading: supports base64 data URLs and HTTP/HTTPS via `URLSession`
  - Keyboard handling: animates button stack bottom constraint on keyboard show/hide; tap gesture dismisses keyboard
  - Table view for singleSelect/multiSelect options with checkmark accessory and 52pt row height

### Dialog Types
All six dialog types support two modes:
- `basic` - Standard modal dialog (default)
- `fullscreen` - Full-screen dialog presentation

Dialog types:
- `alert` - Simple message with single dismiss button
- `confirm` - Message with OK/Cancel buttons returning boolean
- `prompt` - Message with text input field, returns string value and cancelled flag
- `singleSelect` - Radio-style selection from options list, returns selected value
- `multiSelect` - Checkbox-style selection from options list, returns array of selected values
- `sheet` - Structured layout with header logo, title, and data rows; returns confirmed boolean
  - Android basic: SheetBottomDialogFragment with BottomSheetDialog (peek height matches content)
  - Android fullscreen: SheetBottomDialogFragment in expanded mode with skip-collapsed
  - iOS basic: FullScreenDialogViewController presented as `.pageSheet` with detents
  - iOS fullscreen: FullScreenDialogViewController presented as `.fullScreen`
  - Rows support: title (required), logo (optional, data URL or HTTP), value (optional, right-aligned)

### Prompt Input Focus
Prompt dialogs accept an optional `focusInput` boolean parameter to automatically focus the input field and open the keyboard:
- **iOS basic mode**: Input always auto-focuses due to UIAlertController's built-in behavior
- **iOS fullscreen mode**: Controlled by `focusInput` parameter
- **Android & Web**: Controlled by `focusInput` parameter in both modes
- Default: `false` (except iOS basic mode)

### Style Options
All dialogs accept optional `DialogStyleOptions` for customization:
- `buttonColor` - OK/primary button color (hex, e.g., "#FF5722")
- `cancelButtonColor` - Cancel button color (hex)
- `titleColor` - Title text color (hex)
- `messageColor` - Message text color (hex)
- `backgroundColor` - Dialog background color (hex)
- `titleFontSize` - Title font size in sp/pt
- `messageFontSize` - Message font size in sp/pt
- `buttonFontSize` - Button font size in sp/pt

### Key Files for Adding New Options
When adding new style options, update these files:
1. `src/definitions.ts` - TypeScript interface
2. `android/.../DialogStyleOptions.java` - Android model class (add field, getter/setter, update `writeToBundle()`/`readFromBundle()`, and `hasStyles()`)
3. `android/.../ExtendedDialogPlugin.java` - Extract option from call using `extractStyleOptions()`
4. `android/.../ExtendedDialog.java` - Apply to basic dialogs
5. `android/.../FullScreenDialogFragment.java` - Apply to fullscreen dialogs (uses `DialogStyleOptions.readFromBundle()`)
6. `ios/.../ExtendedDialog.swift` - iOS `DialogStyleOptions` struct and basic dialogs
7. `ios/.../ExtendedDialogPlugin.swift` - Extract option from call
8. `ios/.../FullScreenDialogViewController.swift` - Apply to fullscreen dialogs
9. `src/web.ts` - Web fallback implementation

### Key Dependencies
- Android: Material Components (`com.google.android.material:material:1.12.0`)
- iOS: UIKit with Liquid Glass button styling (iOS 26+ via `UIButton.Configuration.glass()` / `.prominentGlass()`), falls back to manual button styling on older iOS
- iOS presentation: `.pageSheet` with custom detents requires iOS 16+ (falls back to `.large()` on iOS 15); `.fullScreen` works on iOS 13+

### References
- [Material Design 3 Dialogs (Android)](https://github.com/material-components/material-components-android/blob/master/docs/components/Dialog.md) - Official M3 dialog implementation guide
