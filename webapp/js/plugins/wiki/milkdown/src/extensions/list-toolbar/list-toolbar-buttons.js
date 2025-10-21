import { commandsCtx, editorViewCtx } from '@milkdown/core';
import { wrapInBulletListCommand, wrapInOrderedListCommand, bulletListSchema, orderedListSchema, listItemSchema } from '@milkdown/preset-commonmark';
import { liftListItem, wrapInList } from '@milkdown/prose/schema-list';

export const BULLET_LIST_ICON = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><g clip-path="url(#clip0_977_8070)"><path d="M4 10.5C3.17 10.5 2.5 11.17 2.5 12C2.5 12.83 3.17 13.5 4 13.5C4.83 13.5 5.5 12.83 5.5 12C5.5 11.17 4.83 10.5 4 10.5ZM4 4.5C3.17 4.5 2.5 5.17 2.5 6C2.5 6.83 3.17 7.5 4 7.5C4.83 7.5 5.5 6.83 5.5 6C5.5 5.17 4.83 4.5 4 4.5ZM4 16.5C3.17 16.5 2.5 17.18 2.5 18C2.5 18.82 3.18 19.5 4 19.5C4.82 19.5 5.5 18.82 5.5 18C5.5 17.18 4.83 16.5 4 16.5ZM8 19H20C20.55 19 21 18.55 21 18C21 17.45 20.55 17 20 17H8C7.45 17 7 17.45 7 18C7 18.55 7.45 19 8 19ZM8 13H20C20.55 13 21 12.55 21 12C21 11.45 20.55 11 20 11H8C7.45 11 7 11.45 7 12C7 12.55 7.45 13 8 13ZM7 6C7 6.55 7.45 7 8 7H20C20.55 7 21 6.55 21 6C21 5.45 20.55 5 20 5H8C7.45 5 7 5.45 7 6Z"/></g><defs><clipPath id="clip0_977_8070"><rect width="24" height="24"/></clipPath></defs></svg>`;

export const ORDERED_LIST_ICON = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><g clip-path="url(#clip0_977_8067)"><path d="M8 7H20C20.55 7 21 6.55 21 6C21 5.45 20.55 5 20 5H8C7.45 5 7 5.45 7 6C7 6.55 7.45 7 8 7ZM20 17H8C7.45 17 7 17.45 7 18C7 18.55 7.45 19 8 19H20C20.55 19 21 18.55 21 18C21 17.45 20.55 17 20 17ZM20 11H8C7.45 11 7 11.45 7 12C7 12.55 7.45 13 8 13H20C20.55 13 21 12.55 21 12C21 11.45 20.55 11 20 11ZM4.5 16H2.5C2.22 16 2 16.22 2 16.5C2 16.78 2.22 17 2.5 17H4V17.5H3.5C3.22 17.5 3 17.72 3 18C3 18.28 3.22 18.5 3.5 18.5H4V19H2.5C2.22 19 2 19.22 2 19.5C2 19.78 2.22 20 2.5 20H4.5C4.78 20 5 19.78 5 19.5V16.5C5 16.22 4.78 16 4.5 16ZM2.5 5H3V7.5C3 7.78 3.22 8 3.5 8C3.78 8 4 7.78 4 7.5V4.5C4 4.22 3.78 4 3.5 4H2.5C2.22 4 2 4.22 2 4.5C2 4.78 2.22 5 2.5 5ZM4.5 10H2.5C2.22 10 2 10.22 2 10.5C2 10.78 2.22 11 2.5 11H3.8L2.12 12.96C2.04 13.05 2 13.17 2 13.28V13.5C2 13.78 2.22 14 2.5 14H4.5C4.78 14 5 13.78 5 13.5C5 13.22 4.78 13 4.5 13H3.2L4.88 11.04C4.96 10.95 5 10.83 5 10.72V10.5C5 10.22 4.78 10 4.5 10Z"/></g><defs><clipPath id="clip0_977_8067"><rect width="24" height="24"/></clipPath></defs></svg>`;

function getListInfo(ctx) {
  const view = ctx.get(editorViewCtx);
  const { state } = view;
  const { $from } = state.selection;
  const bulletListType = bulletListSchema.type(ctx);
  const orderedListType = orderedListSchema.type(ctx);

  for (let d = $from.depth; d > 0; d--) {
    const node = $from.node(d);
    if (node.type === bulletListType) {
      return { inList: true, listType: 'bullet', depth: d };
    }
    if (node.type === orderedListType) {
      return { inList: true, listType: 'ordered', depth: d };
    }
  }
  return { inList: false, listType: null, depth: -1 };
}

