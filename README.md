# capacitor-extended-dialog

A Capacitor 6+ plugin for native dialogs — alerts, confirms, prompts, single/multi-select, and structured sheets — with Material 3 styling on Android, Liquid Glass on iOS, and a web fallback. Supports basic and fullscreen presentation modes with customizable colors, fonts, and layout.

## Install

```bash
npm install capacitor-extended-dialog
npx cap sync
```

## Android sheet behavior

- In `mode: 'basic'`, native Android sheets open between 50% and 80% of screen height.
- Dragging up continuously increases available body space while keeping header content at the top and actions pinned to the bottom.
- If sheet content exceeds the 80% initial cap, the body area becomes internally scrollable.
- In `mode: 'fullscreen'`, the same top-content/bottom-actions layout is preserved with a scrollable body area for overflow.

## API

<docgen-index>

* [`alert(...)`](#alert)
* [`confirm(...)`](#confirm)
* [`prompt(...)`](#prompt)
* [`singleSelect(...)`](#singleselect)
* [`multiSelect(...)`](#multiselect)
* [`sheet(...)`](#sheet)
* [`messageSheet(...)`](#messagesheet)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### alert(...)

```typescript
alert(options: AlertOptions) => Promise<void>
```

Show an alert dialog with a single dismiss button.

| Param         | Type                                                  | Description                    |
| ------------- | ----------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#alertoptions">AlertOptions</a></code> | - Alert configuration options. |

--------------------


### confirm(...)

```typescript
confirm(options: ConfirmOptions) => Promise<ConfirmResult>
```

Show a confirmation dialog with OK and Cancel buttons.

| Param         | Type                                                      | Description                      |
| ------------- | --------------------------------------------------------- | -------------------------------- |
| **`options`** | <code><a href="#confirmoptions">ConfirmOptions</a></code> | - Confirm configuration options. |

**Returns:** <code>Promise&lt;<a href="#confirmresult">ConfirmResult</a>&gt;</code>

--------------------


### prompt(...)

```typescript
prompt(options: PromptOptions) => Promise<PromptResult>
```

Show a prompt dialog with a text input field.

| Param         | Type                                                    | Description                     |
| ------------- | ------------------------------------------------------- | ------------------------------- |
| **`options`** | <code><a href="#promptoptions">PromptOptions</a></code> | - Prompt configuration options. |

**Returns:** <code>Promise&lt;<a href="#promptresult">PromptResult</a>&gt;</code>

--------------------


### singleSelect(...)

```typescript
singleSelect(options: SingleSelectOptions) => Promise<SingleSelectResult>
```

Show a single-select dialog where the user picks one option.

| Param         | Type                                                                | Description                            |
| ------------- | ------------------------------------------------------------------- | -------------------------------------- |
| **`options`** | <code><a href="#singleselectoptions">SingleSelectOptions</a></code> | - Single select configuration options. |

**Returns:** <code>Promise&lt;<a href="#singleselectresult">SingleSelectResult</a>&gt;</code>

--------------------


### multiSelect(...)

```typescript
multiSelect(options: MultiSelectOptions) => Promise<MultiSelectResult>
```

Show a multi-select dialog where the user picks one or more options.

| Param         | Type                                                              | Description                           |
| ------------- | ----------------------------------------------------------------- | ------------------------------------- |
| **`options`** | <code><a href="#multiselectoptions">MultiSelectOptions</a></code> | - Multi select configuration options. |

**Returns:** <code>Promise&lt;<a href="#multiselectresult">MultiSelectResult</a>&gt;</code>

--------------------


### sheet(...)

```typescript
sheet(options: SheetOptions) => Promise<SheetResult>
```

Show a sheet dialog with a header, title, and structured data rows.

| Param         | Type                                                  | Description                    |
| ------------- | ----------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#sheetoptions">SheetOptions</a></code> | - Sheet configuration options. |

**Returns:** <code>Promise&lt;<a href="#sheetresult">SheetResult</a>&gt;</code>

--------------------


### messageSheet(...)

```typescript
messageSheet(options: MessageSheetOptions) => Promise<MessageSheetResult>
```

Show a message sheet dialog with an optional header logo, title, and message.

| Param         | Type                                                                | Description                            |
| ------------- | ------------------------------------------------------------------- | -------------------------------------- |
| **`options`** | <code><a href="#messagesheetoptions">MessageSheetOptions</a></code> | - Message sheet configuration options. |

**Returns:** <code>Promise&lt;<a href="#messagesheetresult">MessageSheetResult</a>&gt;</code>

--------------------


### Interfaces


#### AlertOptions

| Prop              | Type                | Description                   | Default           |
| ----------------- | ------------------- | ----------------------------- | ----------------- |
| **`buttonTitle`** | <code>string</code> | Title for the dismiss button. | <code>"OK"</code> |


#### ConfirmResult

| Prop        | Type                 | Description                                                 |
| ----------- | -------------------- | ----------------------------------------------------------- |
| **`value`** | <code>boolean</code> | Whether the user confirmed (`true`) or cancelled (`false`). |


#### ConfirmOptions

| Prop                    | Type                | Description                        | Default               |
| ----------------------- | ------------------- | ---------------------------------- | --------------------- |
| **`okButtonTitle`**     | <code>string</code> | Title for the confirmation button. | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code> | Title for the cancel button.       | <code>"Cancel"</code> |


#### PromptResult

| Prop            | Type                 | Description                                              |
| --------------- | -------------------- | -------------------------------------------------------- |
| **`value`**     | <code>string</code>  | The text entered by the user. Empty string if cancelled. |
| **`cancelled`** | <code>boolean</code> | Whether the user cancelled the dialog.                   |


#### PromptOptions

| Prop                    | Type                 | Description                                                                                                                                                                                                                                                                                                                                                      | Default               |
| ----------------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------- |
| **`okButtonTitle`**     | <code>string</code>  | Title for the confirmation button.                                                                                                                                                                                                                                                                                                                               | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code>  | Title for the cancel button.                                                                                                                                                                                                                                                                                                                                     | <code>"Cancel"</code> |
| **`inputPlaceholder`**  | <code>string</code>  | Placeholder text shown in the input field when empty.                                                                                                                                                                                                                                                                                                            |                       |
| **`inputText`**         | <code>string</code>  | Pre-filled value for the input field.                                                                                                                                                                                                                                                                                                                            |                       |
| **`focusInput`**        | <code>boolean</code> | Whether to automatically focus the input field and open the keyboard when the dialog appears. Note: On iOS, basic (non-fullscreen) dialogs always auto-focus the input field due to UIAlertController's built-in behavior. This option only takes effect on iOS when using fullscreen mode (`mode: 'fullscreen'`). On Android and web, this works for all modes. | <code>false</code>    |


#### SingleSelectResult

| Prop            | Type                        | Description                                               |
| --------------- | --------------------------- | --------------------------------------------------------- |
| **`value`**     | <code>string \| null</code> | The value of the selected option, or `null` if cancelled. |
| **`cancelled`** | <code>boolean</code>        | Whether the user cancelled the dialog.                    |


#### SingleSelectOptions

| Prop                    | Type                        | Description                               | Default               |
| ----------------------- | --------------------------- | ----------------------------------------- | --------------------- |
| **`options`**           | <code>SelectOption[]</code> | List of options to display for selection. |                       |
| **`selectedValue`**     | <code>string</code>         | Value of the initially selected option.   |                       |
| **`okButtonTitle`**     | <code>string</code>         | Title for the confirmation button.        | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code>         | Title for the cancel button.              | <code>"Cancel"</code> |


#### SelectOption

| Prop        | Type                | Description                                 |
| ----------- | ------------------- | ------------------------------------------- |
| **`label`** | <code>string</code> | Display text for the option.                |
| **`value`** | <code>string</code> | Value returned when the option is selected. |


#### MultiSelectResult

| Prop            | Type                  | Description                                                   |
| --------------- | --------------------- | ------------------------------------------------------------- |
| **`values`**    | <code>string[]</code> | The values of the selected options. Empty array if cancelled. |
| **`cancelled`** | <code>boolean</code>  | Whether the user cancelled the dialog.                        |


#### MultiSelectOptions

| Prop                    | Type                        | Description                               | Default               |
| ----------------------- | --------------------------- | ----------------------------------------- | --------------------- |
| **`options`**           | <code>SelectOption[]</code> | List of options to display for selection. |                       |
| **`selectedValues`**    | <code>string[]</code>       | Values of the initially selected options. |                       |
| **`okButtonTitle`**     | <code>string</code>         | Title for the confirmation button.        | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code>         | Title for the cancel button.              | <code>"Cancel"</code> |


#### SheetResult

| Prop            | Type                 | Description                           |
| --------------- | -------------------- | ------------------------------------- |
| **`confirmed`** | <code>boolean</code> | True if confirmed, false if cancelled |


#### SheetOptions

| Prop                     | Type                                              | Description                                              |
| ------------------------ | ------------------------------------------------- | -------------------------------------------------------- |
| **`headerLogo`**         | <code>string</code>                               | Header logo - supports base64 data URL or HTTP/HTTPS URL |
| **`title`**              | <code>string</code>                               | Sheet title                                              |
| **`subtitle`**           | <code>string</code>                               | Optional subtitle displayed between title and rows       |
| **`rows`**               | <code>SheetRow[]</code>                           | Description rows                                         |
| **`confirmButtonTitle`** | <code>string</code>                               | Confirm button title                                     |
| **`cancelButtonTitle`**  | <code>string</code>                               | Cancel button title                                      |
| **`mode`**               | <code><a href="#dialogmode">DialogMode</a></code> | Dialog mode                                              |


#### SheetRow

| Prop        | Type                | Description                                                     |
| ----------- | ------------------- | --------------------------------------------------------------- |
| **`title`** | <code>string</code> | Row title (required)                                            |
| **`logo`**  | <code>string</code> | Optional logo/icon - supports base64 data URL or HTTP/HTTPS URL |
| **`value`** | <code>string</code> | Optional value displayed on the right                           |


#### MessageSheetResult

| Prop            | Type                 | Description                           |
| --------------- | -------------------- | ------------------------------------- |
| **`confirmed`** | <code>boolean</code> | True if confirmed, false if cancelled |


#### MessageSheetOptions

| Prop                     | Type                                              | Description                                              |
| ------------------------ | ------------------------------------------------- | -------------------------------------------------------- |
| **`headerLogo`**         | <code>string</code>                               | Header logo - supports base64 data URL or HTTP/HTTPS URL |
| **`title`**              | <code>string</code>                               | Sheet title                                              |
| **`subtitle`**           | <code>string</code>                               | Optional subtitle displayed between title and message    |
| **`message`**            | <code>string</code>                               | Message body displayed in the sheet                      |
| **`confirmButtonTitle`** | <code>string</code>                               | Confirm button title                                     |
| **`cancelButtonTitle`**  | <code>string</code>                               | Cancel button title                                      |
| **`mode`**               | <code><a href="#dialogmode">DialogMode</a></code> | Dialog mode                                              |


### Type Aliases


#### DialogMode

Dialog presentation mode.

- `'basic'` — Standard modal dialog (default)
- `'fullscreen'` — Full-screen dialog presentation

<code>'basic' | 'fullscreen'</code>

</docgen-api>
