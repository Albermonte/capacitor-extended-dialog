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
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### alert(...)

```typescript
alert(options: AlertOptions) => any
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#alertoptions">AlertOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### confirm(...)

```typescript
confirm(options: ConfirmOptions) => any
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#confirmoptions">ConfirmOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### prompt(...)

```typescript
prompt(options: PromptOptions) => any
```

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#promptoptions">PromptOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### singleSelect(...)

```typescript
singleSelect(options: SingleSelectOptions) => any
```

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code><a href="#singleselectoptions">SingleSelectOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### multiSelect(...)

```typescript
multiSelect(options: MultiSelectOptions) => any
```

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#multiselectoptions">MultiSelectOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### AlertOptions

| Prop              | Type                |
| ----------------- | ------------------- |
| **`buttonTitle`** | <code>string</code> |


#### ConfirmOptions

| Prop                    | Type                |
| ----------------------- | ------------------- |
| **`okButtonTitle`**     | <code>string</code> |
| **`cancelButtonTitle`** | <code>string</code> |


#### ConfirmResult

| Prop        | Type                 |
| ----------- | -------------------- |
| **`value`** | <code>boolean</code> |


#### PromptOptions

| Prop                    | Type                |
| ----------------------- | ------------------- |
| **`okButtonTitle`**     | <code>string</code> |
| **`cancelButtonTitle`** | <code>string</code> |
| **`inputPlaceholder`**  | <code>string</code> |
| **`inputText`**         | <code>string</code> |


#### PromptResult

| Prop            | Type                 |
| --------------- | -------------------- |
| **`value`**     | <code>string</code>  |
| **`cancelled`** | <code>boolean</code> |


#### SingleSelectOptions

| Prop                    | Type                |
| ----------------------- | ------------------- |
| **`options`**           | <code>{}</code>     |
| **`selectedValue`**     | <code>string</code> |
| **`okButtonTitle`**     | <code>string</code> |
| **`cancelButtonTitle`** | <code>string</code> |


#### SelectOption

| Prop        | Type                |
| ----------- | ------------------- |
| **`label`** | <code>string</code> |
| **`value`** | <code>string</code> |


#### SingleSelectResult

| Prop            | Type                        |
| --------------- | --------------------------- |
| **`value`**     | <code>string \| null</code> |
| **`cancelled`** | <code>boolean</code>        |


#### MultiSelectOptions

| Prop                    | Type                |
| ----------------------- | ------------------- |
| **`options`**           | <code>{}</code>     |
| **`selectedValues`**    | <code>{}</code>     |
| **`okButtonTitle`**     | <code>string</code> |
| **`cancelButtonTitle`** | <code>string</code> |


#### MultiSelectResult

| Prop            | Type                 |
| --------------- | -------------------- |
| **`values`**    | <code>{}</code>      |
| **`cancelled`** | <code>boolean</code> |

</docgen-api>
