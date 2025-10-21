class TbPicker {
    constructor(options = {}) {
        this.selector = options.selector || '.tb-picker';
        this.cssUrl = options.cssUrl || 'css/tabler-icons.min.css';
        this.icons = [];
        this.filteredIcons = [];
        this.currentPage = 0;
        this.iconsPerPage = 36;
        this.isLoading = true;
        this.init();
    }

    async init() {
        document.querySelectorAll(this.selector).forEach(el => this.createPicker(el));
        await this.fetchIcons();
        this.isLoading = false;
        document.querySelectorAll(this.selector).forEach(el => this.renderIcons(el));
    }

    async fetchIcons() {
        try {
            const response = await fetch(this.cssUrl);
            const css = await response.text();
            const regex = /\.ti-([a-z0-9-]+):before/g;
            let match;
            const iconSet = new Set();
            while ((match = regex.exec(css)) !== null) {
                iconSet.add(match[1]);
            }
            this.icons = Array.from(iconSet).sort();
            this.filteredIcons = [...this.icons];
        } catch (e) {
            console.error('TbPicker: Failed to fetch icons', e);
        }
    }

    createPicker(container) {
        const inputName = container.dataset.name || 'icon';
        const currentValue = container.dataset.value || '';

        container.innerHTML = `
            <div class="tbp-wrapper">
                <input type="hidden" name="${inputName}" class="tbp-value" value="${currentValue}">
                <div class="tbp-trigger" tabindex="0">
                    <div class="tbp-preview ${currentValue ? '' : 'empty'}">
                        <i class="${currentValue || 'ti ti-icons'}"></i>
                    </div>
                    <span class="tbp-label">${currentValue || 'Sélectionner une icône'}</span>
                    <i class="ti ti-chevron-down tbp-arrow"></i>
                </div>
                <div class="tbp-panel">
                    <input type="text" class="tbp-search" placeholder="Rechercher...">
                    <div class="tbp-grid">
                        <div class="tbp-loading">
                            <i class="ti ti-loader-2 tbp-spinner"></i>
                            <span>Chargement...</span>
                        </div>
                    </div>
                    <div class="tbp-footer">
                        <button type="button" class="tbp-prev" disabled>&laquo; Préc.</button>
                        <span class="tbp-page-info">-</span>
                        <button type="button" class="tbp-next" disabled>Suiv. &raquo;</button>
                    </div>
                </div>
            </div>
        `;

        this.bindEvents(container);
    }

    bindEvents(container) {
        const trigger = container.querySelector('.tbp-trigger');
        const search = container.querySelector('.tbp-search');
        const prev = container.querySelector('.tbp-prev');
        const next = container.querySelector('.tbp-next');

        trigger.addEventListener('click', () => {
            container.classList.toggle('open');
            if (container.classList.contains('open')) {
                search.focus();
            }
        });

        document.addEventListener('click', (e) => {
            if (!container.contains(e.target)) {
                container.classList.remove('open');
            }
        });

        search.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            this.filteredIcons = this.icons.filter(icon => icon.includes(query));
            this.currentPage = 0;
            this.renderIcons(container);
        });

        prev.addEventListener('click', () => {
            if (this.currentPage > 0) {
                this.currentPage--;
                this.renderIcons(container);
            }
        });

        next.addEventListener('click', () => {
            const maxPage = Math.ceil(this.filteredIcons.length / this.iconsPerPage) - 1;
            if (this.currentPage < maxPage) {
                this.currentPage++;
                this.renderIcons(container);
            }
        });

        container.querySelector('.tbp-grid').addEventListener('click', (e) => {
            const iconEl = e.target.closest('.tbp-icon');
            if (iconEl) {
                this.selectIcon(container, iconEl.dataset.icon);
            }
        });
    }

    renderIcons(container) {
        const grid = container.querySelector('.tbp-grid');
        const prev = container.querySelector('.tbp-prev');
        const next = container.querySelector('.tbp-next');
        const pageInfo = container.querySelector('.tbp-page-info');

        if (this.isLoading) {
            return;
        }

        const start = this.currentPage * this.iconsPerPage;
        const end = start + this.iconsPerPage;
        const pageIcons = this.filteredIcons.slice(start, end);
        const totalPages = Math.ceil(this.filteredIcons.length / this.iconsPerPage);

        if (pageIcons.length === 0) {
            grid.innerHTML = '<div class="tbp-no-results">Aucune icône trouvée</div>';
        } else {
            grid.innerHTML = pageIcons.map(icon => `
                <div class="tbp-icon" data-icon="ti ti-${icon}" title="${icon}">
                    <i class="ti ti-${icon}"></i>
                </div>
            `).join('');
        }

        prev.disabled = this.currentPage === 0;
        next.disabled = this.currentPage >= totalPages - 1;
        pageInfo.textContent = `${this.currentPage + 1} / ${totalPages || 1}`;
    }

    selectIcon(container, iconClass) {
        const input = container.querySelector('.tbp-value');
        const preview = container.querySelector('.tbp-preview');
        const label = container.querySelector('.tbp-label');

        input.value = iconClass;
        preview.innerHTML = `<i class="${iconClass}"></i>`;
        preview.classList.remove('empty');
        label.textContent = iconClass;
        container.classList.remove('open');

        container.dispatchEvent(new CustomEvent('iconselected', { detail: { icon: iconClass } }));
    }
}
