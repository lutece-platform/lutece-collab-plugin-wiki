import { pickerStyles } from './utils/styles.js';
import PopulationGrant from './features/populationGrant.js';
import PeopleSearch from './features/peopleSearch.js';

const SCROLL_FLAG = 'wikiScrollToUserPermissions';
const REST_BASE = 'rest/wiki/permission';
const LABEL_PREFIX = 'label';
const SUGGESTION_CACHE_LIMIT = 200;

/**
 * Access picker of the item modification page, built after the identitypicker plugin: a modal in a
 * shadow root, with a search screen and a results screen the header back arrow navigates between,
 * animated transitions, a loading overlay and an in-modal confirmation dialog. It grants a
 * permission either to a whole population or to named people through the wiki permission REST
 * endpoints.
 *
 * The host element carries everything it needs: the item code, the security token, the searchable
 * criteria as child elements, and every label as data attributes filled by i18n in the template.
 */
export default class WikiAccessPicker {
    /**
     * Creates the picker and wires the page.
     * @param {HTMLElement} host - The element carrying the configuration, becoming the shadow host
     */
    constructor(host) {
        this.host = host;
        this.code = host.dataset.code;
        this.token = host.dataset.token;
        this.permissionType = 'VIEW';
        this.labels = {};
        this.suggestionCache = new Map();
        Object.entries(host.dataset).forEach(([key, value]) => {
            if (key.startsWith(LABEL_PREFIX)) {
                const name = key.slice(LABEL_PREFIX.length);
                this.labels[name.charAt(0).toLowerCase() + name.slice(1)] = value;
            }
        });
        this.fields = [...host.querySelectorAll('[data-field-code]')]
            .map(span => ({ code: span.dataset.fieldCode.trim(), name: span.dataset.fieldName }));
        this.populationGrant = new PopulationGrant(this);
        this.peopleSearch = new PeopleSearch(this);
        this.createModal();
        this.initPagePanel();
    }

    /**
     * Creates the modal structure inside the shadow root.
     */
    createModal() {
        this.shadowRoot = this.host.attachShadow({ mode: 'open' });
        const style = document.createElement('style');
        style.textContent = pickerStyles;
        this.shadowRoot.appendChild(style);

        this.modal = document.createElement('div');
        this.modal.className = 'ip-modal';
        this.modal.innerHTML = this.getModalHTML();
        this.shadowRoot.appendChild(this.modal);

        this.modalContent = this.shadowRoot.querySelector('.ip-modal-content');
        this.searchContainer = this.shadowRoot.querySelector('.ip-search-container');
        this.resultsContainer = this.shadowRoot.querySelector('.ip-results-container');
        this.backButton = this.shadowRoot.querySelector('.ip-back');
        this.infoMessage = this.shadowRoot.querySelector('.ip-info-message');
        this.headerTitle = this.shadowRoot.querySelector('.ip-header h2');
        this.modalHeader = this.shadowRoot.querySelector('.ip-header');
        this.scrollableContent = this.shadowRoot.querySelector('.ip-scrollable-content');
        this.loadingContainer = this.shadowRoot.querySelector('.ip-loading-container');

        this.initSearchView();
        this.shadowRoot.querySelector('.ip-close').addEventListener('click', () => this.closeModal());
        this.backButton.addEventListener('click', () => this.showSearchView());
        this.shadowRoot.querySelectorAll('.ip-button-close').forEach(button => {
            button.addEventListener('click', () => this.closeModal());
        });
        document.addEventListener('keydown', event => {
            if (event.key === 'Escape' && this.modal.classList.contains('ip-modal-open')) {
                this.closeModal();
            }
        });
    }

