(function() {
    'use strict';

    const LINE_OFFSET_PER_DEPTH = 10;
    const ITEM_BASE_OFFSET = 14;
    const ITEM_OFFSET_PER_DEPTH = 12;
    const HEADER_TYPES = ['category', 'chapter'];
    const LEAF_TYPES = ['book', 'page'];
    const STORAGE_PREFIX = 'wiki.sidebar.open.';
    const HIDDEN = 'sidebar-tree-hidden';

    /**
     * Returns the depth of a tree item.
     * @param {Element} item the tree item
     * @returns {number} its depth
     */
    function depthOf(item) {
        return parseInt(item.dataset.depth) || 0;
    }

    /**
     * Lowers the case and removes the accents of a text.
     * @param {string} text the text
     * @returns {string} the normalized text
     */
    function normalize(text) {
        return text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    }

    /**
     * Reads the codes of the headers the user left open.
     * @param {string} key the storage key
     * @returns {Array<string>|null} the codes, or null when nothing is stored
     */
    function readOpen(key) {
        try {
            const value = localStorage.getItem(key);
            return value ? JSON.parse(value) : null;
        } catch (e) {
            return null;
        }
    }

    /**
     * Stores the codes of the open headers.
     * @param {string} key the storage key
     * @param {Array<string>} codes the codes
     */
    function writeOpen(key, codes) {
        try {
            localStorage.setItem(key, JSON.stringify(codes));
        } catch (e) {
            return;
        }
    }

    /**
     * Draws the tree lines of the visible items.
     * @param {Array<Element>} items every item of the tree
     */
    function drawLines(items) {
        const visible = items.filter(el => !el.classList.contains(HIDDEN));
        items.forEach(el => el.querySelectorAll(':scope > .sidebar-tree-connector, :scope > .sidebar-tree-vertical-line').forEach(n => n.remove()));
        visible.forEach((el, index) => {
            const depth = depthOf(el);
            const offset = depth * LINE_OFFSET_PER_DEPTH;
            const upperOffset = (index > 0 ? depthOf(visible[index - 1]) : depth) * LINE_OFFSET_PER_DEPTH;
            const lowerOffset = (index < visible.length - 1 ? depthOf(visible[index + 1]) : depth) * LINE_OFFSET_PER_DEPTH;

            el.style.paddingInlineStart = (ITEM_BASE_OFFSET + depth * ITEM_OFFSET_PER_DEPTH) + 'px';

            if (offset !== upperOffset) {
                const width = Math.max(upperOffset, offset) + 1;
                const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
                svg.setAttribute('viewBox', '0 0 ' + width + ' 16');
                svg.style.width = width + 'px';
                svg.style.height = '16px';
                svg.classList.add('sidebar-tree-connector');
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', upperOffset);
                line.setAttribute('y1', '0');
                line.setAttribute('x2', offset);
                line.setAttribute('y2', '12');
                svg.appendChild(line);
                el.insertBefore(svg, el.firstChild);
            }

            const vertical = document.createElement('div');
            vertical.className = 'sidebar-tree-vertical-line';
            vertical.style.insetInlineStart = offset + 'px';
            if (offset !== upperOffset) vertical.classList.add('sidebar-tree-line-top-cut');
            if (offset !== lowerOffset) vertical.classList.add('sidebar-tree-line-bottom-cut');
            el.insertBefore(vertical, el.firstChild);
        });
    }

    /**
     * Makes a sidebar tree collapsible and filterable.
     * @param {Element} tree the tree container
     */
    function initTree(tree) {
        const items = Array.from(tree.querySelectorAll('.sidebar-tree-item'));
        if (!items.length) return;

        const children = new Map(items.map((item, index) => {
            const below = [];
            for (let i = index + 1; i < items.length && depthOf(items[i]) > depthOf(item); i++) below.push(items[i]);
            return [item, below];
        }));
        const parents = new Map(items.map((item, index) => {
            const above = [];
            let depth = depthOf(item);
            for (let i = index - 1; i >= 0 && depth > 0; i--) {
                if (depthOf(items[i]) < depth) {
                    above.push(items[i]);
                    depth = depthOf(items[i]);
                }
            }
            return [item, above];
        }));
        const headers = items.filter(el => HEADER_TYPES.includes(el.dataset.type) && children.get(el).length);
        const leaves = items.filter(el => LEAF_TYPES.includes(el.dataset.type));
        const labels = new Map(leaves.map(el => [el, el.querySelector('.sidebar-tree-link')]));
        const root = items.find(el => depthOf(el) === 0);
        const storageKey = STORAGE_PREFIX + (root ? root.dataset.code : tree.id);
        const active = items.find(el => el.classList.contains('active'));
        const forcedOpen = active ? parents.get(active) : [];
        const stored = readOpen(storageKey) || [];
        const open = new Set(headers.filter(h => stored.includes(h.dataset.code) || forcedOpen.includes(h)));
        const filter = document.querySelector('.wiki-sidebar-filter input[data-tree="' + tree.id + '"]');
        const empty = document.querySelector('.wiki-sidebar-filter-empty[data-tree="' + tree.id + '"]');

        headers.forEach(header => {
            const title = header.querySelector(':scope > span');
            const toggle = document.createElement('button');
            toggle.type = 'button';
            toggle.className = 'sidebar-tree-toggle';
            const label = document.createElement('span');
            label.append(...title.childNodes);
            toggle.appendChild(label);
            toggle.insertAdjacentHTML('beforeend', '<i class="ti ti-chevron-down" aria-hidden="true"></i>');
            title.appendChild(toggle);
            toggle.addEventListener('click', () => {
                if (open.has(header)) open.delete(header); else open.add(header);
                writeOpen(storageKey, headers.filter(h => open.has(h)).map(h => h.dataset.code));
                render();
            });
        });

        /**
         * Shows or hides the leaf link text, marking the query.
         * @param {Element} link the link
         * @param {string} text the original text
         * @param {string} query the normalized query
         * @returns {boolean} true when the text matches the query
         */
        function mark(link, text, query) {
            const at = query ? normalize(text).indexOf(query) : -1;
            link.textContent = text;
            if (at < 0) return !query;
            const hit = document.createElement('mark');
            hit.className = 'sidebar-tree-hit';
            hit.textContent = text.slice(at, at + query.length);
            link.textContent = text.slice(0, at);
            link.append(hit, text.slice(at + query.length));
            return true;
        }

        const texts = new Map(leaves.map(el => [el, labels.get(el) ? labels.get(el).textContent.trim() : '']));

        /**
         * Applies the open headers and the filter to the tree.
         */
        function render() {
            const query = filter ? normalize(filter.value.trim()) : '';
            const matches = new Set(leaves.filter(el => labels.get(el) && mark(labels.get(el), texts.get(el), query)));
            const expanded = query ? new Set(headers.filter(h => children.get(h).some(c => matches.has(c)))) : open;

            items.forEach(el => {
                const closed = parents.get(el).some(p => headers.includes(p) && !expanded.has(p));
                const filtered = query && LEAF_TYPES.includes(el.dataset.type) && !matches.has(el);
                el.classList.toggle(HIDDEN, Boolean(closed || filtered));
            });
            headers.forEach(h => {
                h.classList.toggle('collapsed', !expanded.has(h));
                h.querySelector('.sidebar-tree-toggle').setAttribute('aria-expanded', String(expanded.has(h)));
            });
            if (empty) empty.hidden = !query || matches.size > 0;
            drawLines(items);
        }

        if (filter) filter.addEventListener('input', render);
        render();
    }

    /**
     * Initializes every sidebar tree of the page, lets its action menus overflow the scrolling sidebar and scrolls to
     * the active item.
     */
    function init() {
        document.querySelectorAll('.wiki-sidebar-tree').forEach(initTree);
        document.querySelectorAll('.wiki-sidebar-tree [data-bs-toggle="dropdown"]')
            .forEach(toggle => toggle.setAttribute('data-bs-popper-config', '{"strategy":"fixed"}'));
        const active = document.querySelector('.wiki-sidebar-tree .sidebar-tree-item.active');
        if (active) active.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    init();
})();
