import { editorViewCtx } from '@milkdown/core';
import { $prose } from '@milkdown/utils';
import { Decoration, DecorationSet } from '@milkdown/kit/prose/view';
import { Plugin, PluginKey } from '@milkdown/kit/prose/state';
import { openAiBubble } from './ai-edit-bubble.js';
import './ai-processing.css';

const aiSelectionKey = new PluginKey('ai-selection');
const ICON = '<span class="ai-toolbar-text"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 18a2 2 0 0 1 2 2a2 2 0 0 1 2 -2a2 2 0 0 1 -2 -2a2 2 0 0 1 -2 2zm0 -12a2 2 0 0 1 2 2a2 2 0 0 1 2 -2a2 2 0 0 1 -2 -2a2 2 0 0 1 -2 2zm-7 12a6 6 0 0 1 6 -6a6 6 0 0 1 -6 -6a6 6 0 0 1 -6 6a6 6 0 0 1 6 6z"/></svg>%s</span>';

/**
 * Returns the object that reads and marks the selection the AI works on.
 * @param {Object} view the ProseMirror view
 * @returns {{get: Function, set: Function, clear: Function}} the tracker
 */
function trackerFor(view) {
  return {
    get: () => {
      const { from, to } = aiSelectionKey.getState(view.state);
      return from === null ? null : { from, to };
    },
    set: (from, to, mode) => view.dispatch(view.state.tr.setMeta(aiSelectionKey, { from, to, mode })),
    clear: () => view.dispatch(view.state.tr.setMeta(aiSelectionKey, { clear: true })),
  };
}

/**
 * Creates the toolbar button that opens the AI bubble on the selection.
 * @param {Object} t the translations
 * @param {Array<Object>} features the AI features shown as suggestions
 * @returns {Object} the toolbar configuration
 */
export function createAiAssistantToolbarButton(t, features = []) {
  return {
    buildToolbar: (builder) => {
      builder.addGroup('ai-assistant', t.aiAssistant.buttonLabel).addItem('ai-assistant', {
        icon: ICON.replace('%s', t.aiAssistant.buttonLabel),
        active: () => false,
        onRun: (ctx) => {
          const view = ctx.get(editorViewCtx);
          openAiBubble(ctx, t, features, trackerFor(view));
        },
      });
    },
  };
}

/**
 * Creates the plugin that keeps the selection the AI works on, marks it while the bubble is open, and opens the bubble
 * with Ctrl+J (Cmd+J on Mac).
 * @param {Object} t the translations
 * @param {Array<Object>} features the AI features shown as suggestions
 * @returns {Object} the Milkdown plugin
 */
export function createAiAssistantPlugin(t, features = []) {
  return $prose((ctx) => new Plugin({
    key: aiSelectionKey,
    state: {
      init: () => ({ from: null, to: null, mode: null }),
      apply(tr, value) {
        const meta = tr.getMeta(aiSelectionKey);
        if (meta?.clear) {
          return { from: null, to: null, mode: null };
        }
        if (meta) {
          return {
            from: meta.from ?? value.from,
            to: meta.to ?? value.to,
            mode: meta.mode,
          };
        }
        if (value.from !== null && tr.docChanged) {
          return { ...value, from: tr.mapping.map(value.from, 1), to: tr.mapping.map(value.to, -1) };
        }
        return value;
      },
    },
    props: {
      decorations(state) {
        const { from, to, mode } = aiSelectionKey.getState(state);
        if (from === null || from >= to) {
          return null;
        }
        const className = mode === 'processing' ? 'ai-processing' : 'ai-selected';
        return DecorationSet.create(state.doc, [Decoration.inline(from, to, { class: className })]);
      },
      handleKeyDown(view, event) {
        if ((event.ctrlKey || event.metaKey) && !event.altKey && !event.shiftKey && event.key.toLowerCase() === 'j') {
          event.preventDefault();
          return openAiBubble(ctx, t, features, trackerFor(view));
        }
        return false;
      },
    },
  }));
}
