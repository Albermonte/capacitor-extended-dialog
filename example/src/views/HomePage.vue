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
