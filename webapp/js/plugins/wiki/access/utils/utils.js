/**
 * Converts a string to title case.
 * @param {string} value - The string to convert
 * @returns {string} The string with each word capitalized
 */
export function toTitleCase(value) {
    return (value || '').toLowerCase().replace(/(^|[\s-'])\S/g, c => c.toUpperCase());
}

/**
 * Delays a call until the caller has stayed quiet for a while, so a keystroke burst costs one call.
 * @param {Function} fn - The function to call
 * @param {number} ms - How long the caller must stay quiet
 * @returns {Function} The debounced function
 */
export function debounce(fn, ms) {
    let timer = null;
    return (...args) => {
        window.clearTimeout(timer);
        timer = window.setTimeout(() => fn(...args), ms);
    };
}