    /**
     * Generates the HTML structure of the modal.
     * @returns {string} The modal template
     */
    getModalHTML() {
        return `
            <div class="ip-modal-content">
                <div class="ip-header">
                    <div class="ip-header-container">
                        <span class="ip-back" style="display: none;">&#8592;</span>
                        <h2></h2>
                        <button class="ip-close ip-button-light ip-button-rounded">&times;</button>
                    </div>
                </div>
                <div class="ip-main-container">
                    <div class="ip-scrollable-content">
                        <div class="ip-content-area">
                            <div class="ip-info-message" style="display: none;"></div>
                            <div class="ip-search-container" style="display: none;"></div>
                            <div class="ip-results-container" style="display: none;"></div>
                            <div class="ip-loading-container" style="display: none;">
                                <div class="ip-loader"></div>
                                <div class="ip-loading-message"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Builds the search screen: the two grant modes as radio cards, and the form of each mode.
     */
    initSearchView() {
        this.searchContainer.innerHTML = `
            <div class="ip-search-option">
                <label class="js-perm-mode" data-mode="population">
                    <input type="radio" name="permMode" value="population" checked>
                    <span class="ip-mode-text">
                        <span class="ip-mode-title">${this.labels.tabPopulation}</span>
                        <span class="ip-mode-help">${this.labels.tabPopulationHelp}</span>
                    </span>
                </label>
                <label class="js-perm-mode" data-mode="people">
                    <input type="radio" name="permMode" value="people">
                    <span class="ip-mode-text">
                        <span class="ip-mode-title">${this.labels.tabPeople}</span>
                        <span class="ip-mode-help">${this.labels.tabPeopleHelp}</span>
                    </span>
                </label>
            </div>
            <form id="permPanePopulation"></form>
            <form id="permPanePeople" class="d-none"></form>
        `;
        this.panePopulation = this.searchContainer.querySelector('#permPanePopulation');
        this.panePeople = this.searchContainer.querySelector('#permPanePeople');
        this.populationGrant.render(this.panePopulation);
        this.peopleSearch.render(this.panePeople, this.resultsContainer);
        this.searchContainer.querySelectorAll('input[name="permMode"]').forEach(radio => {
            radio.addEventListener('change', () => this.setMode(radio.value));
        });
    }

    /**
     * Switches between granting a population and granting named people, one intent at a time.
     * @param {string} mode - population or people
     */
    setMode(mode) {
        this.searchContainer.querySelector(`input[name="permMode"][value="${mode}"]`).checked = true;
        this.panePopulation.classList.toggle('d-none', mode !== 'population');
        this.panePeople.classList.toggle('d-none', mode === 'population');
        this.adjustModalHeight();
    }

    /**
     * Shows the search screen.
     */
    showSearchView() {
        this.transitionView(this.searchContainer);
        this.backButton.style.display = 'none';
        this.modalContent.classList.remove('wide-view');
        this.setHeaderTitle(this.permissionType === 'EDIT' ? this.labels.titleEdit : this.labels.titleView);
    }

    /**
     * Shows the results screen, reached after a people search.
     */
    showResultsView() {
        this.transitionView(this.resultsContainer);
        this.backButton.style.display = 'inline';
        this.setHeaderTitle(this.labels.resultsTitle);
    }

    /**
     * Sets the header title text.
     * @param {string} title - The title to display
     */
    setHeaderTitle(title) {
        this.headerTitle.textContent = title;
    }

    /**
     * Transitions between screens with animation.
     * @param {HTMLElement} showElement - The screen to show
     */
    transitionView(showElement) {
        this.removeMessage();
        [this.searchContainer, this.resultsContainer].forEach(el => {
            el.style.display = 'none';
            el.classList.remove('active');
        });
        showElement.style.display = 'block';
        showElement.classList.add('active');
        this.adjustModalHeight();
    }

    /**
     * Adjusts the modal height to the viewport.
     */
    adjustModalHeight() {
        this.modalContent.style.maxHeight = `${window.innerHeight * 0.9}px`;
        this.scrollableContent.style.maxHeight = `${window.innerHeight * 0.9 - this.modalHeader.offsetHeight}px`;
    }

    /**
     * Shows a message in the info message area.
     * @param {string} text - The message
     * @param {string} type - info, error, success or warning
     */
    showMessage(text, type) {
        this.infoMessage.replaceChildren();
        const main = document.createElement('p');
        main.className = 'ip-info-message-main';
        main.textContent = text;
        const closeButton = document.createElement('span');
        closeButton.className = 'ip-info-message-close';
        closeButton.textContent = '×';
        closeButton.addEventListener('click', () => this.removeMessage());
        this.infoMessage.append(main, closeButton);
        this.infoMessage.className = `ip-info-message ${type}`;
        this.infoMessage.style.display = 'block';
        this.adjustModalHeight();
    }

    /**
     * Removes the current message from display.
     */
    removeMessage() {
        this.infoMessage.style.display = 'none';
        this.infoMessage.replaceChildren();
    }

    /**
     * Covers the content with the loading overlay while the directory answers.
     * @param {string} message - What is being waited for
     */
    showLoading(message) {
        this.scrollableContent.scrollTop = 0;
        this.loadingContainer.querySelector('.ip-loading-message').textContent = message || '';
        this.loadingContainer.style.display = 'flex';
        void this.loadingContainer.offsetWidth;
        this.loadingContainer.classList.add('show');
    }

    /**
     * Uncovers the content.
     */
    hideLoading() {
        this.loadingContainer.classList.remove('show');
        setTimeout(() => {
            this.loadingContainer.style.display = 'none';
        }, 300);
    }

    /**
     * Opens the modal for a permission type, on the search screen.
     * @param {string} permissionType - VIEW or EDIT
     */
    openModal(permissionType) {
        this.permissionType = permissionType;
        this.modal.style.display = 'flex';
        this.showSearchView();
        document.body.style.overflow = 'hidden';
        requestAnimationFrame(() => {
            this.modal.classList.add('ip-modal-open');
            this.adjustModalHeight();
        });
    }

    /**
     * Closes the modal with animation, then puts the forms back to their resting state.
     */
    closeModal() {
        this.modal.classList.remove('ip-modal-open');
        setTimeout(() => {
            document.body.style.overflow = '';
            this.modal.style.display = 'none';
            setTimeout(() => this.resetForms(), 200);
        }, 300);
    }

    /**
     * Puts both screens back to their resting state, for the next opening.
     */
    resetForms() {
        this.populationGrant.reset();
        this.peopleSearch.reset();
        this.setMode('population');
        this.removeMessage();
        this.showSearchView();
    }

    /**
     * Shows an in-modal confirmation dialog.
     * @param {string} message - The confirmation message
     * @param {Function} onConfirm - Called when confirmed
     */
    showConfirmDialog(message, onConfirm) {
        const modal = document.createElement('div');
        modal.className = 'ip-confirm-modal ip-modal ip-modal-open';
        modal.style.display = 'flex';
        modal.innerHTML = `
            <div class="ip-confirm-dialog ip-modal-content">
                <p></p>
                <div class="ip-confirm-buttons">
                    <button class="ip-button-light ip-cancel-btn">${this.labels.cancel}</button>
                    <button class="ip-confirm-btn">${this.labels.add}</button>
                </div>
            </div>
        `;
        modal.querySelector('p').textContent = message;
        const closeDialog = () => this.shadowRoot.removeChild(modal);
        modal.querySelector('.ip-cancel-btn').addEventListener('click', closeDialog);
        modal.querySelector('.ip-confirm-btn').addEventListener('click', () => {
            onConfirm();
            closeDialog();
        });
        modal.addEventListener('click', event => {
            if (event.target === modal) {
                closeDialog();
            }
        });
        this.shadowRoot.appendChild(modal);
    }

    /**
     * Calls a permission endpoint, the payload in the body, and keeps the token issued for the
     * next call.
     * @param {string} path - The endpoint below the permission root
     * @param {URLSearchParams} payload - The request body
     * @returns {Promise<Object>} ok plus the parsed data, or ok false plus the status
     */
    async call(path, payload) {
        const query = new URLSearchParams({ code: this.code, permission_type: this.permissionType, token: this.token }).toString();
        const response = await fetch(`${REST_BASE}/${path}?${query}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: payload
        });
        if (!response.ok) {
            return { ok: false, status: response.status };
        }
        const data = await response.json();
        if (data.nextToken) {
            this.token = data.nextToken;
        }
        return { ok: true, data };
    }

    /**
     * Asks the server which values an attribute takes, among those still possible given the other
     * criteria. The directory holds thousands of combinations once every organisation level is
     * mapped, so they stay on the server and only the matching values travel. Answers are kept for
     * the page's lifetime: the values change once a day, the same prefixes are asked constantly.
     * @param {string} attribute - The provider attribute
     * @param {string} typed - What the user typed so far
     * @param {AbortSignal} signal - Cancels the call when the user keeps typing
     * @param {Object} context - Values held by the other attribute fields
     * @returns {Promise<Array>} The matching values
     */
    async fetchSuggestions(attribute, typed, signal, context = {}) {
        const params = new URLSearchParams({ code: this.code, attribute, q: typed });
        Object.entries(context).forEach(([name, value]) => params.append('provider_attribute_' + name, value));
        const query = params.toString();
        if (this.suggestionCache.has(query)) {
            return this.suggestionCache.get(query);
        }
        const response = await fetch(`${REST_BASE}/attribute-values?${query}`, {
            credentials: 'same-origin',
            headers: { Accept: 'application/json' },
            signal
        });
        if (!response.ok) {
            return [];
        }
        const data = await response.json();
        const values = Array.isArray(data.values) ? data.values : [];
        if (this.suggestionCache.size >= SUGGESTION_CACHE_LIMIT) {
            this.suggestionCache.clear();
        }
        this.suggestionCache.set(query, values);
        return values;
    }

    /**
     * Asks the server what a population rule on an attribute value would cover: the directory
     * spelling of the value and how many agents carry it today.
     * @param {string} attribute - The provider attribute
     * @param {string} value - The value as typed
     * @returns {Promise<Object>} canonical (null when unknown) and count
     */
    async fetchPopulationCount(attribute, value) {
        const params = new URLSearchParams({ code: this.code, attribute, value });
        const response = await fetch(`${REST_BASE}/population-count?${params.toString()}`, {
            credentials: 'same-origin',
            headers: { Accept: 'application/json' }
        });
        if (!response.ok) {
            throw new Error(String(response.status));
        }
        return response.json();
    }

    /**
     * Reloads the page and asks the next load to land on the permission panel.
     */
    reloadOnPanel() {
        window.sessionStorage.setItem(SCROLL_FLAG, '1');
        window.location.reload();
    }

    /**
     * Wires the permission panel of the page: open buttons, population badges, scroll restore.
     */
    initPagePanel() {
        document.querySelectorAll('.js-add-users').forEach(button => {
            button.addEventListener('click', event => {
                event.preventDefault();
                this.openModal(button.dataset.permissionType);
            });
        });

        document.querySelectorAll('.js-remove-population').forEach(button => {
            button.addEventListener('click', async () => {
                this.permissionType = button.dataset.permissionType;
                const params = new URLSearchParams({ attribute: button.dataset.attribute, value: button.dataset.value });
                button.disabled = true;
                const outcome = await this.call('revoke-attribute', params);
                if (outcome.ok) {
                    this.reloadOnPanel();
                } else {
                    button.disabled = false;
                }
            });
        });

        document.querySelectorAll('a[href*="action=removeUserPermission"]').forEach(link => {
            link.addEventListener('click', () => window.sessionStorage.setItem(SCROLL_FLAG, '1'));
        });

        if (window.sessionStorage.getItem(SCROLL_FLAG) === '1') {
            window.sessionStorage.removeItem(SCROLL_FLAG);
            this.scrollToPanel();
            window.setTimeout(() => this.scrollToPanel(), 300);
        }
    }

    /**
     * Puts the view on the permission panel, without animation.
     */
    scrollToPanel() {
        const target = document.getElementById('userPermissions');
        if (target) {
            target.scrollIntoView({ behavior: 'instant', block: 'start' });
        }
    }
}
