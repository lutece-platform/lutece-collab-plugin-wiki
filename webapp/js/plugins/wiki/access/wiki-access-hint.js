(function() {
    'use strict';

    const box = document.getElementById('wikiAccess');
    const select = document.getElementById('viewRole');
    if (!box || !select) return;

    const published = document.getElementById('isPublished');
    const hint = document.getElementById('wikiAccessHint');
    const roleLabel = document.getElementById('wikiAccessRole');
    const icon = document.getElementById('wikiAccessIcon');
    const label = box.dataset;
    const steps = Array.from(box.querySelectorAll('.wiki-access-step:not(.wiki-access-current)'));
    const restricting = steps.filter(step => step.dataset.restricted === 'true').map(step => step.dataset.title);
    const unpublished = steps.filter(step => step.dataset.published === 'false').map(step => step.dataset.title);

    /**
     * Joins titles as « A », « B » and « C ».
     * @param {Array<string>} titles the titles
     * @returns {string} the joined titles
     */
    function names(titles) {
        const quoted = titles.map(title => '«\u00a0' + title + '\u00a0»');
        return quoted.length < 2 ? quoted.join('') : quoted.slice(0, -1).join(', ') + ' ' + label.labelAnd + ' ' + quoted[quoted.length - 1];
    }

    /**
     * Fills a message pattern.
     * @param {string} pattern the pattern, {0} for the parents, {1} for the item
     * @param {Array<string>} titles the parent titles
     * @returns {string} the message
     */
    function format(pattern, titles) {
        return pattern.replace('{0}', names(titles)).replace('{1}', label.noun);
    }

    /**
     * Returns the message that explains who can see the item.
     * @param {boolean} restricted true when the item adds its own restriction
     * @returns {string} the message
     */
    function message(restricted) {
        if (published && !published.checked) return format(label.labelUnpublished, []);
        if (unpublished.length) return format(label.labelUnpublishedParent, unpublished);
        if (restricted && restricting.length) return format(label.labelBoth, restricting);
        if (restricted) return format(label.labelRestricted, []);
        if (restricting.length) return format(label.labelInherited, restricting);
        return format(label.labelPublic, []);
    }

    /**
     * Updates the role, the icon and the message of the item.
     */
    function render() {
        const option = select.options[select.selectedIndex];
        const restricted = select.value !== 'none';
        hint.textContent = message(restricted);
        if (roleLabel) {
            roleLabel.textContent = option ? option.dataset.short : '';
        }
        if (icon) icon.className = 'ti ti-' + (restricted ? 'lock' : 'world');
    }

    const editSelect = document.getElementById('editRole');
    const editRoleLabel = document.getElementById('wikiEditRole');

    /**
     * Shows the selected edit role on the item of the edit path.
     */
    function renderEdit() {
        const option = editSelect.options[editSelect.selectedIndex];
        editRoleLabel.textContent = option ? option.dataset.short : '';
    }

    select.addEventListener('change', render);
    if (published) published.addEventListener('change', render);
    render();
    if (editSelect && editRoleLabel) {
        editSelect.addEventListener('change', renderEdit);
        renderEdit();
    }
})();
