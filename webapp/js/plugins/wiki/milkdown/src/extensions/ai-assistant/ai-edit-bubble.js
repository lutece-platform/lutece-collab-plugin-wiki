import { editorViewCtx, parserCtx, schemaCtx } from '@milkdown/core';
import { getMarkdown } from '@milkdown/utils';
import { DOMSerializer, Slice } from '@milkdown/kit/prose/model';
import { TextSelection } from '@milkdown/kit/prose/state';
import { streamAi, unwrapFence } from './ai-stream.js';
import { renderMermaidPreview } from '../mermaid/mermaid-render-preview.js';
import './ai-edit-bubble.css';

const VISIBLE_CHIPS = 4;
const REFUSAL = 'CANNOT:';
const ADDITION = 'ADD:';
const SEND = '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 5l0 14"/><path d="M18 11l-6 -6"/><path d="M6 11l6 -6"/></svg>';

let current = null;

/**
 * Creates an element.
 * @param {string} tag the tag
 * @param {string} className the classes
 * @param {string} [html] the content
 * @returns {HTMLElement} the element
 */
function element(tag, className, html) {
  const node = document.createElement(tag);
  node.className = className;
  if (html !== undefined) {
    node.innerHTML = html;
  }
  return node;
}

/**
 * Creates a button.
 * @param {string} className the classes
 * @param {string} label the text
 * @param {Function} onClick the action
 * @returns {HTMLButtonElement} the button
 */
function button(className, label, onClick) {
  const node = element('button', className);
  node.type = 'button';
  node.textContent = label;
  node.addEventListener('click', onClick);
  return node;
}

/**
 * Splits an answer into its kind and its content: an addition to place next to the selection, a refusal, or a new
 * version of the selection.
 * @param {string} answer the answer of the model
 * @returns {{kind: string, content: string}} the kind (addition, refusal or edit) and the content
 */
function readAnswer(answer) {
  const text = unwrapFence(answer);
  if (text.startsWith(REFUSAL)) return { kind: 'refusal', content: text.slice(REFUSAL.length).trim() };
  if (text.startsWith(ADDITION)) return { kind: 'addition', content: unwrapFence(text.slice(ADDITION.length)) };
  return { kind: 'edit', content: text };
}

/**
 * Returns the actions offered on an answer, the first one being the main one.
 * @param {string} type the type of the feature, or undefined for an instruction
 * @returns {Array<string>} the actions
 */
function actionsFor(type) {
  if (type === 'insertAfter') return ['insertAfter', 'replace'];
  if (type === 'insertBefore') return ['insertBefore', 'replace'];
  return ['replace', 'insertAfter'];
}

/**
 * Puts the answer in the document, in place of the selection or next to it.
 * @param {Object} view the ProseMirror view
 * @param {Function} parser the Markdown parser
 * @param {string} markdown the answer
 * @param {{from: number, to: number}} range the selection
 * @param {string} action replace, insertAfter or insertBefore
 */
function applyAnswer(view, parser, markdown, range, action) {
  const doc = parser(markdown);
  const single = doc.content.childCount === 1 && doc.content.firstChild.isTextblock;
  const tr = view.state.tr;
  let from = range.from;
  let to = range.to;
  if (action === 'insertAfter') {
    const $to = tr.doc.resolve(range.to);
    from = to = $to.after(Math.max($to.depth, 1));
  } else if (action === 'insertBefore') {
    const $from = tr.doc.resolve(range.from);
    from = to = $from.before(Math.max($from.depth, 1));
  }
  const open = action === 'replace' && single ? 1 : 0;
  tr.replace(from, to, new Slice(doc.content, open, open));
  const end = tr.mapping.map(to, 1);
  tr.setSelection(TextSelection.near(tr.doc.resolve(end), -1)).scrollIntoView();
  view.dispatch(tr);
}

/**
 * The bubble that lets the user ask the AI to change the selection, previews the answer and applies it once approved.
 */
