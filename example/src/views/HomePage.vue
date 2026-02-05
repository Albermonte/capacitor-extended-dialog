<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>Extended Dialog Demo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <ion-header collapse="condense">
        <ion-toolbar>
          <ion-title size="large">Extended Dialog</ion-title>
        </ion-toolbar>
      </ion-header>

      <!-- Result Display -->
      <ion-card v-if="lastResult">
        <ion-card-header>
          <ion-card-title>Last Result</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <pre>{{ JSON.stringify(lastResult, null, 2) }}</pre>
        </ion-card-content>
      </ion-card>

      <!-- Alert Section -->
      <ion-list-header>
        <ion-label>Alert Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showAlert('basic')">
          <ion-label>
            <h2>Basic Alert</h2>
            <p>Simple alert with OK button</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showAlert('fullscreen')">
          <ion-label>
            <h2>Fullscreen Alert</h2>
            <p>Alert in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showAlertCustomButton()">
          <ion-label>
            <h2>Custom Button Alert</h2>
            <p>Alert with custom button title</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Confirm Section -->
      <ion-list-header>
        <ion-label>Confirm Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showConfirm('basic')">
          <ion-label>
            <h2>Basic Confirm</h2>
            <p>Yes/No confirmation dialog</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showConfirm('fullscreen')">
          <ion-label>
            <h2>Fullscreen Confirm</h2>
            <p>Confirmation in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showConfirmCustomButtons()">
          <ion-label>
            <h2>Custom Buttons Confirm</h2>
            <p>Confirm with custom button titles</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Prompt Section -->
      <ion-list-header>
        <ion-label>Prompt Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showPrompt('basic')">
          <ion-label>
            <h2>Basic Prompt</h2>
            <p>Simple text input dialog</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPrompt('fullscreen')">
          <ion-label>
            <h2>Fullscreen Prompt</h2>
            <p>Text input in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPromptWithPlaceholder()">
          <ion-label>
            <h2>Prompt with Placeholder</h2>
            <p>Shows placeholder text in input</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPromptPrefilled()">
          <ion-label>
            <h2>Pre-filled Prompt</h2>
            <p>Input with default value</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPromptWithFocus()">
          <ion-label>
            <h2>Prompt with Auto-Focus</h2>
            <p>Keyboard opens automatically</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPromptWithoutFocus()">
          <ion-label>
            <h2>Prompt without Auto-Focus</h2>
            <p>Keyboard opens on tap only</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showPromptFullscreenWithFocus()">
          <ion-label>
            <h2>Fullscreen with Auto-Focus</h2>
            <p>Fullscreen prompt, keyboard opens automatically</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Single Select Section -->
      <ion-list-header>
        <ion-label>Single Select Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showSingleSelect('basic')">
          <ion-label>
            <h2>Basic Single Select</h2>
            <p>Choose one option from a list</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showSingleSelect('fullscreen')">
          <ion-label>
            <h2>Fullscreen Single Select</h2>
            <p>Single select in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showSingleSelectPreselected()">
          <ion-label>
            <h2>Pre-selected Single Select</h2>
            <p>With a default selection</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Multi Select Section -->
      <ion-list-header>
        <ion-label>Multi Select Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showMultiSelect('basic')">
          <ion-label>
            <h2>Basic Multi Select</h2>
            <p>Choose multiple options</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showMultiSelect('fullscreen')">
          <ion-label>
            <h2>Fullscreen Multi Select</h2>
            <p>Multi select in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showMultiSelectPreselected()">
          <ion-label>
            <h2>Pre-selected Multi Select</h2>
            <p>With default selections</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Sheet Dialogs Section -->
      <ion-list-header>
        <ion-label>Sheet Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showSheet('basic')">
          <ion-label>
            <h2>Basic Sheet</h2>
            <p>Confirmation sheet with rows</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showSheet('fullscreen')">
          <ion-label>
            <h2>Fullscreen Sheet</h2>
            <p>Sheet in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showMessageSheet('basic')">
          <ion-label>
            <h2>Message Sheet (Basic)</h2>
            <p>Title + long message with fixed buttons</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showMessageSheet('fullscreen')">
          <ion-label>
            <h2>Message Sheet (Fullscreen)</h2>
            <p>Sign message-style confirmation</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showSheetWithLogo()">
          <ion-label>
            <h2>Sheet with Header Logo</h2>
            <p>Sheet with header image</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showSheetWithRowLogos()">
          <ion-label>
            <h2>Sheet with Row Logos</h2>
            <p>Each row has an icon</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showTransactionSheet()">
          <ion-label>
            <h2>Transaction Confirmation</h2>
            <p>Real-world payment example</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showStyledSheet()">
          <ion-label>
            <h2>Styled Sheet</h2>
            <p>Custom colors and fonts</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Bug Fix Tests Section -->
      <ion-list-header>
        <ion-label>Bug Fix Tests</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showDarkModeButtonSheet('basic')">
          <ion-label>
            <h2>Dark Mode Button (Basic)</h2>
            <p>Verify confirm button text contrast in dark mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showDarkModeButtonSheet('fullscreen')">
          <ion-label>
            <h2>Dark Mode Button (Fullscreen)</h2>
            <p>Verify confirm button text contrast in dark mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showLongValueSheet('basic')">
          <ion-label>
            <h2>Long Value Overflow (Basic)</h2>
            <p>Values should ellipsize, titles remain visible</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showLongValueSheet('fullscreen')">
          <ion-label>
            <h2>Long Value Overflow (Fullscreen)</h2>
            <p>Values should ellipsize, titles remain visible</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Styled Dialogs Section -->
      <ion-list-header>
        <ion-label>Styled Dialogs</ion-label>
      </ion-list-header>
      <ion-list inset>
        <ion-item button @click="showStyledAlert()">
          <ion-label>
            <h2>Styled Alert</h2>
            <p>Custom button color and large message</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showStyledConfirm()">
          <ion-label>
            <h2>Styled Confirm</h2>
            <p>Red cancel, green OK button</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showStyledPrompt()">
          <ion-label>
            <h2>Styled Prompt (Fullscreen)</h2>
            <p>Custom colors in fullscreen mode</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showStyledSingleSelect()">
          <ion-label>
            <h2>Styled Single Select</h2>
            <p>Purple buttons, large text</p>
          </ion-label>
        </ion-item>
        <ion-item button @click="showStyledMultiSelect()">
          <ion-label>
            <h2>Styled Multi Select</h2>
            <p>Orange theme with large buttons</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Spacer for bottom padding -->
      <div style="height: 50px"></div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonList,
  IonListHeader,
  IonItem,
  IonLabel,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent
} from '@ionic/vue';
import { ExtendedDialog } from 'capacitor-extended-dialog';
import type { DialogMode } from 'capacitor-extended-dialog';

