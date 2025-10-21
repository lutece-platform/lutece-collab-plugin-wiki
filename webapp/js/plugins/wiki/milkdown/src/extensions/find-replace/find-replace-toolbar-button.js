import { createCustomDropdownToolbarItem, toolbarDropdownManager } from '../toolbar-dropdown/toolbar-dropdown-extension.js';
import { editorViewCtx } from '@milkdown/core';
import { $prose } from '@milkdown/utils';
import { Decoration, DecorationSet } from '@milkdown/kit/prose/view';
import { Plugin, PluginKey } from '@milkdown/kit/prose/state';
import './find-replace.css';

const findReplaceKey = new PluginKey('find-replace');

let currentState = {
  searchTerm: '',
  caseSensitive: false,
  matches: [],
  currentIndex: -1
};

function findMatches(doc, searchTerm, caseSensitive) {
  if (!searchTerm) return [];

  const matches = [];
  const search = caseSensitive ? searchTerm : searchTerm.toLowerCase();

  doc.descendants((node, pos) => {
    if (node.isText) {
      const text = caseSensitive ? node.text : node.text.toLowerCase();
      let index = 0;

      while ((index = text.indexOf(search, index)) !== -1) {
        matches.push({
          from: pos + index,
          to: pos + index + searchTerm.length
        });
        index += 1;
      }
    }
  });

  return matches;
}

function updateDecorations(view) {
  const tr = view.state.tr;
  tr.setMeta(findReplaceKey, { action: 'update' });
  view.dispatch(tr);
}

function clearHighlights(view) {
  currentState = {
    searchTerm: '',
    caseSensitive: false,
    matches: [],
    currentIndex: -1
  };

  const tr = view.state.tr;
  tr.setMeta(findReplaceKey, { action: 'clear' });
  view.dispatch(tr);
}

export const findReplacePlugin = $prose(() => {
  return new Plugin({
    key: findReplaceKey,
    state: {
      init() {
        return DecorationSet.empty;
      },
      apply(tr, decorations, oldState, newState) {
        const meta = tr.getMeta(findReplaceKey);

        if (meta?.action === 'clear') {
          return DecorationSet.empty;
        }

        if (meta?.action === 'update' || tr.docChanged) {
          const { searchTerm, caseSensitive, currentIndex } = currentState;

          if (!searchTerm) {
            return DecorationSet.empty;
          }

          currentState.matches = findMatches(newState.doc, searchTerm, caseSensitive);

          const decorationsArray = currentState.matches.map((match, index) => {
            const className = index === currentIndex ? 'find-highlight find-highlight-current' : 'find-highlight';
            return Decoration.inline(match.from, match.to, { class: className });
          });

          return DecorationSet.create(newState.doc, decorationsArray);
        }

        return decorations.map(tr.mapping, tr.doc);
      }
    },
    props: {
      decorations(state) {
        return this.getState(state);
      }
    }
  });
});

