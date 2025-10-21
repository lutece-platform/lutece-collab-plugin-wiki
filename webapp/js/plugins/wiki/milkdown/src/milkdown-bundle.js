import { Crepe, CrepeBuilder } from '@milkdown/crepe';
import { LanguageDescription, StreamLanguage } from '@codemirror/language';
import { languages as languageData } from '@codemirror/language-data';
import { oneDark } from '@codemirror/theme-one-dark';
import { basicSetup } from 'codemirror';
import { keymap } from '@codemirror/view';
import { defaultKeymap, indentWithTab } from '@codemirror/commands';
import { properties } from '@codemirror/legacy-modes/mode/properties';
import { commandsCtx, editorViewCtx, parserCtx } from '@milkdown/core';
import { clearTextInCurrentBlockCommand, wrapInBlockTypeCommand, blockquoteSchema, imageSchema } from '@milkdown/preset-commonmark';
import { translations, getTranslation } from './translations.js';
import { githubAlertsEditablePlugin } from './extensions/github-alerts/github-alerts-editable.js';
import { createHeadingStyleToolbarButton } from './extensions/heading-style-dropdown/heading-style-toolbar-button.js';
import { createAiAssistantToolbarButton, aiProcessingPlugin } from './extensions/ai-assistant/ai-assistant-toolbar-button.js';
import { createFindReplaceToolbarButton, findReplacePlugin } from './extensions/find-replace/find-replace-toolbar-button.js';
import { renderMermaidPreview } from './extensions/mermaid/mermaid-render-preview.js';
import { createFileUploadMenuItem } from './extensions/file-upload/file-upload.js';
import { videoPlugin, createVideoUploadMenuItem } from './extensions/video-player/video-player.js';
import { createListToolbarButtons } from './extensions/list-toolbar/list-toolbar-buttons.js';
import { createPasteSanitizerPlugin } from './extensions/paste-sanitizer/paste-sanitizer.js';

import './milkdown-themes.css';
import './extensions/paste-sanitizer/paste-sanitizer.css';
import './extensions/toolbar-dropdown/toolbar-dropdown.css';
import './properties-syntax.css';
import './extensions/github-alerts/github-alerts.css';
import './extensions/ai-assistant/ai-assistant.css';
import './extensions/mermaid/mermaid.css';
import './extensions/video-player/video-player.css';
import './upload-overlay.css';

const propertiesLanguage = LanguageDescription.of({
  name: 'Properties',
  alias: ['properties', 'props', 'ini', 'conf', 'config'],
  extensions: ['properties', 'props', 'ini', 'conf', 'config'],
  load() {
    return Promise.resolve(StreamLanguage.define(properties));
  },
});

const mermaidLanguage = LanguageDescription.of({
  name: 'Mermaid',
  alias: ['mermaid'],
  extensions: ['mmd', 'mermaid'],
  load() {
    return Promise.resolve(StreamLanguage.define({
      startState: () => ({}),
      token: (stream) => {
        stream.skipToEnd();
        return 'mermaid';
      }
    }));
  },
});

const languages = [...languageData, propertiesLanguage, mermaidLanguage];

