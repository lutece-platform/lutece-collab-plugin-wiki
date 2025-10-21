import { $prose } from '@milkdown/utils';
import { Plugin } from '@milkdown/prose/state';
import { Decoration, DecorationSet } from '@milkdown/prose/view';

const ALERT_TYPES = {
  NOTE: { class: 'alert-info' },
  TIP: { class: 'alert-success' },
  IMPORTANT: { class: 'alert-info' },
  WARNING: { class: 'alert-warning' },
  CAUTION: { class: 'alert-danger' }
};

export const githubAlertsEditablePlugin = $prose(() => {
  return new Plugin({
    props: {
      decorations(state) {
        const decorations = [];

        state.doc.descendants((node, pos) => {
          if (node.type.name === 'blockquote') {
            const firstChild = node.firstChild;
            if (firstChild && firstChild.type.name === 'paragraph') {
              const text = firstChild.textContent;
              const match = text && text.match(/^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]/);

              if (match) {
                const alertType = match[1];
                const config = ALERT_TYPES[alertType];

                decorations.push(
                  Decoration.node(pos, pos + node.nodeSize, {
                    class: `alert ${config.class}`,
                    'data-alert-type': alertType
                  })
                );

                const markerStart = pos + 2;
                const markerEnd = markerStart + match[0].length;

                decorations.push(
                  Decoration.inline(markerStart, markerEnd, {
                    class: 'github-alert-marker'
                  })
                );
              }
            }
          }
        });

        return DecorationSet.create(state.doc, decorations);
      }
    }
  });
});