const lastResult = ref<unknown>(null);

// Sample options for select dialogs
const fruitOptions = [
  { label: 'Apple', value: 'apple' },
  { label: 'Banana', value: 'banana' },
  { label: 'Cherry', value: 'cherry' },
  { label: 'Date', value: 'date' },
  { label: 'Elderberry', value: 'elderberry' }
];

// Alert handlers
async function showAlert(mode: DialogMode) {
  await ExtendedDialog.alert({
    title: 'Alert',
    message: `This is a ${mode} alert dialog. It displays information to the user.`,
    mode
  });
  lastResult.value = { type: 'alert', mode, dismissed: true };
}

async function showAlertCustomButton() {
  await ExtendedDialog.alert({
    title: 'Success!',
    message: 'Your operation completed successfully.',
    buttonTitle: 'Got it!'
  });
  lastResult.value = { type: 'alert', customButton: 'Got it!', dismissed: true };
}

// Confirm handlers
async function showConfirm(mode: DialogMode) {
  const result = await ExtendedDialog.confirm({
    title: 'Confirm Action',
    message: `Are you sure you want to proceed? This is a ${mode} confirm dialog.`,
    mode
  });
  lastResult.value = { type: 'confirm', mode, confirmed: result.value };
}

async function showConfirmCustomButtons() {
  const result = await ExtendedDialog.confirm({
    title: 'Delete Item?',
    message: 'This action cannot be undone. Do you want to continue?',
    okButtonTitle: 'Delete',
    cancelButtonTitle: 'Keep'
  });
  lastResult.value = { type: 'confirm', customButtons: true, confirmed: result.value };
}

