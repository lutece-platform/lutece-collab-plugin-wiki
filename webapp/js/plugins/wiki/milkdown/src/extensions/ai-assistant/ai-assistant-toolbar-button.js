import { createDropdownToolbarItem } from '../toolbar-dropdown/toolbar-dropdown-extension.js';
import { editorViewCtx, serializerCtx, parserCtx, schemaCtx } from '@milkdown/core';
import { $prose, getMarkdown } from '@milkdown/utils';
import { Decoration, DecorationSet } from '@milkdown/kit/prose/view';
import { Plugin, PluginKey } from '@milkdown/kit/prose/state';
import { Slice } from '@milkdown/kit/prose/model';
import './ai-processing.css';

let rateLimitState = {
  remaining: null,
  limit: null
};

async function fetchRateLimitStatus() {
  try {
    const response = await fetch('rest/wiki/ai/features/ratelimit', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      const data = await response.json();
      rateLimitState.remaining = data.remaining || data._nRemaining;
      rateLimitState.limit = data.limit || data._nLimit;
    }
  } catch (error) {
    console.error('Failed to fetch rate limit status:', error);
  }
}

function getHeaderConfig(t) {
  if (rateLimitState.remaining === null || rateLimitState.limit === null) {
    return null;
  }

  let counterClass = '';
  if (rateLimitState.remaining === 0) {
    counterClass = 'depleted';
  } else if (rateLimitState.remaining < rateLimitState.limit * 0.2) {
    counterClass = 'low';
  }

  return {
    text: t.aiAssistant?.remainingLabel || 'Remaining',
    counter: rateLimitState.remaining,
    counterClass
  };
}

function isItemDisabled() {
  return rateLimitState.remaining !== null && rateLimitState.remaining === 0;
}

export function createAiAssistantToolbarButton(t, aiConfig = []) {
  if (!aiConfig || aiConfig.length === 0) {
    return {};
  }

  const buttonText = t.aiAssistant?.buttonLabel || 'Ask AI';

  const createAiOption = (aiItem) => ({
    key: aiItem.label.toLowerCase().replace(/\s+/g, '-'),
    label: aiItem.label,
    onRun: (ctx) => handleAiAction(ctx, aiItem, t)
  });

  const aiOptions = aiConfig.map(item => createAiOption(item));

  if (aiConfig.length > 0) {
    fetchRateLimitStatus();
  }

  return {
    buildToolbar: (builder) => {
      const aiGroup = builder.addGroup('ai-assistant', buttonText);

      const buttonHTML = `<span class="ai-toolbar-text">${buttonText}</span>`;

      const getHeaderConfigFn = () => getHeaderConfig(t);

      aiGroup.addItem(
        'ai-assistant-dropdown',
        createDropdownToolbarItem(
          buttonHTML,
          aiOptions,
          null,
          getHeaderConfigFn,
          isItemDisabled,
          'ai-toolbar-text'
        )
      );
    }
  };
}

const aiProcessingKey = new PluginKey('ai-processing');

function addAiProcessingDecoration(view, from, to) {
  const tr = view.state.tr;
  tr.setMeta(aiProcessingKey, { from, to, action: 'add' });
  view.dispatch(tr);
}

function removeAiProcessingDecoration(view) {
  const tr = view.state.tr;
  tr.setMeta(aiProcessingKey, { action: 'remove' });
  view.dispatch(tr);
}

export const aiProcessingPlugin = $prose(() => {
  return new Plugin({
    key: aiProcessingKey,
    state: {
      init() {
        return { from: null, to: null };
      },
      apply(tr, value) {
        const meta = tr.getMeta(aiProcessingKey);
        if (meta) {
          if (meta.action === 'add') {
            return { from: meta.from, to: meta.to };
          } else if (meta.action === 'remove') {
            return { from: null, to: null };
          }
        }
        if (value.from !== null && tr.docChanged) {
          return {
            from: tr.mapping.map(value.from),
            to: tr.mapping.map(value.to)
          };
        }
        return value;
      }
    },
    props: {
      decorations(state) {
        const { from, to } = this.getState(state);
        if (from === null || to === null) {
          return null;
        }
        const decoration = Decoration.inline(from, to, {
          class: 'ai-processing'
        });
        return DecorationSet.create(state.doc, [decoration]);
      }
    }
  });
});

