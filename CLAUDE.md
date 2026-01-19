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
- `ExtendedDialog.java` - Dialog implementation using `MaterialAlertDialogBuilder`
- `FullScreenDialogFragment.java` - Full-screen dialog mode using `DialogFragment`

### iOS (`ios/Sources/ExtendedDialogPlugin/`)
- `ExtendedDialogPlugin.swift` - Capacitor plugin bridge with `CAPPluginMethod` definitions
- `ExtendedDialog.swift` - Dialog implementation using `UIAlertController`
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
2. `android/.../DialogStyleOptions.java` - Android model class
3. `android/.../ExtendedDialogPlugin.java` - Extract option from call
4. `android/.../ExtendedDialog.java` - Apply to basic dialogs
5. `android/.../FullScreenDialogFragment.java` - Apply to fullscreen dialogs
6. `ios/.../ExtendedDialog.swift` - iOS model struct and basic dialogs
7. `ios/.../ExtendedDialogPlugin.swift` - Extract option from call
8. `ios/.../FullScreenDialogViewController.swift` - Apply to fullscreen dialogs
9. `src/web.ts` - Web fallback implementation

### Key Dependencies
- Android: Material Components (`com.google.android.material:material:1.11.0`)
- iOS: UIKit with Liquid Glass blur effects (iOS 26+), falls back gracefully on iOS 13+

### References
- [Material Design 3 Dialogs (Android)](https://github.com/material-components/material-components-android/blob/master/docs/components/Dialog.md) - Official M3 dialog implementation guide
