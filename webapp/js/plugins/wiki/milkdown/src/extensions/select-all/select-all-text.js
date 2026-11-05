import { $prose } from '@milkdown/utils';
import { AllSelection, Plugin, PluginKey, Selection, TextSelection } from '@milkdown/kit/prose/state';

/**
 * Turns a select-all (Ctrl+A) into a text selection of the whole page, so the selection toolbar shows. Only when the
 * page starts and ends with text: the text selection then covers exactly the same content.
 */
export const selectAllTextPlugin = $prose(() => new Plugin({
  key: new PluginKey('select-all-text'),
  appendTransaction(transactions, oldState, newState) {
    const { doc, selection } = newState;
    if (!(selection instanceof AllSelection)) {
      return null;
    }
    const start = Selection.atStart(doc);
    const end = Selection.atEnd(doc);
    if (!(start instanceof TextSelection) || !(end instanceof TextSelection) || start.from >= end.to) {
      return null;
    }
    return newState.tr.setSelection(TextSelection.create(doc, start.from, end.to));
  },
}));
