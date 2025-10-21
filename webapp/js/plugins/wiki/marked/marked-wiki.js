(function() {
    'use strict';

    const basePath = 'js/plugins/wiki/marked/';
    const depsPath = basePath + 'dependencies/';
    const extensionsPath = basePath + 'extensions/';

    const coreLibs = ['marked.min.js', 'highlight.min.js', 'purify.min.js'];
    const extensions = ['marked-alert.js', 'marked-video.js', 'marked-mermaid.js', 'marked-lightbox.js'];

    /**
     * TOC Layout Constants
     * LINE_OFFSET_PER_DEPTH: Horizontal offset in pixels for each depth level (10px per level)
     * ITEM_BASE_OFFSET: Base padding for TOC items (14px)
     * ITEM_OFFSET_PER_DEPTH: Additional padding per depth level (12px per level)
     */
    const TOC_LINE_OFFSET_PER_DEPTH = 10;
    const TOC_ITEM_BASE_OFFSET = 14;
    const TOC_ITEM_OFFSET_PER_DEPTH = 12;

    const MarkedWiki = {
        _initPromise: null,
        _scrollSpyCleanup: null,

        escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        },

        initCodeCopy(container) {
            container.querySelectorAll('.wiki-code-copy').forEach(btn => {
                if (btn.dataset.initialized) return;
                btn.dataset.initialized = 'true';
                btn.addEventListener('click', function() {
                    const code = this.closest('.card').querySelector('code').textContent;
                    navigator.clipboard.writeText(code).then(() => {
                        btn.innerHTML = '<i class="ti ti-check"></i>';
                        setTimeout(() => btn.innerHTML = '<i class="ti ti-copy"></i>', 2000);
                    });
                });
            });
        },

        loadScript(src) {
            return new Promise((resolve, reject) => {
                if (document.querySelector(`script[src="${src}"]`)) {
                    resolve();
                    return;
                }
                const script = document.createElement('script');
                script.src = src;
                script.onload = () => setTimeout(resolve, 10);
                script.onerror = reject;
                document.head.appendChild(script);
            });
        },

        async loadScriptsSequentially(scripts, basePath) {
            for (const script of scripts) {
                await this.loadScript(basePath + script);
            }
        },

        async init() {
            if (this._initPromise) return this._initPromise;

            this._initPromise = (async () => {
                await this.loadScriptsSequentially(coreLibs, depsPath);
                await this.loadScriptsSequentially(extensions, extensionsPath);

                const renderer = new marked.Renderer();
                const originalLinkRenderer = renderer.link;
                renderer.link = function(token) {
                    const href = token.href || '';
                    const hrefLower = href.toLowerCase().trim();
                    if (hrefLower.startsWith('javascript:') || hrefLower.startsWith('data:') || hrefLower.startsWith('vbscript:')) {
                        return token.text || '';
                    }
                    const html = originalLinkRenderer.call(this, token);
                    return html.replace(/^<a /, '<a target="_blank" rel="noopener noreferrer" ');
                };

                const self = this;
                renderer.code = function(token) {
                    const code = token.text || token;
                    const lang = token.lang || '';
                    const langDisplay = lang || 'text';
                    let highlighted;
                    if (lang && hljs.getLanguage(lang)) {
                        try {
                            highlighted = hljs.highlight(code, { language: lang }).value;
                        } catch (err) {
                            highlighted = self.escapeHtml(code);
                        }
                    } else {
                        highlighted = self.escapeHtml(code);
                    }
                    return `<div class="card my-3">
                        <div class="card-header d-flex justify-content-between align-items-center py-2">
                        <small class="text-muted">${langDisplay}</small>
                        <button class="wiki-code-copy btn btn-sm btn-light" title="Copier">
                        <i class="ti ti-copy"></i>
                        </button>
                        </div>
                        <div class="card-body p-0">
                        <pre class="m-0 p-3"><code class="hljs language-${langDisplay}">${highlighted}</code></pre>
                        </div>
                        </div>`;
                };

                marked.setOptions({ renderer, breaks: true, gfm: true });

                if (window.markedAlert) marked.use(window.markedAlert);
                if (window.markedVideo) marked.use(window.markedVideo);
                if (window.markedMermaid) marked.use(window.markedMermaid);
            })();

            return this._initPromise;
        },

        async render(container, content) {
            if (typeof container === 'string') {
                container = document.querySelector(container);
            }
            if (!container) {
                console.error('MarkedWiki: container not found');
                return;
            }

            await this.init();
            container.innerHTML = DOMPurify.sanitize(marked.parse(content));
            this.initCodeCopy(container);

            if (window.renderMermaidBlocks) {
                renderMermaidBlocks(container);
            }
            if (window.initLightbox) {
                initLightbox(container);
            }

            return container;
        },

        async parse(content) {
            await this.init();
            return DOMPurify.sanitize(marked.parse(content));
        },

        getLineOffset(depth) {
            return depth * TOC_LINE_OFFSET_PER_DEPTH;
        },

        getItemOffset(depth) {
            return TOC_ITEM_BASE_OFFSET + depth * TOC_ITEM_OFFSET_PER_DEPTH;
        },

        async generateTOC(contentSelector, tocSelector) {
            const contentEl = document.querySelector(contentSelector);
            const tocContainer = document.querySelector(tocSelector);
            if (!contentEl || !tocContainer) return;

            await this.init();

            const allHeaders = contentEl.querySelectorAll('h1, h2, h3, h4, h5, h6');
            const headers = Array.from(allHeaders).filter(header => {
                const style = window.getComputedStyle(header);
                return style.display !== 'none' && style.visibility !== 'hidden';
            });

            tocContainer.innerHTML = '';

            const navWrapper = document.createElement('div');
            navWrapper.className = 'toc-nav-wrapper';

            const tocItems = new Map();
            const itemsContainer = document.createElement('div');
            itemsContainer.className = 'toc-items-container';

            const baseLevel = headers.length > 0
                ? Math.min(...headers.map(h => parseInt(h.tagName.charAt(1))))
                : 1;

            const items = [];
            headers.forEach((header, index) => {
                if (!header.id) header.id = 'heading-' + index;
                const level = parseInt(header.tagName.charAt(1));
                const depth = level - baseLevel;
                items.push({ id: header.id, title: header.textContent, depth });
            });

            items.forEach((item, index) => {
                const upperDepth = index > 0 ? items[index - 1].depth : item.depth;
                const lowerDepth = index < items.length - 1 ? items[index + 1].depth : item.depth;

                const offset = this.getLineOffset(item.depth);
                const upperOffset = this.getLineOffset(upperDepth);
                const lowerOffset = this.getLineOffset(lowerDepth);

                const a = document.createElement('a');
                a.href = '#' + item.id;
                a.className = 'toc-link';
                a.dataset.headingId = item.id;
                a.dataset.depth = item.depth;
                a.style.paddingInlineStart = this.getItemOffset(item.depth) + 'px';

                if (offset !== upperOffset) {
                    const svgWidth = Math.max(upperOffset, offset) + 1;
                    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
                    svg.setAttribute('viewBox', `0 0 ${svgWidth} 16`);
                    svg.style.width = svgWidth + 'px';
                    svg.classList.add('toc-connector');

                    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                    line.setAttribute('x1', upperOffset);
                    line.setAttribute('y1', '0');
                    line.setAttribute('x2', offset);
                    line.setAttribute('y2', '12');
                    line.classList.add('toc-connector-line');

                    svg.appendChild(line);
                    a.appendChild(svg);
                }

                const verticalLine = document.createElement('div');
                verticalLine.className = 'toc-vertical-line';
                verticalLine.style.insetInlineStart = offset + 'px';

                if (offset !== upperOffset) verticalLine.classList.add('toc-line-top-cut');
                if (offset !== lowerOffset) verticalLine.classList.add('toc-line-bottom-cut');

                a.appendChild(verticalLine);

                const textSpan = document.createElement('span');
                textSpan.textContent = item.title;
                a.appendChild(textSpan);

                a.addEventListener('click', (e) => {
                    e.preventDefault();
                    const targetId = a.getAttribute('href').substring(1);
                    const targetElement = document.getElementById(targetId);
                    if (targetElement) {
                        targetElement.scrollIntoView({ behavior: 'auto' });
                        window.location.hash = targetId;
                        this.blinkElement(targetElement);
                    }
                });

                itemsContainer.appendChild(a);
                tocItems.set(item.id, { element: a, depth: item.depth });
            });

            navWrapper.appendChild(itemsContainer);
            tocContainer.appendChild(navWrapper);

            this.initZoneScrollSpy(items, tocItems, itemsContainer);
        },

        initZoneScrollSpy(items, tocItems, itemsContainer) {
            let svgMaskContainer = null;
            let tocThumb = null;
            let currentSvgInfo = null;

            const createSvgMask = () => {
                if (itemsContainer.clientHeight === 0) return;

                let maxWidth = 0;
                let maxHeight = 0;
                const pathSegments = [];

                items.forEach((item, index) => {
                    const element = itemsContainer.querySelector(`a[href="#${item.id}"]`);
                    if (!element) return;

                    const styles = getComputedStyle(element);
                    const offset = this.getLineOffset(item.depth) + 1;
                    const top = element.offsetTop + parseFloat(styles.paddingTop);
                    const bottom = element.offsetTop + element.clientHeight - parseFloat(styles.paddingBottom);

                    maxWidth = Math.max(offset, maxWidth);
                    maxHeight = Math.max(maxHeight, bottom);

                    pathSegments.push((index === 0 ? 'M' : 'L') + offset + ' ' + top);
                    pathSegments.push('L' + offset + ' ' + bottom);
                });

                if (pathSegments.length === 0) return;

                currentSvgInfo = {
                    path: pathSegments.join(' '),
                    width: maxWidth + 1,
                    height: maxHeight
                };

                if (!svgMaskContainer) {
                    svgMaskContainer = document.createElement('div');
                    svgMaskContainer.className = 'toc-svg-mask-container';
                    itemsContainer.insertBefore(svgMaskContainer, itemsContainer.firstChild);

                    tocThumb = document.createElement('div');
                    tocThumb.className = 'toc-thumb';
                    svgMaskContainer.appendChild(tocThumb);
                }

                svgMaskContainer.style.width = currentSvgInfo.width + 'px';
                svgMaskContainer.style.height = currentSvgInfo.height + 'px';

                const svgData = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${currentSvgInfo.width} ${currentSvgInfo.height}"><path d="${currentSvgInfo.path}" stroke="black" stroke-width="1" fill="none" /></svg>`;
                svgMaskContainer.style.maskImage = `url("data:image/svg+xml,${encodeURIComponent(svgData)}")`;
                svgMaskContainer.style.webkitMaskImage = `url("data:image/svg+xml,${encodeURIComponent(svgData)}")`;
            };

            const calcThumbPosition = (activeIds) => {
                if (activeIds.length === 0 || itemsContainer.clientHeight === 0) {
                    return [0, 0];
                }

                let upper = Number.MAX_VALUE;
                let lower = 0;

                for (const id of activeIds) {
                    const element = itemsContainer.querySelector(`a[href="#${id}"]`);
                    if (!element) continue;

                    const styles = getComputedStyle(element);
                    upper = Math.min(upper, element.offsetTop + parseFloat(styles.paddingTop));
                    lower = Math.max(lower, element.offsetTop + element.clientHeight - parseFloat(styles.paddingBottom));
                }

                return [upper, lower - upper];
            };

            const updateVisibleZone = () => {
                const headingPositions = items.map(item => {
                    const headerEl = document.getElementById(item.id);
                    if (!headerEl) return null;
                    const rect = headerEl.getBoundingClientRect();
                    return { id: item.id, top: rect.top, bottom: rect.bottom };
                }).filter(item => item !== null);

                if (headingPositions.length === 0) return;

                const viewportHeight = window.innerHeight;
                const visibleIds = headingPositions
                    .filter(h => h.top < viewportHeight - 100 && h.bottom > 100)
                    .map(h => h.id);

                tocItems.forEach((item, id) => {
                    item.element.dataset.active = visibleIds.includes(id) ? 'true' : 'false';
                });

                if (visibleIds.length > 0 && tocThumb) {
                    const pos = calcThumbPosition(visibleIds);
                    tocThumb.style.setProperty('--fd-top', pos[0] + 'px');
                    tocThumb.style.setProperty('--fd-height', pos[1] + 'px');

                    const navWrapper = itemsContainer.parentElement;
                    if (navWrapper) {
                        const firstItem = tocItems.get(visibleIds[0]);
                        if (firstItem) {
                            const scrollTop = navWrapper.scrollTop;
                            const clientHeight = navWrapper.clientHeight;
                            const itemTop = firstItem.element.offsetTop;
                            const itemHeight = firstItem.element.clientHeight;

                            if (itemTop < scrollTop) {
                                navWrapper.scrollTop = itemTop - 20;
                            } else if (itemTop + itemHeight > scrollTop + clientHeight) {
                                navWrapper.scrollTop = itemTop - clientHeight + itemHeight + 20;
                            }
                        }
                    }
                }
            };

            createSvgMask();

            const resizeObserver = new ResizeObserver(() => {
                createSvgMask();
                updateVisibleZone();
            });
            resizeObserver.observe(itemsContainer);

            updateVisibleZone();
            window.addEventListener('scroll', updateVisibleZone, { passive: true });
            window.addEventListener('resize', updateVisibleZone);

            this._scrollSpyCleanup = () => {
                window.removeEventListener('scroll', updateVisibleZone);
                window.removeEventListener('resize', updateVisibleZone);
                resizeObserver.disconnect();
            };
        },

        cleanup() {
            if (this._scrollSpyCleanup) {
                this._scrollSpyCleanup();
                this._scrollSpyCleanup = null;
            }
        },

        blinkElement(element) {
            element.style.animation = 'none';
            void element.offsetWidth;
            element.style.animation = 'toc-blink 1s ease-out';
            setTimeout(() => element.style.animation = '', 1000);
        }
    };

    window.MarkedWiki = MarkedWiki;
})();
