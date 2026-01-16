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

### Key Dependencies
- Android: Material Components (`com.google.android.material:material:1.11.0`)
- iOS: UIKit with Liquid Glass blur effects (iOS 26+), falls back gracefully on iOS 13+
