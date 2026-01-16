import { registerPlugin } from '@capacitor/core';

import type { ExtendedDialogPlugin } from './definitions';

const ExtendedDialog = registerPlugin<ExtendedDialogPlugin>('ExtendedDialog', {
  web: () => import('./web').then((m) => new m.ExtendedDialogWeb()),
});

export * from './definitions';
export { ExtendedDialog };