class AiEditBubble {
  /**
   * @param {Object} ctx the Milkdown context
   * @param {Object} t the translations
   * @param {Array<Object>} features the AI features shown as suggestions
   * @param {Object} tracker reads and sets the tracked selection
   */
  constructor(ctx, t, features, tracker) {
    this.ctx = ctx;
    this.view = ctx.get(editorViewCtx);
    this.t = t.aiAssistant;
    this.features = features;
    this.tracker = tracker;
    this.steps = [];
    this.answer = null;
    this.stream = null;

    const { from, to } = this.view.state.selection;
    this.text = getMarkdown({ from, to })(ctx);
    this.tracker.set(from, to, 'selected');

    this.build();
    this.place();
    this.input.focus();
  }

  /**
   * Builds the bubble.
   */
  build() {
    this.root = element('div', 'ai-bubble');
    this.root.setAttribute('role', 'dialog');
    this.root.setAttribute('aria-label', this.t.dialogLabel);

    this.result = element('div', 'ai-bubble-result');
    this.result.hidden = true;
    this.preview = element('div', 'ai-bubble-preview');
    this.preview.setAttribute('aria-live', 'polite');
    this.actions = element('div', 'ai-bubble-actions');
    this.result.append(this.preview, this.actions);

    this.form = element('form', 'ai-bubble-prompt');
    this.input = element('input', 'ai-bubble-input');
    this.input.type = 'text';
    this.input.placeholder = this.t.placeholder;
    this.input.maxLength = 1000;
    this.input.setAttribute('aria-label', this.t.placeholder);
    this.send = element('button', 'ai-bubble-send', SEND);
    this.send.type = 'submit';
    this.send.setAttribute('aria-label', this.t.send);
    const tag = element('span', 'ai-bubble-tag');
    tag.textContent = `@${this.t.buttonLabel}`;
    this.form.append(tag, this.input, this.send);
    this.form.addEventListener('submit', (event) => {
      event.preventDefault();
      const instruction = this.input.value.trim();
      if (instruction) {
        this.run({ instruction });
      }
    });

    this.chips = element('div', 'ai-bubble-chips');
    this.renderChips(false);

    this.status = element('div', 'ai-bubble-status');
    this.status.setAttribute('role', 'status');

    this.root.append(this.result, this.form, this.chips, this.status);
    this.loadLimit();
    this.root.addEventListener('keydown', (event) => this.onKeyDown(event));
    this.root.addEventListener('mousedown', (event) => event.stopPropagation());

    this.host = this.view.dom.closest('.milkdown') || this.view.dom.parentElement;
    this.host.appendChild(this.root);

    this.onOutside = (event) => {
      if (!this.root.contains(event.target) && this.answer === null && !this.stream) {
        this.close();
      }
    };
    this.onMove = () => this.place();
    document.addEventListener('mousedown', this.onOutside);
    window.addEventListener('resize', this.onMove);
    window.addEventListener('scroll', this.onMove, true);
  }

  /**
   * Shows the features as suggestions, the first ones only unless asked.
   * @param {boolean} all true to show every feature
   */
  renderChips(all) {
    this.chips.replaceChildren();
    const shown = all ? this.features : this.features.slice(0, VISIBLE_CHIPS);
    shown.forEach((feature) => {
      this.chips.append(button('ai-bubble-chip', feature.label, () => this.run({ feature })));
    });
    if (!all && this.features.length > VISIBLE_CHIPS) {
      this.chips.append(button('ai-bubble-chip ai-bubble-more', this.t.more, () => this.renderChips(true)));
    }
  }

  /**
   * Reads how many requests the user has left today: warns when few are left, blocks when none is.
   */
  async loadLimit() {
    const response = await fetch('rest/wiki/ai/features/ratelimit').catch(() => null);
    const limit = response?.ok ? await response.json().catch(() => null) : null;
    if (!limit || limit.remaining === undefined || !this.root.isConnected) return;
    if (limit.remaining <= 0) {
      this.setStatus(this.t.limitReached, true);
      this.form.querySelectorAll('input, button').forEach((node) => { node.disabled = true; });
      this.chips.querySelectorAll('button').forEach((node) => { node.disabled = true; });
    } else if (limit.remaining < limit.limit * 0.2) {
      this.setStatus(this.t.remaining.replace('{n}', limit.remaining));
    }
  }

