export interface ExtendedDialogPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