function scrollToMatch(view, match) {
  if (!match) return;

  const { from, to } = match;
  const tr = view.state.tr.setSelection(
    view.state.selection.constructor.near(view.state.doc.resolve(from))
  );
  view.dispatch(tr);

  const dom = view.domAtPos(from);
  if (dom?.node) {
    const element = dom.node.nodeType === Node.TEXT_NODE ? dom.node.parentElement : dom.node;
    element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

function buildFindReplaceContent(ctx, btn, t) {
  const view = ctx.get(editorViewCtx);
  const labels = t?.findReplace || {};

  const { from, to } = view.state.selection;
  const selectedText = from !== to ? view.state.doc.textBetween(from, to, ' ') : '';

  if (selectedText) {
    currentState.searchTerm = selectedText;
  }

  const content = document.createElement('div');
  content.className = 'find-replace-content';

  const searchGroup = document.createElement('div');
  searchGroup.className = 'find-replace-group';

  const searchInput = document.createElement('input');
  searchInput.type = 'text';
  searchInput.className = 'find-replace-input';
  searchInput.placeholder = labels.searchPlaceholder || 'Rechercher...';
  searchInput.value = currentState.searchTerm;

  const counter = document.createElement('span');
  counter.className = 'find-replace-counter';
  counter.textContent = '0 / 0';

  searchGroup.appendChild(searchInput);
  searchGroup.appendChild(counter);

  const replaceInput = document.createElement('input');
  replaceInput.type = 'text';
  replaceInput.className = 'find-replace-input';
  replaceInput.placeholder = labels.replacePlaceholder || 'Remplacer par...';

  const optionsRow = document.createElement('div');
  optionsRow.className = 'find-replace-options';

  const caseLabel = document.createElement('label');
  caseLabel.className = 'find-replace-checkbox';

  const caseCheckbox = document.createElement('input');
  caseCheckbox.type = 'checkbox';
  caseCheckbox.checked = currentState.caseSensitive;

  caseLabel.appendChild(caseCheckbox);
  caseLabel.appendChild(document.createTextNode(` ${labels.caseSensitive || 'Respecter la casse'}`));
  optionsRow.appendChild(caseLabel);

  const actionsRow = document.createElement('div');
  actionsRow.className = 'find-replace-actions';

  const prevBtn = document.createElement('button');
  prevBtn.className = 'find-replace-btn find-replace-btn-nav';
  prevBtn.innerHTML = '&#9664;';
  prevBtn.title = labels.previous || 'Précédent';

  const nextBtn = document.createElement('button');
  nextBtn.className = 'find-replace-btn find-replace-btn-nav';
  nextBtn.innerHTML = '&#9654;';
  nextBtn.title = labels.next || 'Suivant';

  const replaceBtn = document.createElement('button');
  replaceBtn.className = 'find-replace-btn';
  replaceBtn.textContent = labels.replace || 'Remplacer';

  const replaceAllBtn = document.createElement('button');
  replaceAllBtn.className = 'find-replace-btn';
  replaceAllBtn.textContent = labels.replaceAll || 'Tout';

  actionsRow.appendChild(prevBtn);
  actionsRow.appendChild(nextBtn);
  actionsRow.appendChild(replaceBtn);
  actionsRow.appendChild(replaceAllBtn);

  content.appendChild(searchGroup);
  content.appendChild(replaceInput);
  content.appendChild(optionsRow);
  content.appendChild(actionsRow);

  const updateCounter = () => {
    const total = currentState.matches.length;
    const current = total > 0 ? currentState.currentIndex + 1 : 0;
    counter.textContent = `${current} / ${total}`;
  };

  const doSearch = () => {
    currentState.searchTerm = searchInput.value;
    currentState.caseSensitive = caseCheckbox.checked;
    currentState.matches = findMatches(view.state.doc, currentState.searchTerm, currentState.caseSensitive);
    currentState.currentIndex = currentState.matches.length > 0 ? 0 : -1;

    updateDecorations(view);
    updateCounter();

    if (currentState.matches.length > 0) {
      scrollToMatch(view, currentState.matches[0]);
    }
  };

  let searchTimeout;
  searchInput.addEventListener('input', () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(doSearch, 150);
  });

  caseCheckbox.addEventListener('change', doSearch);

  prevBtn.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (currentState.matches.length === 0) return;

    currentState.currentIndex--;
    if (currentState.currentIndex < 0) {
      currentState.currentIndex = currentState.matches.length - 1;
    }

    updateDecorations(view);
    updateCounter();
    scrollToMatch(view, currentState.matches[currentState.currentIndex]);
  });

  nextBtn.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (currentState.matches.length === 0) return;

    currentState.currentIndex++;
    if (currentState.currentIndex >= currentState.matches.length) {
      currentState.currentIndex = 0;
    }

    updateDecorations(view);
    updateCounter();
    scrollToMatch(view, currentState.matches[currentState.currentIndex]);
  });

  replaceBtn.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (currentState.currentIndex < 0 || currentState.matches.length === 0) return;

    const match = currentState.matches[currentState.currentIndex];
    const replaceText = replaceInput.value;

    const tr = view.state.tr.insertText(replaceText, match.from, match.to);
    view.dispatch(tr);

    doSearch();
  });

  replaceAllBtn.addEventListener('click', (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (currentState.matches.length === 0) return;

    const replaceText = replaceInput.value;
    let tr = view.state.tr;

    for (let i = currentState.matches.length - 1; i >= 0; i--) {
      const match = currentState.matches[i];
      tr = tr.insertText(replaceText, match.from, match.to);
    }

    view.dispatch(tr);
    clearHighlights(view);
    toolbarDropdownManager.closeAll();
  });

  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      if (e.shiftKey) {
        prevBtn.click();
      } else {
        nextBtn.click();
      }
    }
    if (e.key === 'Escape') {
      toolbarDropdownManager.closeAll();
    }
  });

  replaceInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      replaceBtn.click();
    }
    if (e.key === 'Escape') {
      toolbarDropdownManager.closeAll();
    }
  });

  setTimeout(() => searchInput.focus(), 50);

  if (currentState.searchTerm) {
    doSearch();
  }

  const onEditorFocus = () => {
    if (content.isConnected) {
      toolbarDropdownManager.closeAll();
    }
  };

  view.dom.addEventListener('focus', onEditorFocus, true);

  const observer = new MutationObserver(() => {
    if (!content.isConnected) {
      clearHighlights(view);
      view.dom.removeEventListener('focus', onEditorFocus, true);
      observer.disconnect();
    }
  });

  requestAnimationFrame(() => {
    observer.observe(document.body, { childList: true, subtree: true });
  });

  return content;
}

const SEARCH_ICON = `<span class="find-replace-icon"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></span>`;

export function createFindReplaceToolbarButton(t) {
  return {
    buildToolbar: (builder) => {
      const group = builder.addGroup('find-replace', 'Rechercher');

      group.addItem(
        'find-replace-dropdown',
        createCustomDropdownToolbarItem(SEARCH_ICON, (ctx, btn) => buildFindReplaceContent(ctx, btn, t), null, null, 'find-replace-icon')
      );
    }
  };
}

export function clearFindReplace(ctx) {
  const view = ctx.get(editorViewCtx);
  clearHighlights(view);
}
