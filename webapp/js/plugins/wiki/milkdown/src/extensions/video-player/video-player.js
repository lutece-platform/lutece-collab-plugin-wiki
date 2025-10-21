import { $node, $command, $inputRule, $remark, $view } from '@milkdown/utils';
import { editorViewCtx, commandsCtx, parserCtx, schemaCtx } from '@milkdown/core';
import { clearTextInCurrentBlockCommand } from '@milkdown/preset-commonmark';
import { InputRule } from '@milkdown/kit/prose/inputrules';
import directive from 'remark-directive';
import { visit } from 'unist-util-visit';

const ALLOWED_DIRECTIVES = ['video'];

function filterUnknownDirectives() {
  return (tree) => {
    visit(tree, (node, index, parent) => {
      if (
        (node.type === 'textDirective' || node.type === 'leafDirective' || node.type === 'containerDirective') &&
        !ALLOWED_DIRECTIVES.includes(node.name)
      ) {
        const text = { type: 'text', value: `:${node.name}` };
        parent.children.splice(index, 1, text);
        return index;
      }
    });
  };
}

export const videoRemarkPlugin = $remark('videoRemark', () => directive);
export const videoDirectiveFilterPlugin = $remark('videoDirectiveFilter', () => filterUnknownDirectives);

export const videoNode = $node('video', () => ({
  content: '',
  group: 'block',
  selectable: true,
  draggable: true,
  atom: true,
  isolating: true,
  defining: true,
  marks: '',
  attrs: {
    src: { default: '' },
    ratio: { default: 1 }
  },
  parseDOM: [
    {
      tag: 'div[data-video-container]',
      getAttrs: (dom) => ({
        src: dom.getAttribute('data-src') || '',
        ratio: parseFloat(dom.getAttribute('data-ratio')) || 1
      })
    }
  ],
  toDOM: (node) => {
    const container = document.createElement('div');
    container.setAttribute('data-video-container', '');
    container.setAttribute('data-src', node.attrs.src);
    container.setAttribute('data-ratio', node.attrs.ratio);
    container.className = 'milkdown-video-container';

    const video = document.createElement('video');
    video.src = node.attrs.src;
    video.controls = true;
    video.className = 'milkdown-video';
    video.style.maxWidth = '100%';
    video.style.height = 'auto';

    container.appendChild(video);
    return container;
  },
  parseMarkdown: {
    match: (node) => {
      if (node.type === 'leafDirective' && node.name === 'video') {
        return true;
      }
      if (node.type === 'html' && node.value?.includes('data-video-container')) {
        return true;
      }
      return false;
    },
    runner: (state, node, type) => {
      if (node.type === 'leafDirective') {
        state.addNode(type, {
          src: node.attributes?.src || '',
          ratio: node.attributes?.ratio ? parseFloat(node.attributes.ratio) : 1
        });
      } else if (node.type === 'html') {
        const value = node.value || '';
        const srcMatch = value.match(/data-src="([^"]+)"/);
        const ratioMatch = value.match(/data-ratio="([^"]+)"/);
        if (srcMatch) {
          state.addNode(type, {
            src: srcMatch[1],
            ratio: ratioMatch ? parseFloat(ratioMatch[1]) : 1
          });
        }
      }
    }
  },
  toMarkdown: {
    match: (node) => node.type.name === 'video',
    runner: (state, node) => {
      const attributes = {
        src: node.attrs.src
      };
      if (node.attrs.ratio && node.attrs.ratio !== 1) {
        attributes.ratio = node.attrs.ratio.toString();
      }

      state.addNode('leafDirective', undefined, undefined, {
        name: 'video',
        attributes
      });
    }
  }
}));

export const insertVideoCommand = $command(
  'insertVideo',
  (ctx) => (payload) => {
    if (!payload || !payload.src) {
      return false;
    }

    return (state, dispatch) => {
      if (!dispatch) {
        return false;
      }
      const node = videoNode.type(ctx).create({
        src: payload.src
      });
      if (!node) return true;

      dispatch(state.tr.replaceSelectionWith(node).scrollIntoView());
      return true;
    };
  }
);

export const videoInputRule = $inputRule((ctx) =>
  new InputRule(
    /::video\{src="([^"]+)"\s*\}/,
    (state, match, start, end) => {
      const [matched, src = ''] = match;
      const { tr } = state;
      if (matched) {
        const node = videoNode.type(ctx).create({ src });
        if (node) {
          return tr.replaceWith(start - 1, end, node);
        }
      }
      return null;
    }
  )
);