async function handleAiAction(ctx, aiItem, t) {
  const view = ctx.get(editorViewCtx);
  const parser = ctx.get(parserCtx);
  const { state } = view;
  const { selection } = state;
  const { from, to } = selection;

  const selectedMarkdown = getMarkdown({ from, to })(ctx);

  if (!selectedMarkdown || selectedMarkdown.trim().length === 0) {
    return;
  }

  addAiProcessingDecoration(view, from, to);

  let eventSource = null;

  try {
    const initResponse = await fetch('rest/wiki/ai/features/stream/init', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ feature_id: aiItem.id, text: selectedMarkdown }),
    });

    if (!initResponse.ok) {
      const errorData = await initResponse.json().catch(() => ({ error: `HTTP error! status: ${initResponse.status}` }));
      const errorMessage = errorData.error || `HTTP error! status: ${initResponse.status}`;
      throw { status: initResponse.status, message: errorMessage, isServerError: true };
    }

    const { stream_id } = await initResponse.json();

    const accumulatedResponse = await new Promise((resolve, reject) => {
      let response = '';
      eventSource = new EventSource(`rest/wiki/ai/features/stream/events/${stream_id}`);

      eventSource.addEventListener('token', (event) => {
        const data = JSON.parse(event.data);
        response += data.token;
      });

      eventSource.addEventListener('completed', () => {
        eventSource.close();
        resolve(response);
      });

      eventSource.addEventListener('error', (event) => {
        eventSource.close();
        try {
          const data = JSON.parse(event.data);
          reject(new Error(data.error || 'Stream error'));
        } catch {
          reject(new Error('Stream connection error'));
        }
      });

      eventSource.onerror = () => {
        eventSource.close();
        reject(new Error('Stream connection lost'));
      };
    });

    removeAiProcessingDecoration(view);

    const responseDoc = parser(accumulatedResponse);

    if (!responseDoc) {
      throw new Error('Failed to parse AI response as markdown');
    }

    const tr = view.state.tr;

    let openStart = 0;
    let openEnd = 0;

    if (responseDoc.content.childCount === 1) {
      const firstChild = responseDoc.content.firstChild;

      if (firstChild.isBlock && aiItem.type === 'replace') {
        openStart = 1;
        openEnd = 1;
      }
    }

    const slice = new Slice(responseDoc.content, openStart, openEnd);

    switch (aiItem.type) {
      case 'replace':
        tr.replace(from, to, slice);
        break;

      case 'insertBefore':
        const $fromPos = tr.doc.resolve(from);
        const blockStartBefore = $fromPos.before($fromPos.depth);
        tr.replace(blockStartBefore, blockStartBefore, slice);
        break;

      case 'insertAfter':
        const $toPos = tr.doc.resolve(to);
        const blockEndAfter = $toPos.after($toPos.depth);
        tr.replace(blockEndAfter, blockEndAfter, slice);
        break;

      default:
        tr.replace(from, to, slice);
    }

    view.dispatch(tr);
    view.focus();

    if (rateLimitState.remaining !== null && rateLimitState.remaining > 0) {
      rateLimitState.remaining--;
    }

  } catch (error) {
    console.error('AI action failed:', error);

    if (eventSource) eventSource.close();
    removeAiProcessingDecoration(view);

    let errorTitle = 'Erreur IA';
    let errorMessage = 'Une erreur est survenue lors du traitement de votre demande.';

    if (error.isServerError && error.message) {
      errorMessage = error.message;

      if (error.status === 429) {
        errorTitle = 'Limite atteinte';
      } else if (error.status === 401 || error.status === 403) {
        errorTitle = 'Accès non autorisé';
      }
    } else if (error.message) {
      errorMessage = error.message;
    }

    alert(`${errorTitle}\n\n${errorMessage}`);
  }
}
