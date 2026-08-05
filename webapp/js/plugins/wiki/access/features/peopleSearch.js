import { toTitleCase } from '../utils/utils.js';
import { attachSuggestions } from '../utils/suggest.js';

const CONFIRM_THRESHOLD = 25;
const ICON_SEARCH = '<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 16 16" fill="currentColor">'
    + '<path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/></svg>';

/**
 * People flow of the picker: a search form on the search screen, its outcome on the results
 * screen, in the manner of the identitypicker plugin — the criteria recalled as tags above the
 * animated result rows, and the actions at the bottom of the screen.
 */
export default class PeopleSearch {
    /**
     * Creates the people flow.
     * @param {Object} picker - The parent access picker
     */
    constructor(picker) {
        this.picker = picker;
        this.list = null;
        this.addButton = null;
    }

    /**
     * Renders the search form and wires its behaviour.
     * @param {HTMLFormElement} form - The form element inside the search screen
     * @param {HTMLElement} resultsContainer - The results screen container
     */
    render(form, resultsContainer) {
        const labels = this.picker.labels;
        this.form = form;
        this.resultsContainer = resultsContainer;
        form.innerHTML = `
            <div class="ip-form-row">
                <div class="ip-form-input">
                    <label for="permSearchLastname">${labels.lastname}</label>
                    <input type="text" id="permSearchLastname" autocomplete="off">
                </div>
                <div class="ip-form-input">
                    <label for="permSearchGivenname">${labels.givenname}</label>
                    <input type="text" id="permSearchGivenname" autocomplete="off">
                </div>
            </div>
            <div class="ip-form-row">
                <div class="ip-form-input" style="flex-basis: 100%;">
                    <label for="permSearchEmail">${labels.email}</label>
                    <input type="text" id="permSearchEmail" autocomplete="off">
                </div>
            </div>
            <details class="ip-fieldset d-none" id="permRefine">
                <summary>${labels.refine}</summary>
                <div class="ip-form-row"></div>
            </details>
            <div class="ip-container-buttons">
                <button type="button" class="ip-button-light ip-button-close">${labels.cancel}</button>
                <button type="submit" id="permSearchButton">${ICON_SEARCH} ${labels.search}</button>
            </div>
        `;

        this.lastname = form.querySelector('#permSearchLastname');
        this.givenname = form.querySelector('#permSearchGivenname');
        this.email = form.querySelector('#permSearchEmail');
        this.refine = form.querySelector('#permRefine');

        this.attributeFields = this.picker.fields.map(field => {
            const holder = document.createElement('div');
            holder.className = 'ip-form-input';
            const label = document.createElement('label');
            label.textContent = field.name;
            const input = document.createElement('input');
            input.type = 'text';
            input.autocomplete = 'off';
            input.className = 'js-provider-attribute';
            input.dataset.attributeCode = field.code;
            input.dataset.attributeLabel = field.name;
            holder.append(label, input);
            this.refine.querySelector('.ip-form-row').append(holder);
            attachSuggestions(input,
                (attribute, typed, signal) => this.picker.fetchSuggestions(attribute, typed, signal, this.contextOf(input)),
                () => input.dataset.attributeCode, this.picker.modal);
            return input;
        });
        this.refine.classList.toggle('d-none', !this.attributeFields.length);

        form.addEventListener('submit', event => {
            event.preventDefault();
            this.search();
        });
    }

    /**
     * Reads the values held by the other attribute fields, narrowing what an attribute can take.
     * @param {HTMLInputElement} input - The field being completed
     * @returns {Object} The other attribute values, keyed by attribute code
     */
    contextOf(input) {
        const context = {};
        this.attributeFields.forEach(other => {
            const value = other.value.trim();
            if (other !== input && value) {
                context[other.dataset.attributeCode] = value;
            }
        });
        return context;
    }

    /**
     * Puts the flow back to its resting state, for the next opening.
     */
    reset() {
        this.form.reset();
        this.refine.open = false;
        this.resultsContainer.replaceChildren();
        this.list = null;
        this.addButton = null;
    }

