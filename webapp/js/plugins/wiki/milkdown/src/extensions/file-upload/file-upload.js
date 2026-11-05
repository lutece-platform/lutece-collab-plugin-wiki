import { editorViewCtx, commandsCtx, parserCtx } from '@milkdown/core';
import { clearTextInCurrentBlockCommand } from '@milkdown/preset-commonmark';
import { uploadWithProgress } from '../upload-progress.js';

export function createFileUploadMenuItem(uploadHandlerRef, t) {
  return {
    label: t.blockEdit?.file || 'File',
    icon: '📎',
    onRun: async (ctx) => {
      const commands = ctx.get(commandsCtx);
      commands.call(clearTextInCurrentBlockCommand.key);

      const view = ctx.get(editorViewCtx);
      const parser = ctx.get(parserCtx);

      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '*/*';
      input.multiple = false;

      input.onchange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        await uploadWithProgress(view, uploadHandlerRef, file, t, (tr, from, to, url) => {
          const linkDoc = parser(`[${file.name}](${url})`);
          return linkDoc?.firstChild ? tr.replaceWith(from, to, linkDoc.firstChild.content) : tr.insertText(url, from, to);
        });
      };

      input.click();
    }
  };
}