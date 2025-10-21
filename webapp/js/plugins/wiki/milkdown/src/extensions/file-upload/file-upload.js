import { editorViewCtx, commandsCtx, parserCtx } from '@milkdown/core';
import { clearTextInCurrentBlockCommand } from '@milkdown/preset-commonmark';
import { Slice } from '@milkdown/kit/prose/model';

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

        try {
          const url = await uploadHandlerRef.handler(file);
          const linkMarkdown = `[${file.name}](${url})`;
          const linkDoc = parser(linkMarkdown);

          if (!linkDoc) {
            throw new Error('Failed to parse link markdown');
          }

          let openStart = 0;
          let openEnd = 0;

          if (linkDoc.content.childCount === 1 && linkDoc.content.firstChild.isBlock) {
            openStart = 1;
            openEnd = 1;
          }

          const slice = new Slice(linkDoc.content, openStart, openEnd);
          const tr = view.state.tr.replaceSelection(slice);
          view.dispatch(tr);
          view.focus();
        } catch (error) {
          console.error('Failed to upload file:', error);
          const errorText = `❌ ${t.fileUpload?.uploadFailed || 'Upload failed'}: ${file.name}`;
          const tr = view.state.tr.insertText(errorText);
          view.dispatch(tr);
        }
      };

      input.click();
    }
  };
}