import { debounce } from './utils.js';

const SUGGESTION_DEBOUNCE = 220;

/**
 * Turns a text field into a suggested-value field. Directory values are messy — spelling variants,
 * abbreviations, accents — so the list is the only way a user can find the value that actually
 * exists. The field stays free text: an unlisted value can still be typed.
 *
 * The list is fixed and appended to the overlay rather than next to the field: the modal content
 * scrolls and carries a transform, either of which would clip or shift an in-place list. It flips
 * above the field when the space below is too short. Hand-rolled rather than TomSelect (already
 * vendored by the plugin) because the picker lives in a shadow root, where TomSelect's global
 * stylesheet and body-anchored dropdown do not reach.
 *
 * @param {HTMLInputElement} input - The field to complete
 * @param {Function} fetchValues - (attribute, typed, signal) resolving to the matching values
 * @param {Function} attributeOf - Returns the attribute the field is currently bound to
 * @param {HTMLElement} overlay - The untransformed element carrying the list
 */
export function attachSuggestions(input, fetchValues, attributeOf, overlay) {
    const list = document.createElement('div');
    list.className = 'js-suggestions d-none';
    list.dataset.suggestFor = input.dataset.attributeCode || input.id;
    overlay.appendChild(list);

    let active = -1;
    let pending = null;

    /** Anchors the list on the field, opening upward when the space below is too short. */
    const place = () => {
        const rect = input.getBoundingClientRect();
        const below = window.innerHeight - rect.bottom - 12;
        const above = rect.top - 12;
        const openDown = below >= 160 || below >= above;

        list.style.left = `${rect.left}px`;
        list.style.width = `${rect.width}px`;
        list.style.maxHeight = `${Math.max(120, Math.min(240, openDown ? below : above))}px`;

        if (openDown) {
            list.style.top = `${rect.bottom + 2}px`;
            list.style.bottom = 'auto';
        } else {
            list.style.top = 'auto';
            list.style.bottom = `${window.innerHeight - rect.top + 2}px`;
        }
    };

    /**
     * Re-anchors the list while it is open: the modal recenters itself whenever its content grows
     * or shrinks, and a list left where the field used to be would cover it.
     */
    const reposition = () => {
        if (!list.classList.contains('d-none')) {
            place();
        }
    };

    /** Empties and hides the list. */
    const close = () => {
        list.classList.add('d-none');
        list.replaceChildren();
        active = -1;
    };

    /** Marks the row the keyboard is on. */
    const highlight = () => {
        [...list.children].forEach((row, i) => row.classList.toggle('active', i === active));
    };

    /** Carries a picked value into the field, as if it had been typed. */
    const choose = value => {
        input.value = value;
        close();
        input.dispatchEvent(new Event('input', { bubbles: true }));
    };

    /** Renders the offered values, or closes when there is nothing to offer. */
    const render = values => {
        const typed = input.value.trim().toLowerCase();
        if (!values.length || (values.length === 1 && values[0].toLowerCase() === typed)) {
            close();
            return;
        }
        list.replaceChildren(...values.map(value => {
            const row = document.createElement('button');
            row.type = 'button';
            row.textContent = value;
            row.addEventListener('mousedown', event => {
                event.preventDefault();
                choose(value);
            });
            return row;
        }));
        active = -1;
        list.classList.remove('d-none');
        place();
    };

    /** Fetches after a quiet delay, and drops the answer of a keystroke the user moved past. */
    const open = debounce(async () => {
        if (pending) {
            pending.abort();
        }
        pending = new AbortController();
        const asked = input.value;
        try {
            const values = await fetchValues(attributeOf(), asked, pending.signal);
            if (input.value === asked) {
                render(values);
            }
        } catch (e) {
            close();
        }
    }, SUGGESTION_DEBOUNCE);

    overlay.addEventListener('scroll', reposition, true);
    window.addEventListener('resize', reposition);
    new ResizeObserver(reposition).observe(overlay.querySelector('.ip-modal-content') || overlay);

    input.addEventListener('input', open);
    input.addEventListener('focus', open);
    input.addEventListener('blur', () => window.setTimeout(close, 120));
    input.addEventListener('keydown', event => {
        const rows = list.children.length;
        if (list.classList.contains('d-none') || !rows) {
            return;
        }
        if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
            event.preventDefault();
            active = (active + (event.key === 'ArrowDown' ? 1 : -1) + rows) % rows;
            highlight();
            list.children[active].scrollIntoView({ block: 'nearest' });
        } else if (event.key === 'Enter' && active >= 0) {
            event.preventDefault();
            event.stopPropagation();
            choose(list.children[active].textContent);
        } else if (event.key === 'Escape') {
            event.stopPropagation();
            close();
        }
    });
}