  /**
   * Places the bubble under the selection, inside the editor.
   */
  place() {
    const range = this.tracker.get();
    if (!range) return;
    const hostBox = this.host.getBoundingClientRect();
    const dom = this.view.dom;
    const style = getComputedStyle(dom);
    const domBox = dom.getBoundingClientRect();
    const textLeft = domBox.left + parseFloat(style.paddingLeft) - hostBox.left;
    const textWidth = domBox.width - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight);
    const width = Math.min(Math.max(textWidth, 320), hostBox.width - 16);
    const left = Math.min(Math.max(textLeft, 8), hostBox.width - width - 8);
    this.root.style.width = `${width}px`;
    this.root.style.left = `${left}px`;
    this.root.style.top = `${this.view.coordsAtPos(range.to).bottom - hostBox.top + 8}px`;
  }

  /**
   * Runs a request on the selection, or on the last answer when there is one.
   * @param {{feature?: Object, instruction?: string}} request the feature or the instruction
   */
  run(request) {
    if (this.answer !== null) {
      this.steps[this.steps.length - 1].result = this.answer;
    }
    this.steps.push({ feature: request.feature, instruction: request.instruction, result: null });
    this.request();
  }

  /**
   * Sends the steps to the server and shows the answer as it comes.
   */
  async request() {
    this.stopStream();
    const last = this.steps[this.steps.length - 1];
    this.answer = null;
    this.input.value = '';
    this.chips.hidden = true;
    this.setStatus('');
    this.result.hidden = false;
    this.preview.classList.add('is-writing');
    this.preview.textContent = this.t.writing;
    this.renderActions();
    this.tracker.set(null, null, 'processing');
    this.place();

    const body = {
      text: this.text,
      steps: this.steps.map((step) => ({
        feature_id: step.feature?.id,
        instruction: step.instruction,
        result: step.result ?? undefined,
      })),
    };
    let latest = null;
    const stream = streamAi('rest/wiki/ai/features/stream/edit', body, (answer) => {
      if (latest === null) {
        requestAnimationFrame(() => {
          if (this.stream === stream) {
            this.preview.textContent = readAnswer(latest).content;
          }
          latest = null;
        });
      }
      latest = answer;
    }, this.t);
    this.stream = stream;

    try {
      const answer = (await stream.done).trim();
      if (this.stream !== stream) return;
      if (!answer) {
        throw new Error(this.t.emptyAnswer);
      }
      this.stream = null;
      this.tracker.set(null, null, 'selected');
      this.answer = answer;
      this.showAnswer();
      this.renderActions(last.feature?.type);
      this.input.placeholder = this.t.refinePlaceholder;
      this.input.focus();
    } catch (error) {
      if (this.stream !== stream) return;
      this.stream = null;
      this.tracker.set(null, null, 'selected');
      this.restore(this.steps.pop());
      this.setStatus(error.message || this.t.error, true);
    }
  }

  /**
   * Renders the answer as it will look in the page, with its Mermaid diagrams drawn, or the reason why the model declined.
   */
  showAnswer() {
    const { kind, content } = readAnswer(this.answer);
    this.preview.classList.remove('is-writing');
    this.preview.classList.toggle('is-notice', kind === 'refusal');
    if (kind === 'refusal') {
      this.preview.textContent = content;
    } else {
      const doc = this.ctx.get(parserCtx)(content);
      const serializer = DOMSerializer.fromSchema(this.ctx.get(schemaCtx));
      this.preview.replaceChildren(serializer.serializeFragment(doc.content));
      this.preview.querySelectorAll('pre[data-language="mermaid"]').forEach((pre) => {
        renderMermaidPreview(pre.textContent, (diagram) => {
          pre.replaceWith(diagram);
          this.place();
        });
      });
    }
    this.place();
  }

  /**
   * Shows the actions on the answer, or the stop button while it is written.
   * @param {string} [type] the feature type of the answer
   */
  renderActions(type) {
    this.actions.replaceChildren();
    if (this.answer === null) {
      this.actions.append(button('ai-bubble-action', this.t.stop, () => this.stopWriting()));
      return;
    }
    const { kind } = readAnswer(this.answer);
    const actions = kind === 'refusal' ? [] : actionsFor(kind === 'addition' && type === undefined ? 'insertAfter' : type);
    actions.forEach((action, index) => {
      const label = this.t[action];
      const className = index === 0 ? 'ai-bubble-action is-primary' : 'ai-bubble-action';
      this.actions.append(button(className, label, () => this.apply(action)));
    });
    this.actions.append(
      button('ai-bubble-action', this.t.retry, () => this.request()),
      button('ai-bubble-action is-quiet', this.t.discard, () => this.close()),
    );
  }

  /**
   * Stops writing the answer and goes back to the request.
   */
  stopWriting() {
    this.stopStream();
    this.tracker.set(null, null, 'selected');
    this.restore(this.steps.pop());
  }

  /**
   * Goes back to the state before a request that was stopped or failed: the previous answer, or the suggestions.
   * @param {Object} [step] the request, given back in the input
   */
  restore(step) {
    const previous = this.steps[this.steps.length - 1];
    this.preview.classList.remove('is-writing');
    this.answer = previous ? previous.result : null;
    this.result.hidden = !previous;
    this.chips.hidden = Boolean(previous);
    if (previous) {
      previous.result = null;
      this.showAnswer();
      this.renderActions(previous.feature?.type);
    }
    this.input.value = step?.instruction || '';
    this.input.focus();
  }

  /**
   * Applies the answer to the document and closes the bubble.
   * @param {string} action replace, insertAfter or insertBefore
   */
  apply(action) {
    const range = this.tracker.get();
    if (!range || this.answer === null) return;
    const { kind, content } = readAnswer(this.answer);
    if (kind === 'refusal') return;
    applyAnswer(this.view, this.ctx.get(parserCtx), content, range, action);
    this.close();
  }

  /**
   * Handles the keyboard: Escape closes, Ctrl+Enter applies the main action.
   * @param {KeyboardEvent} event the event
   */
  onKeyDown(event) {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.close();
    } else if (event.key === 'Enter' && (event.ctrlKey || event.metaKey) && this.answer !== null) {
      event.preventDefault();
      this.actions.querySelector('.is-primary')?.click();
    }
  }

  /**
   * Shows a message under the bubble.
   * @param {string} message the message, empty to hide it
   * @param {boolean} [isError] true for an error
   */
  setStatus(message, isError = false) {
    this.status.textContent = message;
    this.status.classList.toggle('is-error', isError);
  }

  /**
   * Stops listening to the current answer.
   */
  stopStream() {
    this.stream?.stop();
    this.stream = null;
  }

  /**
   * Closes the bubble and gives the focus back to the editor.
   */
  close() {
    this.stopStream();
    document.removeEventListener('mousedown', this.onOutside);
    window.removeEventListener('resize', this.onMove);
    window.removeEventListener('scroll', this.onMove, true);
    this.root.remove();
    this.tracker.clear();
    if (current === this) {
      current = null;
    }
    this.view.focus();
  }
}

/**
 * Opens the AI bubble on the current selection.
 * @param {Object} ctx the Milkdown context
 * @param {Object} t the translations
 * @param {Array<Object>} features the AI features shown as suggestions
 * @param {Object} tracker reads and sets the tracked selection
 * @returns {boolean} true when the bubble was opened
 */
export function openAiBubble(ctx, t, features, tracker) {
  const { selection } = ctx.get(editorViewCtx).state;
  if (selection.empty) {
    return false;
  }
  current?.close();
  current = new AiEditBubble(ctx, t, features, tracker);
  return true;
}
