/**
 * User permission panel of the item modification page: searches the user directory and grants a
 * permission to several users at once through the wiki permission REST endpoints, without leaving
 * the page. Reads its configuration and labels from the data attributes of #addUsersModal.
 */
(() => {
    const modalElement = document.getElementById('addUsersModal');
    if (!modalElement) {
        return;
    }

    const REST = 'rest/wiki/permission';
    const CODE = modalElement.dataset.code;
    let token = modalElement.dataset.token;
    const CONFIRM_THRESHOLD = 25;
    const SCROLL_FLAG = 'wikiScrollToUserPermissions';
    const LABELS = {
        noResults: modalElement.dataset.labelNoResults,
        criteriaRequired: modalElement.dataset.labelCriteriaRequired,
        alreadyGranted: modalElement.dataset.labelAlreadyGranted,
        results: modalElement.dataset.labelResults,
        error: modalElement.dataset.labelError,
        confirmBulk: modalElement.dataset.labelConfirmBulk,
        add: modalElement.dataset.labelAdd
    };

    const title = document.getElementById('addUsersModalLabel');
    const results = document.getElementById('permResults');
    const resultsHeader = document.getElementById('permResultsHeader');
    const selectAll = document.getElementById('permSelectAll');
    const feedback = document.getElementById('permSearchFeedback');
    const addButton = document.getElementById('permAddButton');
    let permissionType = 'VIEW';

    /** Returns the checkboxes of the selectable rows. */
    const boxes = () => [...results.querySelectorAll('input[type="checkbox"]:not(:disabled)')];

    /** Enables the add button and shows how many users are selected. */
    const refreshAddButton = () => {
        const count = boxes().filter(b => b.checked).length;
        addButton.disabled = count === 0;
        addButton.textContent = count === 0 ? LABELS.add : `${LABELS.add} (${count})`;
    };

    /** Builds the query string identifying the call: item, permission and the one-shot token. */
    const queryString = () => new URLSearchParams({ code: CODE, permission_type: permissionType, token }).toString();

    /** Calls an endpoint, the payload in the body, and keeps the token issued for the next call. */
    const call = async (path, payload) => {
        const response = await fetch(`${REST}/${path}?${queryString()}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: payload
        });
        if (!response.ok) {
            return { ok: false, status: response.status };
        }
        const data = await response.json();
        if (data.nextToken) {
            token = data.nextToken;
        }
        return { ok: true, data };
    };

    /** Renders one result row of the table. */
    const row = user => {
        const line = document.createElement('tr');
        if (user.alreadyGranted) {
            line.className = 'text-muted';
        }

        const box = document.createElement('input');
        box.type = 'checkbox';
        box.className = 'form-check-input m-0';
        box.value = user.guid;
        box.disabled = user.alreadyGranted;
        box.addEventListener('change', refreshAddButton);

        const cellBox = document.createElement('td');
        cellBox.append(box);

        const cellLastName = document.createElement('td');
        cellLastName.textContent = user.lastName || (user.firstName ? '' : user.displayName);
        if (user.alreadyGranted) {
            const badge = document.createElement('span');
            badge.className = 'badge bg-light text-muted ms-2';
            badge.textContent = LABELS.alreadyGranted;
            cellLastName.append(badge);
        }

        const cellFirstName = document.createElement('td');
        cellFirstName.textContent = user.firstName || '';

        const cellMail = document.createElement('td');
        cellMail.textContent = user.email || '';

        line.append(cellBox, cellLastName, cellFirstName, cellMail);

        if (!user.alreadyGranted) {
            line.style.cursor = 'pointer';
            line.addEventListener('click', event => {
                if (event.target !== box) {
                    box.checked = !box.checked;
                    refreshAddButton();
                }
            });
        }

        return line;
    };

    /** Runs the search and renders the outcome. */
    const search = async () => {
        const params = new URLSearchParams();
        const criteria = [
            ['search_lastname', document.getElementById('permSearchLastname').value],
            ['search_givenname', document.getElementById('permSearchGivenname').value],
            ['search_email', document.getElementById('permSearchEmail').value]
        ];
        document.querySelectorAll('.js-provider-attribute').forEach(input => {
            criteria.push(['provider_attribute_' + input.dataset.attributeCode, input.value]);
        });

        const filled = criteria.filter(([, value]) => value.trim());
        if (!filled.length) {
            results.replaceChildren();
            resultsHeader.classList.add('d-none');
            selectAll.checked = false;
            refreshAddButton();
            feedback.textContent = LABELS.criteriaRequired;
            return;
        }
        filled.forEach(([name, value]) => params.append(name, value.trim()));

        results.replaceChildren();
        resultsHeader.classList.add('d-none');
        selectAll.checked = false;
        feedback.textContent = '…';
        refreshAddButton();

        try {
            const outcome = await call('search', params);
            if (!outcome.ok) {
                feedback.textContent = LABELS.error + ' (' + outcome.status + ')';
                return;
            }
            const data = outcome.data;
            if (!data.results.length) {
                feedback.textContent = LABELS.noResults;
                return;
            }
            feedback.textContent = data.results.length + ' ' + LABELS.results;
            data.results.forEach(user => results.append(row(user)));
            resultsHeader.classList.remove('d-none');
        } catch (e) {
            feedback.textContent = LABELS.error;
        }
    };

    /** Grants the permission to every selected user, then refreshes the page. */
    const grant = async () => {
        const selected = boxes().filter(b => b.checked).map(b => b.value);
        if (!selected.length) {
            return;
        }
        if (selected.length >= CONFIRM_THRESHOLD && !window.confirm(LABELS.confirmBulk.replace('{0}', selected.length))) {
            return;
        }

        const params = new URLSearchParams();
        selected.forEach(guid => params.append('user_guid', guid));

        addButton.disabled = true;
        try {
            const outcome = await call('grant', params);
            if (!outcome.ok) {
                feedback.textContent = LABELS.error + ' (' + outcome.status + ')';
                addButton.disabled = false;
                return;
            }
            window.sessionStorage.setItem(SCROLL_FLAG, '1');
            window.location.reload();
        } catch (e) {
            feedback.textContent = LABELS.error;
            addButton.disabled = false;
        }
    };

    modalElement.addEventListener('show.bs.modal', event => {
        const button = event.relatedTarget;
        if (button) {
            permissionType = button.dataset.permissionType;
            title.textContent = LABELS.add + ' — ' + button.dataset.permissionLabel;
        }
        results.replaceChildren();
        resultsHeader.classList.add('d-none');
        feedback.textContent = '';
        selectAll.checked = false;
        refreshAddButton();
    });

    selectAll.addEventListener('change', () => {
        boxes().forEach(b => { b.checked = selectAll.checked; });
        refreshAddButton();
    });

    /** Puts the view on the permission panel, without animation. */
    const scrollToPanel = () => {
        const target = document.getElementById('userPermissions');
        if (target) {
            target.scrollIntoView({ behavior: 'instant', block: 'start' });
        }
    };

    if (window.sessionStorage.getItem(SCROLL_FLAG) === '1') {
        window.sessionStorage.removeItem(SCROLL_FLAG);
        scrollToPanel();
        window.setTimeout(scrollToPanel, 300);
    }

    document.querySelectorAll('a[href*="action=removeUserPermission"]').forEach(link => {
        link.addEventListener('click', () => window.sessionStorage.setItem(SCROLL_FLAG, '1'));
    });

    document.getElementById('permSearchButton').addEventListener('click', search);
    addButton.addEventListener('click', grant);
    modalElement.addEventListener('keydown', event => {
        if (event.key === 'Enter' && event.target.tagName === 'INPUT') {
            event.preventDefault();
            search();
        }
    });
})();