export function createVideoUploadMenuItem(uploadHandlerRef, t) {
  return {
    label: t.blockEdit?.video || 'Video',
    icon: '🎬',
    onRun: async (ctx) => {
      const commands = ctx.get(commandsCtx);
      commands.call(clearTextInCurrentBlockCommand.key);

      const view = ctx.get(editorViewCtx);
      const schema = ctx.get(schemaCtx);

      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'video/*';
      input.multiple = false;

      input.onchange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        try {
          const url = await uploadHandlerRef.handler(file);

          const videoNodeType = schema.nodes.video;
          if (videoNodeType) {
            const node = videoNodeType.create({
              src: url
            });
            const tr = view.state.tr.replaceSelectionWith(node);
            view.dispatch(tr);
            view.focus();
          } else {
            const videoDirective = `::video{src="${url}"}`;
            const tr = view.state.tr.insertText(videoDirective);
            view.dispatch(tr);
          }
        } catch (error) {
          console.error('Failed to upload video:', error);
          const errorText = `❌ ${t.fileUpload?.uploadFailed || 'Upload failed'}: ${file.name}`;
          const tr = view.state.tr.insertText(errorText);
          view.dispatch(tr);
        }
      };

      input.click();
    }
  };
}

export const videoNodeView = $view(videoNode, () => (node, view, getPos) => {
    const container = document.createElement('div');
    container.setAttribute('data-video-container', '');
    container.setAttribute('data-src', node.attrs.src);
    container.setAttribute('data-ratio', node.attrs.ratio);
    container.className = 'milkdown-video-container';
    container.contentEditable = 'false';

    const wrapper = document.createElement('div');
    wrapper.className = 'milkdown-video-wrapper';

    const video = document.createElement('video');
    video.src = node.attrs.src;
    video.controls = true;
    video.className = 'milkdown-video';
    video.style.maxWidth = '100%';

    const onVideoLoad = () => {
      const host = container.closest('.milkdown-video-container');
      if (!host) return;

      const maxWidth = host.getBoundingClientRect().width;
      if (!maxWidth) return;

      const height = video.videoHeight;
      const width = video.videoWidth;
      const transformedHeight = width < maxWidth ? height : maxWidth * (height / width);
      const h = (transformedHeight * (node.attrs.ratio ?? 1)).toFixed(2);
      video.dataset.origin = transformedHeight.toFixed(2);
      video.dataset.height = h;
      video.style.height = `${h}px`;
    };

    video.addEventListener('loadedmetadata', onVideoLoad);

    wrapper.appendChild(video);

    const resizeHandle = document.createElement('div');
    resizeHandle.className = 'milkdown-video-resize-handle';

    const onResizeHandlePointerMove = (e) => {
      e.preventDefault();
      const top = video.getBoundingClientRect().top;
      const height = e.clientY - top;
      const h = Number(height < 100 ? 100 : height).toFixed(2);
      video.dataset.height = h;
      video.style.height = `${h}px`;
    };

    const onResizeHandlePointerUp = () => {
      window.removeEventListener('pointermove', onResizeHandlePointerMove);
      window.removeEventListener('pointerup', onResizeHandlePointerUp);

      const originHeight = Number(video.dataset.origin);
      const currentHeight = Number(video.dataset.height);
      const ratio = Number.parseFloat(
        Number(currentHeight / originHeight).toFixed(2)
      );
      if (Number.isNaN(ratio)) return;

      const pos = getPos();
      if (typeof pos === 'number') {
        const tr = view.state.tr;
        const newAttrs = {
          ...node.attrs,
          ratio: ratio
        };
        tr.setNodeMarkup(pos, null, newAttrs);
        view.dispatch(tr);
      }
    };

    const onResizeHandlePointerDown = (e) => {
      e.preventDefault();
      e.stopPropagation();
      window.addEventListener('pointermove', onResizeHandlePointerMove);
      window.addEventListener('pointerup', onResizeHandlePointerUp);
    };

    resizeHandle.addEventListener('pointerdown', onResizeHandlePointerDown);

    wrapper.appendChild(resizeHandle);
    container.appendChild(wrapper);

    return {
      dom: container,
      update: (updatedNode) => {
        if (updatedNode.type !== node.type) {
          return false;
        }
        if (updatedNode.attrs.ratio !== node.attrs.ratio) {
          const originHeight = Number(video.dataset.origin);
          if (!isNaN(originHeight)) {
            const h = (originHeight * updatedNode.attrs.ratio).toFixed(2);
            video.dataset.height = h;
            video.style.height = `${h}px`;
          }
        }
        return true;
      },
      destroy: () => {
        video.removeEventListener('loadedmetadata', onVideoLoad);
        resizeHandle.removeEventListener('pointerdown', onResizeHandlePointerDown);
      }
    };
  }
);

export const videoPlugin = [
  videoRemarkPlugin,
  videoDirectiveFilterPlugin,
  videoNode,
  videoNodeView,
  insertVideoCommand,
  videoInputRule
].flat();