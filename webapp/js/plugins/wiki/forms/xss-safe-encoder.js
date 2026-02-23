(function() {
    'use strict';

    /**
     * Encode string to URL-safe Base64 (RFC 4648 section 5).
     * Uses - instead of + and _ instead of / to avoid issues
     * with application/x-www-form-urlencoded form submissions.
     * @throws Error if encoding fails
     */
    function encodeToBase64(str) {
        if (!str) return '';
        try {
            return btoa(unescape(encodeURIComponent(str)))
                .replace(/\+/g, '-')
                .replace(/\//g, '_');
        } catch (e) {
            console.error('Error encoding to Base64:', e);
            throw new Error('Failed to encode content safely. Please remove special characters and try again.');
        }
    }

    /**
     * Process form before submission
     */
    function processFormSubmission(form, e) {
        const fieldsToEncode = ['title', 'description', 'content', 'comment'];

        fieldsToEncode.forEach(fieldName => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            if (field && field.value) {
                const existingHidden = form.querySelector(`[name="${fieldName}_encoded"]`);
                if (existingHidden) {
                    existingHidden.remove();
                }

                const encoded = encodeToBase64(field.value);
                const hiddenField = document.createElement('input');
                hiddenField.type = 'hidden';
                hiddenField.name = fieldName + '_encoded';
                hiddenField.value = encoded;
                form.appendChild(hiddenField);

                field.value = '';
            }
        });
    }

    /**
     * Initialize form handlers
     */
    function init() {
        const forms = document.querySelectorAll('form.xss-safe-form');

        forms.forEach(form => {
            form.addEventListener('submit', function(e) {
                try {
                    processFormSubmission(this, e);
                } catch (error) {
                    e.preventDefault();
                    alert(error.message);
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
