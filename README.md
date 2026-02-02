# capacitor-extended-dialog

Show native dialogs in Capacitor with Material 3 and Liquid Glass UI

## Install

```bash
npm install capacitor-extended-dialog
npx cap sync
```

## API

<docgen-index>

* [`alert(...)`](#alert)
* [`confirm(...)`](#confirm)
* [`prompt(...)`](#prompt)
* [`singleSelect(...)`](#singleselect)
* [`multiSelect(...)`](#multiselect)
* [`sheet(...)`](#sheet)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### alert(...)

```typescript
alert(options: AlertOptions) => any
```

Show an alert dialog with a single dismiss button.

| Param         | Type                                                  | Description                    |
| ------------- | ----------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#alertoptions">AlertOptions</a></code> | - Alert configuration options. |

**Returns:** <code>any</code>

--------------------


### confirm(...)

```typescript
confirm(options: ConfirmOptions) => any
```

Show a confirmation dialog with OK and Cancel buttons.

| Param         | Type                                                      | Description                      |
| ------------- | --------------------------------------------------------- | -------------------------------- |
| **`options`** | <code><a href="#confirmoptions">ConfirmOptions</a></code> | - Confirm configuration options. |

**Returns:** <code>any</code>

--------------------


### prompt(...)

```typescript
prompt(options: PromptOptions) => any
```

Show a prompt dialog with a text input field.

| Param         | Type                                                    | Description                     |
| ------------- | ------------------------------------------------------- | ------------------------------- |
| **`options`** | <code><a href="#promptoptions">PromptOptions</a></code> | - Prompt configuration options. |

**Returns:** <code>any</code>

--------------------


### singleSelect(...)

```typescript
singleSelect(options: SingleSelectOptions) => any
```

Show a single-select dialog where the user picks one option.

| Param         | Type                                                                | Description                            |
| ------------- | ------------------------------------------------------------------- | -------------------------------------- |
| **`options`** | <code><a href="#singleselectoptions">SingleSelectOptions</a></code> | - Single select configuration options. |

**Returns:** <code>any</code>

--------------------


### multiSelect(...)

```typescript
multiSelect(options: MultiSelectOptions) => any
```

Show a multi-select dialog where the user picks one or more options.

| Param         | Type                                                              | Description                           |
| ------------- | ----------------------------------------------------------------- | ------------------------------------- |
| **`options`** | <code><a href="#multiselectoptions">MultiSelectOptions</a></code> | - Multi select configuration options. |

**Returns:** <code>any</code>

--------------------


### sheet(...)

```typescript
sheet(options: SheetOptions) => any
```

Show a sheet dialog with a header, title, and structured data rows.

| Param         | Type                                                  | Description                    |
| ------------- | ----------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#sheetoptions">SheetOptions</a></code> | - Sheet configuration options. |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### AlertOptions

| Prop              | Type                | Description                   | Default           |
| ----------------- | ------------------- | ----------------------------- | ----------------- |
| **`buttonTitle`** | <code>string</code> | Title for the dismiss button. | <code>"OK"</code> |


#### ConfirmOptions

| Prop                    | Type                | Description                        | Default               |
| ----------------------- | ------------------- | ---------------------------------- | --------------------- |
| **`okButtonTitle`**     | <code>string</code> | Title for the confirmation button. | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code> | Title for the cancel button.       | <code>"Cancel"</code> |


#### ConfirmResult

| Prop        | Type                 | Description                                                 |
| ----------- | -------------------- | ----------------------------------------------------------- |
| **`value`** | <code>boolean</code> | Whether the user confirmed (`true`) or cancelled (`false`). |


#### PromptOptions

| Prop                    | Type                 | Description                                                                                                                                                                                                                                                                                                                                                      | Default               |
| ----------------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------- |
| **`okButtonTitle`**     | <code>string</code>  | Title for the confirmation button.                                                                                                                                                                                                                                                                                                                               | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code>  | Title for the cancel button.                                                                                                                                                                                                                                                                                                                                     | <code>"Cancel"</code> |
| **`inputPlaceholder`**  | <code>string</code>  | Placeholder text shown in the input field when empty.                                                                                                                                                                                                                                                                                                            |                       |
| **`inputText`**         | <code>string</code>  | Pre-filled value for the input field.                                                                                                                                                                                                                                                                                                                            |                       |
| **`focusInput`**        | <code>boolean</code> | Whether to automatically focus the input field and open the keyboard when the dialog appears. Note: On iOS, basic (non-fullscreen) dialogs always auto-focus the input field due to UIAlertController's built-in behavior. This option only takes effect on iOS when using fullscreen mode (`mode: 'fullscreen'`). On Android and web, this works for all modes. | <code>false</code>    |


#### PromptResult

| Prop            | Type                 | Description                                              |
| --------------- | -------------------- | -------------------------------------------------------- |
| **`value`**     | <code>string</code>  | The text entered by the user. Empty string if cancelled. |
| **`cancelled`** | <code>boolean</code> | Whether the user cancelled the dialog.                   |


#### SingleSelectOptions

| Prop                    | Type                | Description                               | Default               |
| ----------------------- | ------------------- | ----------------------------------------- | --------------------- |
| **`options`**           | <code>{}</code>     | List of options to display for selection. |                       |
| **`selectedValue`**     | <code>string</code> | Value of the initially selected option.   |                       |
| **`okButtonTitle`**     | <code>string</code> | Title for the confirmation button.        | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code> | Title for the cancel button.              | <code>"Cancel"</code> |


#### SelectOption

| Prop        | Type                | Description                                 |
| ----------- | ------------------- | ------------------------------------------- |
| **`label`** | <code>string</code> | Display text for the option.                |
| **`value`** | <code>string</code> | Value returned when the option is selected. |


#### SingleSelectResult

| Prop            | Type                        | Description                                               |
| --------------- | --------------------------- | --------------------------------------------------------- |
| **`value`**     | <code>string \| null</code> | The value of the selected option, or `null` if cancelled. |
| **`cancelled`** | <code>boolean</code>        | Whether the user cancelled the dialog.                    |


#### MultiSelectOptions

| Prop                    | Type                | Description                               | Default               |
| ----------------------- | ------------------- | ----------------------------------------- | --------------------- |
| **`options`**           | <code>{}</code>     | List of options to display for selection. |                       |
| **`selectedValues`**    | <code>{}</code>     | Values of the initially selected options. |                       |
| **`okButtonTitle`**     | <code>string</code> | Title for the confirmation button.        | <code>"OK"</code>     |
| **`cancelButtonTitle`** | <code>string</code> | Title for the cancel button.              | <code>"Cancel"</code> |


#### MultiSelectResult

| Prop            | Type                 | Description                                                   |
| --------------- | -------------------- | ------------------------------------------------------------- |
| **`values`**    | <code>{}</code>      | The values of the selected options. Empty array if cancelled. |
| **`cancelled`** | <code>boolean</code> | Whether the user cancelled the dialog.                        |


#### SheetOptions

| Prop                     | Type                                              | Description                                              |
| ------------------------ | ------------------------------------------------- | -------------------------------------------------------- |
| **`headerLogo`**         | <code>string</code>                               | Header logo - supports base64 data URL or HTTP/HTTPS URL |
| **`title`**              | <code>string</code>                               | Sheet title                                              |
| **`rows`**               | <code>{}</code>                                   | Description rows                                         |
| **`confirmButtonTitle`** | <code>string</code>                               | Confirm button title                                     |
| **`cancelButtonTitle`**  | <code>string</code>                               | Cancel button title                                      |
| **`mode`**               | <code><a href="#dialogmode">DialogMode</a></code> | Dialog mode                                              |


#### SheetRow

| Prop        | Type                | Description                                                     |
| ----------- | ------------------- | --------------------------------------------------------------- |
| **`title`** | <code>string</code> | Row title (required)                                            |
| **`logo`**  | <code>string</code> | Optional logo/icon - supports base64 data URL or HTTP/HTTPS URL |
| **`value`** | <code>string</code> | Optional value displayed on the right                           |


#### SheetResult

| Prop            | Type                 | Description                           |
| --------------- | -------------------- | ------------------------------------- |
| **`confirmed`** | <code>boolean</code> | True if confirmed, false if cancelled |


### Type Aliases


#### DialogMode

Dialog presentation mode.

- `'basic'` — Standard modal dialog (default)
- `'fullscreen'` — Full-screen dialog presentation

<code>'basic' | 'fullscreen'</code>

</docgen-api>