// Prompt handlers
async function showPrompt(mode: DialogMode) {
  const result = await ExtendedDialog.prompt({
    title: 'Enter Name',
    message: `Please enter your name. This is a ${mode} prompt dialog.`,
    mode
  });
  lastResult.value = {
    type: 'prompt',
    mode,
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showPromptWithPlaceholder() {
  const result = await ExtendedDialog.prompt({
    title: 'Email Address',
    message: 'Please enter your email address:',
    inputPlaceholder: 'example@email.com',
    okButtonTitle: 'Submit',
    cancelButtonTitle: 'Skip'
  });
  lastResult.value = {
    type: 'prompt',
    placeholder: 'example@email.com',
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showPromptPrefilled() {
  const result = await ExtendedDialog.prompt({
    title: 'Edit Username',
    message: 'Modify your username:',
    inputText: 'JohnDoe123',
    inputPlaceholder: 'Enter username'
  });
  lastResult.value = {
    type: 'prompt',
    prefilled: 'JohnDoe123',
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showPromptWithFocus() {
  const result = await ExtendedDialog.prompt({
    title: 'Quick Input',
    message: 'The keyboard should open automatically:',
    inputPlaceholder: 'Start typing...',
    focusInput: true
  });
  lastResult.value = {
    type: 'prompt',
    focusInput: true,
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showPromptWithoutFocus() {
  const result = await ExtendedDialog.prompt({
    title: 'Tap to Type',
    message: 'The keyboard should NOT open automatically. Tap the input field to start typing:',
    inputPlaceholder: 'Tap here to type...',
    focusInput: false
  });
  lastResult.value = {
    type: 'prompt',
    focusInput: false,
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showPromptFullscreenWithFocus() {
  const result = await ExtendedDialog.prompt({
    title: 'Fullscreen with Focus',
    message: 'This fullscreen prompt should auto-focus the input and open the keyboard:',
    inputPlaceholder: 'Start typing...',
    mode: 'fullscreen',
    focusInput: true
  });
  lastResult.value = {
    type: 'prompt',
    mode: 'fullscreen',
    focusInput: true,
    value: result.value,
    cancelled: result.cancelled
  };
}

// Single Select handlers
async function showSingleSelect(mode: DialogMode) {
  const result = await ExtendedDialog.singleSelect({
    title: 'Choose Fruit',
    message: `Select your favorite fruit. This is a ${mode} dialog.`,
    options: fruitOptions,
    mode
  });
  lastResult.value = {
    type: 'singleSelect',
    mode,
    selectedValue: result.value,
    cancelled: result.cancelled
  };
}

async function showSingleSelectPreselected() {
  const result = await ExtendedDialog.singleSelect({
    title: 'Change Selection',
    message: 'Cherry is currently selected. Choose a different fruit:',
    options: fruitOptions,
    selectedValue: 'cherry',
    okButtonTitle: 'Confirm',
    cancelButtonTitle: 'Never mind'
  });
  lastResult.value = {
    type: 'singleSelect',
    preselected: 'cherry',
    selectedValue: result.value,
    cancelled: result.cancelled
  };
}

// Multi Select handlers
async function showMultiSelect(mode: DialogMode) {
  const result = await ExtendedDialog.multiSelect({
    title: 'Choose Fruits',
    message: `Select all fruits you like. This is a ${mode} dialog.`,
    options: fruitOptions,
    mode
  });
  lastResult.value = {
    type: 'multiSelect',
    mode,
    selectedValues: result.values,
    cancelled: result.cancelled
  };
}

async function showMultiSelectPreselected() {
  const result = await ExtendedDialog.multiSelect({
    title: 'Update Selection',
    message: 'Apple and Cherry are currently selected. Update your choices:',
    options: fruitOptions,
    selectedValues: ['apple', 'cherry'],
    okButtonTitle: 'Save Changes',
    cancelButtonTitle: 'Discard'
  });
  lastResult.value = {
    type: 'multiSelect',
    preselected: ['apple', 'cherry'],
    selectedValues: result.values,
    cancelled: result.cancelled
  };
}

// Bug Fix Test handlers
async function showDarkModeButtonSheet(mode: DialogMode) {
  const result = await ExtendedDialog.sheet({
    title: 'Dark Mode Test',
    rows: [
      { title: 'Theme', value: 'System Default' },
      { title: 'Contrast', value: 'Button text should be readable' },
      { title: 'Mode', value: mode },
    ],
    confirmButtonTitle: 'Looks Good',
    cancelButtonTitle: 'Cancel',
    mode,
  });
  lastResult.value = { type: 'darkModeTest', mode, confirmed: result.confirmed };
}

async function showLongValueSheet(mode: DialogMode) {
  const result = await ExtendedDialog.sheet({
    title: 'Overflow Test',
    rows: [
      { title: 'Wallet', value: 'NQ07 0000 0000 0000 0000 0000 0000 0000 0000' },
      { title: 'URL', value: 'https://example.com/very/long/path/that/should/be/truncated/properly' },
      { title: 'Description', value: 'This is a really long value that would previously overflow and push the title off screen' },
      { title: 'Short', value: 'OK' },
    ],
    confirmButtonTitle: 'Confirm',
    cancelButtonTitle: 'Cancel',
    mode,
  });
  lastResult.value = { type: 'overflowTest', mode, confirmed: result.confirmed };
}

// Styled Dialog handlers
async function showStyledAlert() {
  await ExtendedDialog.alert({
    title: 'Styled Alert',
    message: 'This alert has custom styling with a larger message font and a custom button color.',
    buttonTitle: 'Awesome!',
    buttonColor: '#FF5722',
    messageFontSize: 18
  });
  lastResult.value = { type: 'styledAlert', dismissed: true };
}

async function showStyledConfirm() {
  const result = await ExtendedDialog.confirm({
    title: 'Delete Account?',
    message: 'This will permanently delete your account. This action cannot be undone.',
    okButtonTitle: 'Delete',
    cancelButtonTitle: 'Keep Account',
    buttonColor: '#4CAF50',
    cancelButtonColor: '#F44336',
    messageFontSize: 16,
    buttonFontSize: 16
  });
  lastResult.value = { type: 'styledConfirm', confirmed: result.value };
}

async function showStyledPrompt() {
  const result = await ExtendedDialog.prompt({
    title: 'Feedback',
    message: 'Please share your thoughts with us:',
    inputPlaceholder: 'Enter your feedback...',
    okButtonTitle: 'Submit',
    cancelButtonTitle: 'Cancel',
    mode: 'fullscreen',
    buttonColor: '#2196F3',
    cancelButtonColor: '#9E9E9E',
    messageFontSize: 18,
    buttonFontSize: 18
  });
  lastResult.value = {
    type: 'styledPrompt',
    value: result.value,
    cancelled: result.cancelled
  };
}

async function showStyledSingleSelect() {
  const result = await ExtendedDialog.singleSelect({
    title: 'Pick a Color',
    message: 'Select your favorite color from the list below:',
    options: [
      { label: 'Red', value: 'red' },
      { label: 'Blue', value: 'blue' },
      { label: 'Green', value: 'green' },
      { label: 'Yellow', value: 'yellow' }
    ],
    okButtonTitle: 'Select',
    cancelButtonTitle: 'Skip',
    buttonColor: '#9C27B0',
    cancelButtonColor: '#7B1FA2',
    messageFontSize: 18,
    buttonFontSize: 16
  });
  lastResult.value = {
    type: 'styledSingleSelect',
    selectedValue: result.value,
    cancelled: result.cancelled
  };
}

async function showStyledMultiSelect() {
  const result = await ExtendedDialog.multiSelect({
    title: 'Select Toppings',
    message: 'Choose your pizza toppings:',
    options: [
      { label: 'Pepperoni', value: 'pepperoni' },
      { label: 'Mushrooms', value: 'mushrooms' },
      { label: 'Olives', value: 'olives' },
      { label: 'Bell Peppers', value: 'peppers' },
      { label: 'Extra Cheese', value: 'cheese' }
    ],
    okButtonTitle: 'Add to Order',
    cancelButtonTitle: 'No Thanks',
    mode: 'fullscreen',
    buttonColor: '#FF9800',
    cancelButtonColor: '#795548',
    messageFontSize: 18,
    buttonFontSize: 18
  });
  lastResult.value = {
    type: 'styledMultiSelect',
    selectedValues: result.values,
    cancelled: result.cancelled
  };
}

// Sheet handlers
async function showSheet(mode: DialogMode) {
  const result = await ExtendedDialog.sheet({
    title: 'Order Summary',
    rows: [
      { title: 'Item', value: 'Premium Package' },
      { title: 'Quantity', value: '1' },
      { title: 'Price', value: '$99.99' },
      { title: 'Tax', value: '$8.00' },
      { title: 'Total', value: '$107.99' }
    ],
    confirmButtonTitle: 'Confirm Order',
    cancelButtonTitle: 'Cancel',
    mode
  });
  lastResult.value = {
    type: 'sheet',
    mode,
    confirmed: result.confirmed
  };
}

async function showMessageSheet(mode: DialogMode) {
  const message = `You are about to sign the following message:\n\n` +
    `I authorize the transfer of funds from my wallet to the recipient. ` +
    `This signature confirms that I understand the transaction details and accept the terms.\n\n` +
    `Network: Testnet\n` +
    `Nonce: 74291\n` +
    `Expires: 10 minutes`;

  const result = await ExtendedDialog.messageSheet({
    title: 'Sign Message',
    message,
    headerLogo: 'https://picsum.photos/seed/sign/80',
    confirmButtonTitle: 'Sign',
    cancelButtonTitle: 'Reject',
    mode,
  });

  lastResult.value = {
    type: 'messageSheet',
    mode,
    confirmed: result.confirmed
  };
}

async function showSheetWithLogo() {
  const result = await ExtendedDialog.sheet({
    title: 'Account Details',
    headerLogo: 'https://picsum.photos/100',
    rows: [
      { title: 'Name', value: 'John Doe' },
      { title: 'Email', value: 'john@example.com' },
      { title: 'Member Since', value: 'Jan 2024' },
      { title: 'Plan', value: 'Premium' }
    ],
    confirmButtonTitle: 'Continue',
    cancelButtonTitle: 'Go Back'
  });
  lastResult.value = {
    type: 'sheetWithLogo',
    confirmed: result.confirmed
  };
}

async function showSheetWithRowLogos() {
  const result = await ExtendedDialog.sheet({
    title: 'Payment Methods',
    rows: [
      { title: 'Credit Card', logo: 'https://picsum.photos/seed/card/40', value: '•••• 4242' },
      { title: 'PayPal', logo: 'https://picsum.photos/seed/paypal/40', value: 'john@email.com' },
      { title: 'Apple Pay', logo: 'https://picsum.photos/seed/apple/40', value: 'Configured' },
      { title: 'Bank Transfer', logo: 'https://picsum.photos/seed/bank/40', value: '•••• 1234' }
    ],
    confirmButtonTitle: 'Select',
    cancelButtonTitle: 'Cancel',
    mode: 'fullscreen'
  });
  lastResult.value = {
    type: 'sheetWithRowLogos',
    confirmed: result.confirmed
  };
}

async function showTransactionSheet() {
  const result = await ExtendedDialog.sheet({
    title: 'Confirm Transfer',
    headerLogo: 'https://picsum.photos/seed/wallet/80',
    rows: [
      { title: 'From', value: 'Checking ••4521' },
      { title: 'To', value: 'Savings ••8832' },
      { title: 'Amount', value: '$500.00' },
      { title: 'Fee', value: '$0.00' },
      { title: 'Date', value: 'Today' }
    ],
    confirmButtonTitle: 'Transfer Now',
    cancelButtonTitle: 'Cancel'
  });
  lastResult.value = {
    type: 'transactionSheet',
    confirmed: result.confirmed
  };
}

async function showStyledSheet() {
  const result = await ExtendedDialog.sheet({
    title: 'Subscription Details',
    headerLogo: 'https://picsum.photos/seed/premium/80',
    rows: [
      { title: 'Plan', value: 'Premium Annual' },
      { title: 'Price', value: '$79.99/year' },
      { title: 'Renewal', value: 'Jan 28, 2027' },
      { title: 'Discount', value: '-20%' }
    ],
    confirmButtonTitle: 'Subscribe',
    cancelButtonTitle: 'Maybe Later',
    mode: 'fullscreen',
    buttonColor: '#4CAF50',
    cancelButtonColor: '#9E9E9E',
    titleColor: '#1976D2',
    buttonFontSize: 18
  });
  lastResult.value = {
    type: 'styledSheet',
    confirmed: result.confirmed
  };
}
</script>

<style scoped>
ion-card {
  margin-bottom: 16px;
}

ion-card pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  margin: 0;
  background: var(--ion-color-light);
  padding: 8px;
  border-radius: 4px;
}

ion-list-header {
  margin-top: 16px;
}
</style>