    /**
     * Returns the filled criteria, both as request parameters and as labelled pairs for the recap.
     * @returns {Object} params plus the [label, value] pairs
     */
    readCriteria() {
        const labels = this.picker.labels;
        const criteria = [
            ['search_lastname', labels.lastname, this.lastname.value],
            ['search_givenname', labels.givenname, this.givenname.value],
            ['search_email', labels.email, this.email.value]
        ];
        this.attributeFields.forEach(input => {
            criteria.push(['provider_attribute_' + input.dataset.attributeCode, input.dataset.attributeLabel, input.value]);
        });

        const params = new URLSearchParams();
        const pairs = [];
        criteria.filter(([, , value]) => value.trim()).forEach(([name, label, value]) => {
            params.append(name, value.trim());
            pairs.push([label, value.trim()]);
        });
        return { params, pairs };
    }

    /**
     * Runs the search, then moves to the results screen.
     */
    async search() {
        const labels = this.picker.labels;
        const { params, pairs } = this.readCriteria();
        if (!pairs.length) {
            this.picker.showMessage(labels.criteriaRequired, 'info');
            return;
        }

        this.picker.removeMessage();
        this.picker.showLoading(labels.searching);
        try {
            const outcome = await this.picker.call('search', params);
            if (!outcome.ok) {
                this.picker.showMessage(labels.error + ' (' + outcome.status + ')', 'error');
                return;
            }
            this.displayResults(outcome.data, pairs);
            this.picker.showResultsView();
        } catch (e) {
            this.picker.showMessage(labels.error, 'error');
        } finally {
            this.picker.hideLoading();
        }
    }

    /**
     * Builds the results screen: the criteria recalled as tags, the rows, and the actions.
     * @param {Object} data - The search response
     * @param {Array} pairs - The [label, value] criteria the search ran on
     */
    displayResults(data, pairs) {
        const labels = this.picker.labels;
        const container = this.resultsContainer;
        container.replaceChildren();
        container.append(this.criteriaBlock(pairs, data));

        const truncated = document.createElement('div');
        truncated.className = 'ip-info-message warning d-none';
        truncated.id = 'permResultsTruncated';
        container.append(truncated);

        if (!data.results.length) {
            const none = document.createElement('div');
            none.className = 'ip-info-message info';
            const main = document.createElement('p');
            main.className = 'ip-info-message-main';
            main.textContent = labels.noResults;
            none.append(main);
            container.append(none);
            this.list = null;
        } else {
            if (data.total > data.results.length) {
                truncated.textContent = labels.resultsLimited.replace('%s', data.results.length);
                truncated.classList.remove('d-none');
            }

            this.list = document.createElement('ul');
            this.list.className = 'ip-results-list';
            this.list.id = 'permResults';
            data.results.forEach(user => this.list.append(this.row(user)));
            this.list.addEventListener('click', event => {
                const line = event.target.closest('li');
                const box = line ? line.querySelector('input[type="checkbox"]') : null;
                if (!box) {
                    return;
                }
                if (event.target !== box) {
                    box.checked = !box.checked;
                }
                this.refreshAddButton();
            });
            container.append(this.list);
        }

        const buttons = document.createElement('div');
        buttons.className = 'ip-container-buttons';
        const cancel = document.createElement('button');
        cancel.type = 'button';
        cancel.className = 'ip-button-light';
        cancel.textContent = labels.cancel;
        cancel.addEventListener('click', () => this.picker.closeModal());
        this.addButton = document.createElement('button');
        this.addButton.type = 'button';
        this.addButton.id = 'permAddButton';
        this.addButton.addEventListener('click', () => this.grant());
        buttons.append(cancel, this.addButton);
        container.append(buttons);
        this.refreshAddButton();
    }

