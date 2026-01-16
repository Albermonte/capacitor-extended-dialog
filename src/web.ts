import { WebPlugin } from '@capacitor/core';

import type { ExtendedDialogPlugin } from './definitions';

export class ExtendedDialogWeb
  extends WebPlugin
  implements ExtendedDialogPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