function createConfig(t = getTranslation('en'), uploadHandlerRef = null, aiConfig = []) {
  return {
    features: {
      [Crepe.Feature.Cursor]: true,
      [Crepe.Feature.ListItem]: true,
      [Crepe.Feature.LinkTooltip]: true,
      [Crepe.Feature.ImageBlock]: true,
      [Crepe.Feature.BlockEdit]: true,
      [Crepe.Feature.Placeholder]: true,
      [Crepe.Feature.Toolbar]: true,
      [Crepe.Feature.CodeMirror]: true,
      [Crepe.Feature.Table]: true,
      [Crepe.Feature.Latex]: false,
    },
    featureConfigs: {
      [Crepe.Feature.Cursor]: {
        color: '#4c9ffe',
        width: 2,
      },
      [Crepe.Feature.Placeholder]: {
        text: t.placeholder.text,
        mode: 'block',
      },
      [Crepe.Feature.CodeMirror]: {
        extensions: [
          keymap.of(defaultKeymap.concat(indentWithTab)),
          basicSetup,
          oneDark,
        ],
        languages: languages,
        theme: oneDark,
        searchPlaceholder: t.codeMirror.searchPlaceholder,
        noResultText: t.codeMirror.noResultText,
        previewOnlyByDefault: (language) => {
          return language?.toLowerCase() === 'mermaid';
        },
        renderPreview: (language, code, applyPreview) => {
          if (language?.toLowerCase() === 'mermaid') {
            renderMermaidPreview(code, applyPreview);
            return true;
          }
          return false;
        },
      },
      [Crepe.Feature.ImageBlock]: {
        inlineUploadPlaceholderText: t.imageBlock.inlineUploadPlaceholder,
        blockUploadPlaceholderText: t.imageBlock.blockUploadPlaceholder,
        blockCaptionPlaceholderText: t.imageBlock.blockCaptionPlaceholder,
        blockUploadButton: t.imageBlock.uploadButton,
        onUpload: async (file) => {
          return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (e) => resolve(e.target.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
          });
        },
      },
      [Crepe.Feature.LinkTooltip]: {
        inputPlaceholder: t.linkTooltip.inputPlaceholder,
      },
      [Crepe.Feature.Toolbar]: (() => {
        const headingConfig = createHeadingStyleToolbarButton(t);
        const listConfig = createListToolbarButtons(t);
        const findReplaceConfig = createFindReplaceToolbarButton(t);
        const aiConfigObj = aiConfig.length > 0 ? createAiAssistantToolbarButton(t, aiConfig) : null;

        return {
          buildToolbar: (builder) => {
            if (headingConfig.buildToolbar) {
              headingConfig.buildToolbar(builder);
            }
            if (listConfig.buildToolbar) {
              listConfig.buildToolbar(builder);
            }
            if (findReplaceConfig.buildToolbar) {
              findReplaceConfig.buildToolbar(builder);
            }
            if (aiConfigObj?.buildToolbar) {
              aiConfigObj.buildToolbar(builder);
            }
          }
        };
      })(),
      [Crepe.Feature.BlockEdit]: {
        blockHandle: {
          getPlacement: () => 'left-start',
        },
        textGroup: {
          label: t.blockEdit.textGroup,
          text: { label: t.blockEdit.text },
          h1: { label: t.blockEdit.h1 },
          h2: { label: t.blockEdit.h2 },
          h3: { label: t.blockEdit.h3 },
          h4: { label: t.blockEdit.h4 },
          h5: { label: t.blockEdit.h5 },
          h6: { label: t.blockEdit.h6 },
          quote: { label: t.blockEdit.quote },
          divider: { label: t.blockEdit.divider },
        },
        listGroup: {
          label: t.blockEdit.listGroup,
          bulletList: { label: t.blockEdit.bulletList },
          orderedList: { label: t.blockEdit.orderedList },
          taskList: { label: t.blockEdit.taskList },
        },
        advancedGroup: {
          label: t.blockEdit.advancedGroup,
          image: null,
          codeBlock: { label: t.blockEdit.codeBlock },
          table: { label: t.blockEdit.table },
        },
        buildMenu: (builder) => {
          const alertsGroup = builder.addGroup('alerts', 'Alerts');

          alertsGroup.addItem('note-alert', {
            label: `${t.githubAlerts?.note || 'Note'}`,
            icon: 'ℹ️',
            onRun: (ctx) => {
              const commands = ctx.get(commandsCtx);
              const blockquote = blockquoteSchema.type(ctx);
              commands.call(clearTextInCurrentBlockCommand.key);
              commands.call(wrapInBlockTypeCommand.key, {
                nodeType: blockquote
              });
              const view = ctx.get(editorViewCtx);
              const { tr } = view.state;
              const { from } = view.state.selection;
              tr.insertText('[!NOTE] ');
              view.dispatch(tr);
            }
          });

          alertsGroup.addItem('tip-alert', {
            label: `${t.githubAlerts?.tip || 'Tip'}`,
            icon: '💡',
            onRun: (ctx) => {
              const commands = ctx.get(commandsCtx);
              const blockquote = blockquoteSchema.type(ctx);
              commands.call(clearTextInCurrentBlockCommand.key);
              commands.call(wrapInBlockTypeCommand.key, {
                nodeType: blockquote
              });
              const view = ctx.get(editorViewCtx);
              const { tr } = view.state;
              const { from } = view.state.selection;
              tr.insertText('[!TIP] ');
              view.dispatch(tr);
            }
          });

          alertsGroup.addItem('warning-alert', {
            label: `${t.githubAlerts?.warning || 'Warning'}`,
            icon: '⚠️',
            onRun: (ctx) => {
              const commands = ctx.get(commandsCtx);
              const blockquote = blockquoteSchema.type(ctx);
              commands.call(clearTextInCurrentBlockCommand.key);
              commands.call(wrapInBlockTypeCommand.key, {
                nodeType: blockquote
              });
              const view = ctx.get(editorViewCtx);
              const { tr } = view.state;
              const { from } = view.state.selection;
              tr.insertText('[!WARNING] ');
              view.dispatch(tr);
            }
          });

          alertsGroup.addItem('caution-alert', {
            label: `${t.githubAlerts?.caution || 'Caution'}`,
            icon: '🛑',
            onRun: (ctx) => {
              const commands = ctx.get(commandsCtx);
              const blockquote = blockquoteSchema.type(ctx);
              commands.call(clearTextInCurrentBlockCommand.key);
              commands.call(wrapInBlockTypeCommand.key, {
                nodeType: blockquote
              });
              const view = ctx.get(editorViewCtx);
              const { tr } = view.state;
              const { from } = view.state.selection;
              tr.insertText('[!CAUTION] ');
              view.dispatch(tr);
            }
          });

          const advancedGroup = builder.getGroup('advanced');
          if (advancedGroup) {
            advancedGroup.addItem('image-upload', {
              label: t.blockEdit.image,
              icon: '🖼️',
              onRun: async (ctx) => {
                const commands = ctx.get(commandsCtx);
                commands.call(clearTextInCurrentBlockCommand.key);

                const view = ctx.get(editorViewCtx);
                const { schema } = view.state;

                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'image/*';

                input.onchange = async (e) => {
                  const file = e.target.files?.[0];
                  if (!file) return;

                  try {
                    const url = await (uploadHandlerRef?.handler || (async (file) => {
                      return new Promise((resolve, reject) => {
                        const reader = new FileReader();
                        reader.onload = (e) => resolve(e.target.result);
                        reader.onerror = reject;
                        reader.readAsDataURL(file);
                      });
                    }))(file);

                    const imageNode = schema.nodes.image.create({
                      src: url,
                      alt: file.name
                    });

                    const tr = view.state.tr.replaceSelectionWith(imageNode);
                    view.dispatch(tr);
                    view.focus();
                  } catch (error) {
                    console.error('Failed to upload image:', error);
                  }
                };

                input.click();
              }
            });

            const fileUploadItem = createFileUploadMenuItem(uploadHandlerRef, t);
            advancedGroup.addItem('file-upload', fileUploadItem);

            const videoUploadItem = createVideoUploadMenuItem(uploadHandlerRef, t);
            advancedGroup.addItem('video-upload', videoUploadItem);
          }
        },
      },
    },
  };
}

