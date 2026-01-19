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
- `definitions.ts` - Plugin API interfaces and types
- `web.ts` - Browser fallback implementation using native alerts and custom modals
- `index.ts` - Plugin registration with Capacitor

### Android (`android/src/main/java/com/albermonte/extendeddialog/`)
- `ExtendedDialogPlugin.java` - Capacitor plugin bridge, handles `@PluginMethod` annotations
  - Extracts style options from plugin calls with M3 fallback defaults in comments (titleColor=#1D1B20, messageColor=#49454F)
  - Default values only used when custom colors not specified; actual rendering uses dynamic theme attributes
- `DialogStyleOptions.java` - Style options model class with Bundle serialization
  - Parses hex color strings to `Integer` using `Color.parseColor()`
  - Validates font sizes (must be > 0) and converts to `Float`
  - `writeToBundle()` / `readFromBundle()` - Serializes options for passing to DialogFragment
  - `hasStyles()` - Returns true if any custom style is set
- `ExtendedDialog.java` - Dialog implementation using `MaterialAlertDialogBuilder`
  - `applyM3Typography()` - Applies M3 typography and colors using theme attributes:
    - Title: `TextAppearance_Material3_HeadlineSmall` with `colorOnSurface` (dynamic, #1D1B20 in light mode)
    - Message: `TextAppearance_Material3_BodyMedium` with `colorOnSurfaceVariant` (dynamic, #49454F in light mode)
    - Buttons: `colorPrimary` (#6750A4 baseline) applied to positive, negative, and neutral buttons
  - `applyDialogStyles()` - Called after `applyM3Typography()` to apply custom style overrides on top of M3 defaults
  - **Important**: MaterialAlertDialogBuilder uses AppCompat resource IDs. Title accessed via `androidx.appcompat.R.id.alertTitle`, NOT `android.R.id.alertTitle`
  - All button styling uses `ColorStateList.valueOf()` for proper state handling
- `FullScreenDialogFragment.java` - Full-screen dialog mode using `DialogFragment` with explicit M3 color tokens:
  - Uses `DialogStyleOptions.readFromBundle()` to deserialize style options from arguments
  - Material components: `TextInputLayout` + `TextInputEditText` for prompt inputs
  - Message text: `colorOnSurfaceVariant` (#49454F in light mode)
  - RadioButton/CheckBox text: `colorOnSurface` (#1D1B20 in light mode)
  - Typography: `TextAppearance_Material3_BodyMedium` for messages, `TextAppearance_Material3_BodyLarge` for list items
- Theme wrapper pattern: Both implementations use `getThemedContext()` to wrap context with `Theme.Material3.DayNight.Dialog`, ensuring Material components work regardless of app's base theme
- Automatic dark mode support via `DayNight` theme variant and dynamic theme attribute resolution

### iOS (`ios/Sources/ExtendedDialogPlugin/`)
- `ExtendedDialogPlugin.swift` - Capacitor plugin bridge with `CAPPluginMethod` definitions
- `ExtendedDialog.swift` - Dialog implementation using `UIAlertController`
  - `DialogStyleOptions` struct with hex color parsing (supports 6 and 8 digit hex including alpha)
  - `hasStyles` computed property to check if any custom style is set
- `FullScreenDialogViewController.swift` - Full-screen dialog mode

### Dialog Types
All five dialog types (alert, confirm, prompt, singleSelect, multiSelect) support two modes:
- `basic` - Standard modal dialog (default)
- `fullscreen` - Full-screen dialog presentation

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
- iOS: UIKit with Liquid Glass blur effects (iOS 26+), falls back gracefully on iOS 13+

### References
- [Material Design 3 Dialogs (Android)](https://github.com/material-components/material-components-android/blob/master/docs/components/Dialog.md) - Official M3 dialog implementation guide
