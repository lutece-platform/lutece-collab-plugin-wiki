import { createDropdownToolbarItem } from '../toolbar-dropdown/toolbar-dropdown-extension.js';
import { editorViewCtx } from '@milkdown/core';
import { setBlockType } from '@milkdown/prose/commands';

export const HEADING_STYLE_ICON = `<svg class="heading-style-icon" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12h8"></path><path d="M4 18V6"></path><path d="M12 18V6"></path><path d="m17 12 3-3 3 3"></path><path d="M20 9v12"></path></svg>`;

/**
 * Creates a heading style toolbar button with dropdown
 * @param {Object} translations - Translation object with heading labels
 * @returns {Object} Toolbar plugin configuration
 */
export function createHeadingStyleToolbarButton(translations = {}) {
  const t = translations.toolbar || {};

  const createHeadingOption = (level, label, icon) => ({
    key: level === 0 ? 'paragraph' : `heading${level}`,
    label,
    icon,
    onRun: (ctx) => {
      const view = ctx.get(editorViewCtx);
      const { state, dispatch } = view;
      const { schema } = state;

      if (level === 0) {
        setBlockType(schema.nodes.paragraph)(state, dispatch);
      } else {
        setBlockType(schema.nodes.heading, { level })(state, dispatch);
      }
    }
  });

  const headingOptions = [
    createHeadingOption(0, t.paragraph || 'Paragraph', '¶'),
    createHeadingOption(1, t.heading1 || 'Heading 1', 'H1'),
    createHeadingOption(2, t.heading2 || 'Heading 2', 'H2'),
    createHeadingOption(3, t.heading3 || 'Heading 3', 'H3'),
    createHeadingOption(4, t.heading4 || 'Heading 4', 'H4'),
    createHeadingOption(5, t.heading5 || 'Heading 5', 'H5'),
    createHeadingOption(6, t.heading6 || 'Heading 6', 'H6')
  ];

  /**
   * Checks if the current selection is inside a blockquote (GitHub alert).
   * Disables heading style changes inside alerts to prevent breaking the alert structure.
   */
  const isInBlockquote = (ctx) => {
    const view = ctx.get(editorViewCtx);
    const { state } = view;
    const { $from } = state.selection;

    for (let d = $from.depth; d > 0; d--) {
      if ($from.node(d).type === state.schema.nodes.blockquote) {
        return true;
      }
    }
    return false;
  };

  /**
   * Checks if the current selection is inside a heading node.
   */
  const isInHeading = (ctx) => {
    try {
      const view = ctx.get(editorViewCtx);
      const { state } = view;
      const { $from } = state.selection;

      for (let d = $from.depth; d >= 0; d--) {
        const node = $from.node(d);
        if (node.type === state.schema.nodes.heading) {
          return true;
        }
      }
      return false;
    } catch {
      return false;
    }
  };

  /**
   * Gets the current heading level (0 for paragraph, 1-6 for headings)
   */
  const getCurrentHeadingLevel = (ctx) => {
    try {
      const view = ctx.get(editorViewCtx);
      const { state } = view;
      const { $from } = state.selection;

      for (let d = $from.depth; d >= 0; d--) {
        const node = $from.node(d);
        if (node.type === state.schema.nodes.heading) {
          return node.attrs.level;
        }
        if (node.type === state.schema.nodes.paragraph) {
          return 0;
        }
      }
      return 0;
    } catch {
      return 0;
    }
  };

  /**
   * Checks if a dropdown item matches the current heading level
   */
  const isItemActive = (option, ctx) => {
    const currentLevel = getCurrentHeadingLevel(ctx);
    if (option.key === 'paragraph') {
      return currentLevel === 0;
    }
    const match = option.key.match(/^heading(\d)$/);
    if (match) {
      return parseInt(match[1], 10) === currentLevel;
    }
    return false;
  };

  return {
    buildToolbar: (builder) => {
      const headingGroup = builder.addGroup('heading-style', t.heading || 'Heading Style');
      headingGroup.addItem('heading-style-dropdown', createDropdownToolbarItem(HEADING_STYLE_ICON, headingOptions, isInBlockquote, null, null, 'heading-style-icon', isInHeading, isItemActive));
    }
  };
}