    /**
     * Recalls the criteria the results answer to, as tags above the list.
     * @param {Array} pairs - The [label, value] criteria
     * @param {Object} data - The search response, for the total
     * @returns {HTMLElement} The recap block
     */
    criteriaBlock(pairs, data) {
        const labels = this.picker.labels;
        const block = document.createElement('div');
        block.className = 'ip-search-criteria';
        const title = document.createElement('p');
        title.textContent = `${data.total} ${labels.results}`;
        const tags = document.createElement('div');
        tags.className = 'ip-criteria-tags';
        pairs.forEach(([label, value]) => {
            const tag = document.createElement('span');
            tag.className = 'ip-tag-criteria';
            const strong = document.createElement('strong');
            strong.textContent = label + ' :';
            tag.append(strong, document.createTextNode(' ' + value));
            tags.append(tag);
        });
        block.append(title, tags);
        return block;
    }

    /**
     * Renders one result row, a checkbox on the selectable ones only. Rows carry no listener of
     * their own: one on the list serves them all.
     * @param {Object} user - One directory search result
     * @returns {HTMLLIElement} The row
     */
    row(user) {
        const labels = this.picker.labels;
        const line = document.createElement('li');
        line.className = 'ip-result-item';

        const left = document.createElement('div');
        left.className = 'ip-result-left';
        const header = document.createElement('div');
        header.className = 'ip-result-header';
        const name = document.createElement('h3');
        const strLast = (user.lastName || '').toUpperCase();
        const strFirst = toTitleCase(user.firstName || '');
        name.textContent = [strLast, strFirst].filter(Boolean).join(' ') || user.displayName;
        header.append(name);
        left.append(header);

        if (user.email) {
            const details = document.createElement('div');
            details.className = 'ip-result-details';
            const mail = document.createElement('p');
            mail.textContent = user.email;
            details.append(mail);
            left.append(details);
        }

        const right = document.createElement('div');
        right.className = 'ip-result-right';
        if (user.alreadyGranted) {
            const tag = document.createElement('span');
            tag.className = 'ip-tag-criteria';
            tag.textContent = labels.alreadyGranted;
            right.append(tag);
        } else {
            const box = document.createElement('input');
            box.type = 'checkbox';
            box.value = user.guid;
            right.append(box);
        }

        line.append(left, right);
        return line;
    }

    /**
     * Returns the checkboxes of the selectable rows.
     * @returns {Array} The checkboxes
     */
    boxes() {
        return this.list ? [...this.list.querySelectorAll('input[type="checkbox"]')] : [];
    }

    /**
     * Enables the add button and shows how many people are selected.
     */
    refreshAddButton() {
        if (!this.addButton) {
            return;
        }
        const labels = this.picker.labels;
        const count = this.boxes().filter(b => b.checked).length;
        this.addButton.disabled = count === 0;
        this.addButton.textContent = count === 0 ? labels.add : `${labels.add} (${count})`;
    }

    /**
     * Grants the permission to every selected person, an in-modal dialog confirming a large batch.
     */
    grant() {
        const labels = this.picker.labels;
        const selected = this.boxes().filter(b => b.checked).map(b => b.value);
        if (!selected.length) {
            return;
        }
        if (selected.length >= CONFIRM_THRESHOLD) {
            this.picker.showConfirmDialog(labels.confirmBulk.replace('{0}', selected.length), () => this.doGrant(selected));
        } else {
            this.doGrant(selected);
        }
    }

    /**
     * Sends the grant, then reloads the page on the permission panel.
     * @param {Array} selected - The guids to grant
     */
    async doGrant(selected) {
        const labels = this.picker.labels;
        const params = new URLSearchParams();
        selected.forEach(guid => params.append('user_guid', guid));

        this.addButton.disabled = true;
        try {
            const outcome = await this.picker.call('grant', params);
            if (!outcome.ok) {
                this.picker.showMessage(labels.error + ' (' + outcome.status + ')', 'error');
                this.addButton.disabled = false;
                return;
            }
            this.picker.reloadOnPanel();
        } catch (e) {
            this.picker.showMessage(labels.error, 'error');
            this.addButton.disabled = false;
        }
    }
}