class MilkdownBundle {
  constructor() {
    this.editors = new Map();
  }

  /**
   * Create a full-featured Milkdown editor with Crepe
   * @param {Object} options - Configuration options
   * @param {string|HTMLElement} options.root - The root element or selector
   * @param {string} options.defaultValue - Initial markdown content
   * @param {string} options.theme - Theme name: 'classic', 'nord', 'frame', 'classic-dark', 'nord-dark', 'frame-dark'
   * @param {string} options.lang - Language: 'en' or 'fr' (default: 'en')
   * @param {Object} options.features - Feature configuration (optional)
   * @param {Object} options.featureConfigs - Feature-specific configurations (optional)
   * @param {Function} options.onUpload - Custom upload handler for images (optional)
   * @param {Array} options.aiConfig - AI assistant configuration array (optional)
   * @param {boolean} options.readonly - Whether to create the editor in readonly mode (optional)
   * @returns {Object} Editor instance with methods
   */
  createEditor(options = {}) {
    const {
      root = '#editor',
      defaultValue = '# Welcome to Milkdown!\n\nStart writing in **Markdown**.',
      theme = 'classic',
      lang = 'en',
      features = {},
      featureConfigs = {},
      onUpload = null,
      aiConfig = [],
      readonly = false,
    } = options;
    
    const t = getTranslation(lang);


    const rootElement = typeof root === 'string' ? document.querySelector(root) : root;
    if (!rootElement) {
      throw new Error(`Root element not found: ${root}`);
    }

    const themeMap = {
      'classic': 'crepe',
      'classic-dark': 'crepe-dark'
    };
    const cssTheme = themeMap[theme] || theme;
    rootElement.className = `milkdown-theme-${cssTheme}`;

    const uploadHandlerRef = {
      handler: onUpload || (async (file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = (e) => resolve(e.target.result);
          reader.onerror = reject;
          reader.readAsDataURL(file);
        });
      })
    };

    const translatedConfig = createConfig(t, uploadHandlerRef, aiConfig);

    const config = {
      root: rootElement,
      defaultValue,
      features: { ...translatedConfig.features, ...features },
      featureConfigs: { ...translatedConfig.featureConfigs },
    };

    Object.keys(featureConfigs).forEach(key => {
      config.featureConfigs[key] = {
        ...config.featureConfigs[key],
        ...featureConfigs[key],
      };
    });

    if (onUpload) {
      config.featureConfigs[Crepe.Feature.ImageBlock].onUpload = onUpload;
      uploadHandlerRef.handler = onUpload;
    }

    const editor = new Crepe(config);

    editor.editor.use(githubAlertsEditablePlugin);
    editor.editor.use(findReplacePlugin);
    editor.editor.use(createPasteSanitizerPlugin(t));

    if (aiConfig && aiConfig.length > 0) {
      editor.editor.use(aiProcessingPlugin);
    }

    editor.editor.use(videoPlugin);

    editor.create().then(() => {
      if (readonly) {
        editor.setReadonly(true);
      }
    }).catch(err => {
      console.error('Failed to initialize editor:', err);
    });

    setTimeout(() => {
      rootElement.addEventListener('click', (e) => {
        let target = e.target;

        while (target && target !== rootElement) {
          if (target.tagName === 'BUTTON') {
            if (target.getAttribute('type') !== 'submit') {
              e.preventDefault();
            }
            break;
          }
          target = target.parentElement;
        }
      }, true);
    }, 100);

    const editorId = Date.now().toString();
    const editorData = { 
      editor, 
      config: options,
      rootElement,
      theme: cssTheme
    };
    this.editors.set(editorId, editorData);

    const wrapper = {
      id: editorId,
      getEditor: () => {
        const data = this.editors.get(editorId);
        return data ? data.editor : null;
      },
      getMarkdown: () => {
        const data = this.editors.get(editorId);
        return data && data.editor ? data.editor.getMarkdown() : '';
      },
      setMarkdown: (markdown) => {
        const data = this.editors.get(editorId);
        if (!data || !data.editor) return;

        try {
          data.editor.editor.action((ctx) => {
            const view = ctx.get(editorViewCtx);
            const parser = ctx.get(parserCtx);

            const doc = parser(markdown);
            if (!doc) return;

            const { state } = view;
            const tr = state.tr.replaceWith(0, state.doc.content.size, doc.content);
            view.dispatch(tr);
          });
        } catch (error) {
          console.error('Error setting markdown:', error);
        }
      },
      setReadonly: (readonly) => {
        const data = this.editors.get(editorId);
        if (data && data.editor) {
          data.editor.setReadonly(readonly);
        }
      },
      destroy: () => {
        const data = this.editors.get(editorId);
        if (data) {
          if (data.editor && data.editor.destroy) {
            data.editor.destroy();
          }
          this.editors.delete(editorId);
        }
      },
      on: (callback) => {
        const data = this.editors.get(editorId);
        if (data && data.editor) {
          data.editor.on(callback);
        }
      },
    };

    return wrapper;
  }

  /**
   * Get all active editors
   * @returns {Map} Map of editor instances
   */
  getEditors() {
    return this.editors;
  }

  /**
   * Destroy all editors
   */
  destroyAll() {
    this.editors.forEach(editor => editor.destroy());
    this.editors.clear();
  }
}

const milkdownBundle = new MilkdownBundle();

milkdownBundle.translations = translations;
milkdownBundle.getTranslation = getTranslation;

if (typeof window !== 'undefined') {
  window.MilkdownBundle = milkdownBundle;
  window.Crepe = Crepe;
  window.CrepeBuilder = CrepeBuilder;
  window.MilkdownTranslations = translations;
}

export default milkdownBundle;
export { Crepe, CrepeBuilder, translations, getTranslation };