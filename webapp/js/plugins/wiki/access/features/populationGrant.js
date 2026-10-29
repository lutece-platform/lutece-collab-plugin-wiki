import { debounce } from '../utils/utils.js';
import { attachSuggestions } from '../utils/suggest.js';

const COUNT_DEBOUNCE = 350;

/**
 * Population form of the search screen: points a directory attribute value, says how many agents
 * it covers right now, and grants the permission to the whole population. A population such as a
 * direction changes as people join and leave, so listing its members would go stale the day it is
 * written.
 */
export default class PopulationGrant {
    /**
     * Creates the population form.
     * @param {Object} picker - The parent access picker
     */
    constructor(picker) {
        this.picker = picker;
        this.countSequence = 0;
    }

    /**
     * Renders the form and wires its behaviour.
     * @param {HTMLFormElement} form - The form element inside the search screen
     */
    render(form) {
        const labels = this.picker.labels;
        this.form = form;
        form.innerHTML = `
            <div class="ip-input-group">
                <label for="permPopAttribute">${labels.criterion}</label>
                <select id="permPopAttribute"></select>
            </div>
            <div class="ip-input-group">
                <label for="permPopValue">${labels.criterionValue}</label>
                <input type="text" id="permPopValue" autocomplete="off">
            </div>
            <div class="ip-info-message success d-none" id="permPopSummary">
                <p class="ip-info-message-main" id="permPopCount"></p>
                <p class="ip-info-message-description">${labels.populationHint}</p>
            </div>
            <div class="ip-container-buttons">
                <button type="button" class="ip-button-light ip-button-close">${labels.cancel}</button>
                <button type="submit" id="permPopulationButton" disabled>${labels.grantPopulation}</button>
            </div>
        `;

        this.attribute = form.querySelector('#permPopAttribute');
        this.value = form.querySelector('#permPopValue');
        this.summary = form.querySelector('#permPopSummary');
        this.count = form.querySelector('#permPopCount');
        this.button = form.querySelector('#permPopulationButton');

        this.picker.fields.forEach(field => {
            const option = document.createElement('option');
            option.value = field.code;
            option.textContent = field.name;
            this.attribute.append(option);
        });
        this.value.placeholder = labels.criterionPlaceholder;

        attachSuggestions(this.value,
            (attribute, typed, signal) => this.picker.fetchSuggestions(attribute, typed, signal),
            () => this.attribute.value, this.picker.modal);

        const debouncedCount = debounce(() => this.countPopulation(), COUNT_DEBOUNCE);
        this.attribute.addEventListener('change', () => {
            this.value.value = '';
            this.countPopulation();
        });
        this.value.addEventListener('change', () => this.countPopulation());
        this.value.addEventListener('input', () => {
            this.button.disabled = true;
            this.summary.classList.add('d-none');
            debouncedCount();
        });
        form.addEventListener('submit', event => {
            event.preventDefault();
            this.grant();
        });
    }

    /**
     * Puts the form back to its resting state, for the next opening.
     */
    reset() {
        this.form.reset();
        this.summary.classList.add('d-none');
        this.button.disabled = true;
        this.button.textContent = this.picker.labels.grantPopulation;
    }

    /**
     * Describes the population the form is pointing at, or null when it is incomplete.
     * @returns {Object|null} attribute, label and value
     */
    chosenPopulation() {
        const value = this.value.value.trim();
        if (!value) {
            return null;
        }
        const option = this.attribute.options[this.attribute.selectedIndex];
        return { attribute: this.attribute.value, label: option ? option.textContent.trim() : this.attribute.value, value };
    }

    /**
     * Says how many agents the chosen criterion covers right now.
     *
     * The server resolves what was typed to the exact directory spelling and counts on that alone:
     * counting what someone half typed would show a number that does not match what the rule would
     * grant. Nothing shows until a count is obtained — an intermediate state would flash on every
     * keystroke that resolves to no value. Answers of a value the user already moved past are
     * dropped, so a slow reply never overwrites the current one.
     */
    async countPopulation() {
        const labels = this.picker.labels;
        const population = this.chosenPopulation();
        const sequence = ++this.countSequence;

        this.button.disabled = true;
        this.button.textContent = labels.grantPopulation;

        if (!population) {
            this.summary.classList.add('d-none');
            return;
        }

        try {
            const measure = await this.picker.fetchPopulationCount(population.attribute, population.value);
            if (sequence !== this.countSequence) {
                return;
            }

            if (!measure.canonical || !measure.count) {
                this.summary.classList.add('d-none');
                return;
            }
            if (measure.canonical !== this.value.value) {
                this.value.value = measure.canonical;
            }

            this.count.textContent = labels.matchingPopulation.replace('%s', measure.count);
            this.summary.classList.remove('d-none');
            this.button.textContent = `${labels.grantPopulation} (${measure.count})`;
            this.button.disabled = false;
        } catch (e) {
            if (sequence === this.countSequence) {
                this.summary.classList.add('d-none');
                this.picker.showMessage(labels.error, 'error');
            }
        }
    }

    /**
     * Grants the permission to the whole population, so nobody has to maintain a list of its
     * members, then reloads the page on the permission panel.
     */
    async grant() {
        const labels = this.picker.labels;
        const population = this.chosenPopulation();
        if (!population || this.button.disabled) {
            return;
        }
        const params = new URLSearchParams({ attribute: population.attribute, value: population.value, label: population.label });
        this.button.disabled = true;
        try {
            const outcome = await this.picker.call('grant-attribute', params);
            if (!outcome.ok) {
                this.count.textContent = labels.populationRefused;
                this.button.disabled = false;
                return;
            }
            this.picker.reloadOnPanel();
        } catch (e) {
            this.count.textContent = labels.error;
            this.button.disabled = false;
        }
    }
}
