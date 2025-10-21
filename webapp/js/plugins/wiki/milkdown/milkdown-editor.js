/**
 * Milkdown Editor - Editable markdown editor for Wiki pages
 */

window.WikiMilkdownEditor = {
    editors: new Map(),
    aiFeatures: null,

    showEditorOverlay: function(message = 'Traitement en cours...') {
        const container = document.getElementById('editor-container');
        if (!container) return;

        const existingOverlay = container.querySelector('.ai-editor-overlay');
        if (existingOverlay) return;

        container.dataset.originalHeight = container.style.height || '';
        container.dataset.originalOverflow = container.style.overflow || '';
        container.style.position = 'relative';
        container.style.height = '400px';
        container.style.overflow = 'hidden';

        const overlay = document.createElement('div');
        overlay.className = 'ai-editor-overlay';

        const content = document.createElement('div');
        content.className = 'ai-overlay-content';

        const spinner = document.createElement('div');
        spinner.className = 'ai-overlay-spinner';

        const messageDiv = document.createElement('div');
        messageDiv.className = 'ai-overlay-message';
        messageDiv.textContent = message;

        const counter = document.createElement('div');
        counter.className = 'ai-overlay-counter';
        counter.dataset.count = '0';
        counter.textContent = '0 chunks reçus';

        content.appendChild(spinner);
        content.appendChild(messageDiv);
        content.appendChild(counter);
        overlay.appendChild(content);
        container.appendChild(overlay);
    },

    updateOverlayCounter: function(count) {
        const counter = document.querySelector('.ai-overlay-counter');
        if (counter) {
            counter.dataset.count = count;
            counter.textContent = `${count} chunk${count > 1 ? 's' : ''} reçu${count > 1 ? 's' : ''}`;
        }
    },

    hideEditorOverlay: function() {
        const container = document.getElementById('editor-container');
        if (!container) return;

        container.style.height = container.dataset.originalHeight || '';
        container.style.overflow = container.dataset.originalOverflow || '';
        delete container.dataset.originalHeight;
        delete container.dataset.originalOverflow;

        const overlay = container.querySelector('.ai-editor-overlay');
        if (overlay) {
            overlay.remove();
        }
    },

    /**
     * Load AI features from REST API
     */
    loadAiFeatures: async function() {
        if (this.aiFeatures !== null) {
            return this.aiFeatures;
        }

        try {
            const response = await fetch('rest/wiki/ai/features');
            if (!response.ok) {
                console.error('Failed to load AI features:', response.status);
                this.aiFeatures = [];
                return [];
            }

            const features = await response.json();

            this.aiFeatures = features.map(feature => ({
                id: feature.id,
                label: feature.name,
                type: feature.type,
                displayMode: feature.displayMode || 'dropdown'
            }));

            return this.aiFeatures;
        } catch (error) {
            console.error('Error loading AI features:', error);
            this.aiFeatures = [];
            return [];
        }
    },

    /**
     * Initialize a Milkdown editor
     */
    init: async function(selector, textareaId, content = '', options = {}) {
        let aiConfig = options.aiConfig || [];
        if (aiConfig.length === 0) {
            aiConfig = await this.loadAiFeatures();
        }

        const dropdownFeatures = aiConfig.filter(f => f.displayMode !== 'button');
        const buttonFeatures = aiConfig.filter(f => f.displayMode === 'button');

        const defaultOptions = {
            theme: 'frame',
            lang: 'fr',
            onUpload: (file) => this.uploadFile(file, options.itemId),
            aiConfig: dropdownFeatures
        };

        const config = Object.assign({}, defaultOptions, options, {
            root: selector,
            defaultValue: content,
            aiConfig: dropdownFeatures
        });

        if (buttonFeatures.length > 0) {
            this.createAiButtonsBar(selector, buttonFeatures, textareaId);
        }

        const editor = window.MilkdownBundle.createEditor(config);

        this.editors.set(textareaId, {
            editor: editor,
            itemId: options.itemId,
            buttonFeatures: buttonFeatures
        });
        this.setupSync(editor, textareaId);
        this.setupPasteHandler(editor, textareaId);

        // Mark editor container as ready to hide the loader
        const container = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (container) {
            container.classList.add('wiki-editor-ready');
        }

        return editor;
    },

    /**
     * Create AI buttons bar above the editor
     */
    createAiButtonsBar: function(selector, buttonFeatures, textareaId) {
        const container = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (!container) return;

        const existingBar = container.parentElement?.querySelector('.ai-buttons-bar');
        if (existingBar) {
            existingBar.remove();
        }

        const bar = document.createElement('div');
        bar.className = 'ai-buttons-bar d-flex flex-wrap gap-2 mb-2 justify-content-end';

        buttonFeatures.forEach(feature => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn btn-primary btn-sm';
            button.innerHTML = `<i class="ti ti-sparkles me-1"></i>${feature.label}`;
            button.dataset.featureId = feature.id;
            button.dataset.featureUrl = feature.url;
            button.dataset.featureType = feature.type;
            button.dataset.textareaId = textareaId;

            button.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleAiButtonClick(feature, textareaId, button);
            });

            bar.appendChild(button);
        });

        container.parentElement.insertBefore(bar, container);
    },

    /**
     * Handle AI button click - uses full editor content
     */
    handleAiButtonClick: async function(feature, textareaId, buttonElement) {
        const editorData = this.editors.get(textareaId);
        if (!editorData || !editorData.editor) {
            console.error('Editor not found for textareaId:', textareaId);
            return;
        }

        const fullContent = editorData.editor.getMarkdown();

        if (!fullContent || fullContent.trim().length === 0) {
            alert('L\'éditeur est vide.');
            return;
        }

        await this.executeAiFeatureFullContent(feature, fullContent, editorData.editor, buttonElement);
    },

    /**
     * Execute AI feature on full content with streaming
     */
    executeAiFeatureFullContent: async function(feature, content, editor, buttonElement) {
        this.showEditorOverlay(feature.label);

        if (buttonElement) {
            const originalContent = buttonElement.innerHTML;
            buttonElement.disabled = true;
            buttonElement.innerHTML = `<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>${feature.label}`;
            buttonElement.dataset.originalContent = originalContent;
        }

        let eventSource = null;

        try {
            const initResponse = await fetch('rest/wiki/ai/features/stream/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ feature_id: feature.id, text: content }),
            });

            if (!initResponse.ok) {
                const errorData = await initResponse.json().catch(() => ({ error: `HTTP error! status: ${initResponse.status}` }));
                throw new Error(errorData.error || `HTTP error! status: ${initResponse.status}`);
            }

            const { stream_id } = await initResponse.json();
            let accumulatedResponse = '';
            let tokenCount = 0;

            await new Promise((resolve, reject) => {
                eventSource = new EventSource(`rest/wiki/ai/features/stream/events/${stream_id}`);

                eventSource.addEventListener('token', (event) => {
                    const data = JSON.parse(event.data);
                    accumulatedResponse += data.token;
                    tokenCount++;
                    this.updateOverlayCounter(tokenCount);
                });

                eventSource.addEventListener('completed', () => {
                    eventSource.close();
                    const currentContent = editor.getMarkdown();
                    switch (feature.type) {
                        case 'insertBefore':
                            editor.setMarkdown(accumulatedResponse + '\n\n' + currentContent);
                            break;
                        case 'insertAfter':
                            editor.setMarkdown(currentContent + '\n\n' + accumulatedResponse);
                            break;
                        case 'replace':
                        default:
                            editor.setMarkdown(accumulatedResponse);
                            break;
                    }
                    resolve();
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

        } catch (error) {
            console.error('AI feature failed:', error);
            alert(`Erreur IA\n\n${error.message}`);
        } finally {
            if (eventSource) eventSource.close();
            this.hideEditorOverlay();
            if (buttonElement) {
                buttonElement.disabled = false;
                buttonElement.innerHTML = buttonElement.dataset.originalContent || `<i class="ti ti-sparkles me-1"></i>${feature.label}`;
            }
        }
    },

    /**
     * Upload file to server
     */
    uploadFile: async function(file, itemId) {
        const results = await this.uploadMultipleFiles([file], itemId);
        if (results && results.length > 0 && results[0].fileUrl) {
            return results[0].fileUrl;
        }
        throw new Error('Upload failed');
    },

    /**
     * Upload multiple files to server
     */
    uploadMultipleFiles: async function(files, itemId) {
        try {
            const formData = new FormData();

            files.forEach(file => {
                formData.append('file', file);
            });

            formData.append('handler', 'wikiAsynchronousUploadHandler');

            if (itemId) {
                formData.append('itemId', itemId);
            }

            const response = await fetch('jsp/site/upload', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error('Upload failed');
            }

            const result = await response.json();

            if (result.success && result.uploadedFiles) {
                return result.uploadedFiles.map(file => ({
                    fileId: file.fileId,
                    fileUrl: file.fileUrl ? file.fileUrl.replace(/&#38;/g, '&') : null,
                    fileName: file.fileName
                }));
            } else {
                throw new Error(result.error || 'Upload failed');
            }
        } catch (error) {
            console.error('Upload error:', error);
            throw error;
        }
    },

    /**
     * Get editor data from storage
     */
    getEditorData: function(textareaId) {
        return this.editors.get(textareaId);
    },

    /**
     * Setup automatic synchronization between editor and textarea
     */
    setupSync: function(editor, textareaId) {
        const textarea = document.getElementById(textareaId);
        if (!textarea) return;

        setTimeout(() => {
            try {
                if (editor && editor.getMarkdown) {
                    textarea.value = editor.getMarkdown();
                }
            } catch (e) {
                console.warn('Failed to sync editor content:', e.message);
            }
        }, 500);

        setInterval(() => {
            try {
                if (editor && editor.getMarkdown) {
                    const markdown = editor.getMarkdown();
                    if (textarea.value !== markdown) {
                        textarea.value = markdown;
                    }
                }
            } catch (e) {
                console.warn('Failed to sync editor content:', e.message);
            }
        }, 1000);
    },

    /**
     * Setup paste handler to intercept images
     */
    setupPasteHandler: function(editor, textareaId) {
        const container = document.querySelector('#editor-container');
        if (!container) return;

        setTimeout(() => {
            const editorElement = container.querySelector('.milkdown');
            if (!editorElement) return;

            editorElement.addEventListener('paste', (e) => this.handlePaste(e, editor), true);
        }, 1000);
    },

    /**
     * Handle paste event for base64 images (from Outlook, direct paste, etc.)
     */
    handlePaste: async function(e, editor) {
        const items = e.clipboardData?.items;
        if (!items) return;

        const hasImage = Array.from(items).some(item => item.type.indexOf('image') !== -1);
        const hasText = Array.from(items).some(item => item.type.indexOf('text') !== -1);

        let shouldProcessBase64 = false;

        if (hasText) {
            const text = e.clipboardData.getData('text/plain');
            const html = e.clipboardData.getData('text/html');
            const hasBase64InText = text && text.includes('data:image') && text.includes('base64,');
            const hasBase64InHtml = html && html.includes('data:image') && html.includes('base64,');
            shouldProcessBase64 = hasBase64InText || hasBase64InHtml;
        }

        if (hasImage || shouldProcessBase64) {
            setTimeout(async () => {
                await this.processBase64InEditor(editor);
            }, 500);
        }
    },

    /**
     * Process base64 images in editor
     */
    processBase64InEditor: async function(editor) {
        const editorData = Array.from(this.editors.values()).find(data => data.editor === editor);
        const itemId = editorData ? editorData.itemId : null;

        const currentMarkdown = editor.getMarkdown();

        if (!currentMarkdown.includes('base64,')) {
            return;
        }

        // Note: Milkdown escapes : as \: in data URLs, so we need to match both
        const mdImageRegex = /!\[([^\]]*)\]\(data\\?:image\/([^;]+);base64,([\s\S]+?)\)/gm;
        const htmlImageRegex = /<img[^>]*src="data:image\/([^;]+);base64,([^"]+)"[^>]*>/gm;
        const mdMatches = [...currentMarkdown.matchAll(mdImageRegex)];
        const htmlMatches = [...currentMarkdown.matchAll(htmlImageRegex)];
        const totalMatches = mdMatches.length + htmlMatches.length;

        if (totalMatches === 0) return;

        setTimeout(() => {
            this.addImageOverlays(totalMatches);
        }, 50);

        const processedMarkdown = await this.processBase64Images(currentMarkdown, itemId);

        if (processedMarkdown !== currentMarkdown) {
            this.removeImageOverlays();
            editor.setMarkdown(processedMarkdown);
        }
    },


    /**
     * Add overlays to base64 images
     */
    addImageOverlays: function(totalImages) {
        const editorElement = document.querySelector('.milkdown');
        if (!editorElement) return;

        const images = editorElement.querySelectorAll('img[src^="data:image"]');
        images.forEach((img, index) => {
            if (!img.parentElement.classList.contains('wiki-image-uploading')) {
                const wrapper = document.createElement('div');
                wrapper.className = 'wiki-image-uploading';
                wrapper.style.position = 'relative';
                wrapper.style.display = 'inline-block';
                wrapper.dataset.imageIndex = index;

                const message = document.createElement('div');
                message.className = 'wiki-upload-message';
                message.textContent = `Téléchargement ${index + 1}/${totalImages}`;

                img.parentNode.insertBefore(wrapper, img);
                wrapper.appendChild(img);
                wrapper.appendChild(message);
            }
        });
    },

    /**
     * Remove image overlays
     */
    removeImageOverlays: function() {
        const wrappers = document.querySelectorAll('.wiki-image-uploading');
        wrappers.forEach(wrapper => {
            const img = wrapper.querySelector('img');
            if (img && wrapper.parentNode) {
                wrapper.parentNode.insertBefore(img, wrapper);
                wrapper.remove();
            }
        });
    },

    /**
     * Process markdown text to replace base64 images with uploaded URLs
     */
    processBase64Images: async function(text, itemId) {
        // Note: Milkdown escapes : as \: in data URLs, so we need to match both
        const mdImageRegex = /!\[([^\]]*)\]\(data\\?:image\/([^;]+);base64,([\s\S]+?)\)/gm;
        const htmlImageRegex = /<img[^>]*src="data:image\/([^;]+);base64,([^"]+)"[^>]*>/gm;

        let processedText = text;
        const files = [];
        const matchesInfo = [];

        // Process markdown images ![...](data:...)
        for (const match of text.matchAll(mdImageRegex)) {
            const [fullMatch, altText, imageType, base64Data] = match;
            try {
                const cleanBase64 = base64Data.replace(/\s/g, '');
                const blob = this.base64ToBlob(cleanBase64, `image/${imageType}`);
                const fileName = this.generateFileName(altText, imageType);
                const file = new File([blob], fileName, { type: `image/${imageType}` });
                files.push(file);
                matchesInfo.push({ fullMatch, altText, fileName, isHtml: false });
            } catch (error) {
                console.error('Failed to process base64 markdown image:', error);
            }
        }

        // Process HTML images <img src="data:...">
        for (const match of text.matchAll(htmlImageRegex)) {
            const [fullMatch, imageType, base64Data] = match;
            const altMatch = fullMatch.match(/alt="([^"]*)"/);
            const altText = altMatch ? altMatch[1] : '';
            try {
                const cleanBase64 = base64Data.replace(/\s/g, '');
                const blob = this.base64ToBlob(cleanBase64, `image/${imageType}`);
                const fileName = this.generateFileName(altText, imageType);
                const file = new File([blob], fileName, { type: `image/${imageType}` });
                files.push(file);
                matchesInfo.push({ fullMatch, altText, fileName, isHtml: true });
            } catch (error) {
                console.error('Failed to process base64 HTML image:', error);
            }
        }

        if (files.length === 0) {
            return text;
        }

        try {
            const uploadResults = await this.uploadMultipleFiles(files, itemId);

            uploadResults.forEach((result, index) => {
                const { fullMatch, altText, fileName, isHtml } = matchesInfo[index];
                if (result && result.fileUrl) {
                    processedText = processedText.replace(fullMatch, `![${altText}](${result.fileUrl})`);
                } else {
                    processedText = processedText.replace(fullMatch, `⚠️ Échec du téléchargement de l'image "${fileName}"`);
                }
            });
        } catch (error) {
            console.error('Upload error:', error);
            matchesInfo.forEach(({ fullMatch, fileName }) => {
                processedText = processedText.replace(fullMatch, `⚠️ Échec du téléchargement de l'image "${fileName}"`);
            });
        }

        return processedText;
    },

    /**
     * Generate a proper filename with extension
     */
    generateFileName: function(altText, imageType) {
        let fileName = altText || `pasted-image-${Date.now()}`;
        
        fileName = fileName
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-zA-Z0-9.-]/g, '_')
            .replace(/_{2,}/g, '_')
            .replace(/^_|_$/g, '');
        
        fileName = fileName.replace(/\.(png|jpg|jpeg|gif|bmp|webp|svg)$/i, '');
        
        if (fileName.length > 50) {
            fileName = fileName.substring(0, 50);
        }
        
        let extension = 'jpg';
        if (imageType) {
            switch(imageType.toLowerCase()) {
                case 'jpeg':
                case 'jpg':
                    extension = 'jpg';
                    break;
                case 'png':
                    extension = 'png';
                    break;
                case 'gif':
                    extension = 'gif';
                    break;
                case 'bmp':
                    extension = 'bmp';
                    break;
                case 'webp':
                    extension = 'webp';
                    break;
                case 'svg+xml':
                case 'svg':
                    extension = 'svg';
                    break;
                default:
                    extension = 'jpg';
                    break;
            }
        }
        
        return `${fileName}.${extension}`;
    },

    /**
     * Convert base64 string to Blob
     */
    base64ToBlob: function(base64, contentType) {
        const byteCharacters = atob(base64);
        const byteArray = new Uint8Array(byteCharacters.length);

        for (let i = 0; i < byteCharacters.length; i++) {
            byteArray[i] = byteCharacters.charCodeAt(i);
        }

        return new Blob([byteArray], { type: contentType });
    },


    /**
     * Get markdown content from editor
     */
    getContent: function(textareaId) {
        const editorData = this.editors.get(textareaId);
        if (editorData && editorData.editor && editorData.editor.getMarkdown) {
            return editorData.editor.getMarkdown();
        }
        const textarea = document.getElementById(textareaId);
        return textarea ? textarea.value : '';
    },

    /**
     * Set content in editor
     */
    setContent: function(textareaId, content) {
        const editorData = this.editors.get(textareaId);
        if (editorData && editorData.editor && editorData.editor.setMarkdown) {
            editorData.editor.setMarkdown(content);
        }
        const textarea = document.getElementById(textareaId);
        if (textarea) {
            textarea.value = content;
        }
    },

    /**
     * Toggle readonly mode
     */
    setReadonly: function(textareaId, readonly) {
        const editorData = this.editors.get(textareaId);
        if (editorData && editorData.editor && editorData.editor.setReadonly) {
            editorData.editor.setReadonly(readonly);
        }
    },

    /**
     * Destroy editor instance
     */
    destroy: function(textareaId) {
        const editorData = this.editors.get(textareaId);
        if (editorData && editorData.editor && editorData.editor.destroy) {
            editorData.editor.destroy();
            this.editors.delete(textareaId);
        }
    },

    /**
     * Insert text at cursor position
     */
    insertText: function(textareaId, text) {
        const editorData = this.editors.get(textareaId);
        if (editorData && editorData.editor && editorData.editor.action) {
            editorData.editor.action((ctx) => {
                const view = ctx.get(editorViewCtx);
                const { state } = view;
                const { selection } = state;
                const { from } = selection;
                view.dispatch(state.tr.insertText(text, from));
            });
        }
    },

    /**
     * Setup form submission handler
     */
    setupFormSubmit: function(formId, textareaId) {
        const form = document.getElementById(formId);
        if (!form) return;

        form.addEventListener('submit', (e) => {
            const content = this.getContent(textareaId);
            const textarea = document.getElementById(textareaId);
            if (textarea) {
                textarea.value = content;
            }
        });
    },

    /**
     * Handle file upload from BlockEdit menu
     */
    handleFileUpload: async function(files, ctx) {
        const editorData = Array.from(this.editors.values())[0];
        const itemId = editorData ? editorData.itemId : null;

        if (!itemId) {
            console.error('No itemId found for file upload');
            return;
        }

        try {
            const uploadResults = await this.uploadMultipleFiles(files, itemId);

            const editor = editorData.editor;
            if (!editor || !editor.getEditor) {
                console.error('No editor found');
                return;
            }

            const currentMarkdown = editor.getMarkdown();

            const links = uploadResults.map((result, index) => {
                if (result && result.fileUrl) {
                    return `[${result.fileName}](${result.fileUrl})`;
                } else {
                    return `⚠️ Échec du téléchargement de "${files[index].name}"`;
                }
            }).join('\n');

            const newMarkdown = currentMarkdown + '\n' + links;
            editor.setMarkdown(newMarkdown);
        } catch (error) {
            console.error('File upload failed:', error);
        }
    }
};