function isInBlockquote(ctx) {
  const view = ctx.get(editorViewCtx);
  const { state } = view;
  const { $from } = state.selection;

  for (let d = $from.depth; d > 0; d--) {
    if ($from.node(d).type === state.schema.nodes.blockquote) {
      return true;
    }
  }
  return false;
}

function convertListType(ctx, targetListType) {
  const view = ctx.get(editorViewCtx);
  const listItemType = listItemSchema.type(ctx);
  const bulletListType = bulletListSchema.type(ctx);
  const orderedListType = orderedListSchema.type(ctx);
  const targetType = targetListType === 'bullet' ? bulletListType : orderedListType;

  const liftCmd = liftListItem(listItemType);

  let lifted = liftCmd(view.state, (tr) => view.dispatch(tr));

  let attempts = 0;
  while (lifted && attempts < 10) {
    const info = getListInfo(ctx);
    if (!info.inList) break;
    lifted = liftCmd(view.state, (tr) => view.dispatch(tr));
    attempts++;
  }

  const wrapCmd = wrapInList(targetType);
  wrapCmd(view.state, (tr) => view.dispatch(tr));

  return true;
}

function liftFromList(ctx) {
  const view = ctx.get(editorViewCtx);
  const { state, dispatch } = view;
  const listItemType = listItemSchema.type(ctx);

  const command = liftListItem(listItemType);
  let success = command(state, dispatch);

  if (success) {
    let currentState = view.state;
    let attempts = 0;
    while (attempts < 10) {
      const info = getListInfoFromState(currentState, ctx);
      if (!info.inList) break;

      const cmd = liftListItem(listItemType);
      if (!cmd(currentState, (tr) => {
        view.dispatch(tr);
        currentState = view.state;
      })) {
        break;
      }
      attempts++;
    }
  }

  return success;
}

function getListInfoFromState(state, ctx) {
  const { $from } = state.selection;
  const bulletListType = bulletListSchema.type(ctx);
  const orderedListType = orderedListSchema.type(ctx);

  for (let d = $from.depth; d > 0; d--) {
    const node = $from.node(d);
    if (node.type === bulletListType) {
      return { inList: true, listType: 'bullet', depth: d };
    }
    if (node.type === orderedListType) {
      return { inList: true, listType: 'ordered', depth: d };
    }
  }
  return { inList: false, listType: null, depth: -1 };
}

export function createListToolbarButtons(translations = {}) {
  const t = translations.toolbar || {};

  return {
    buildToolbar: (builder) => {
      const listGroup = builder.addGroup('list', t.listGroup || 'Lists');

      listGroup.addItem('bullet-list', {
        icon: BULLET_LIST_ICON,
        active: (ctx) => {
          try {
            const info = getListInfo(ctx);
            return info.inList && info.listType === 'bullet';
          } catch {
            return false;
          }
        },
        onRun: (ctx) => {
          if (isInBlockquote(ctx)) return;

          const info = getListInfo(ctx);
          const view = ctx.get(editorViewCtx);
          const bulletListType = bulletListSchema.type(ctx);

          if (info.inList) {
            if (info.listType === 'bullet') {
              liftFromList(ctx);
            } else {
              convertListType(ctx, 'bullet');
            }
          } else {
            const wrapCmd = wrapInList(bulletListType);
            wrapCmd(view.state, (tr) => view.dispatch(tr));
          }
        },
      });

      listGroup.addItem('ordered-list', {
        icon: ORDERED_LIST_ICON,
        active: (ctx) => {
          try {
            const info = getListInfo(ctx);
            return info.inList && info.listType === 'ordered';
          } catch {
            return false;
          }
        },
        onRun: (ctx) => {
          if (isInBlockquote(ctx)) return;

          const info = getListInfo(ctx);
          const view = ctx.get(editorViewCtx);
          const orderedListType = orderedListSchema.type(ctx);

          if (info.inList) {
            if (info.listType === 'ordered') {
              liftFromList(ctx);
            } else {
              convertListType(ctx, 'ordered');
            }
          } else {
            const wrapCmd = wrapInList(orderedListType);
            wrapCmd(view.state, (tr) => view.dispatch(tr));
          }
        },
      });
    }
  };
}
