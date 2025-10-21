document.addEventListener('DOMContentLoaded', function () {
    const codeInput = document.getElementById('code');
    if (codeInput) {
        const form = codeInput.closest('form');

        codeInput.addEventListener('blur', function () {
            const code = this.value;

            if (!code || code === this.defaultValue) {
                clearError(codeInput);
                return;
            }

            codeInput.dataset.validating = 'true';

            const baseUrl = document.getElementsByTagName('base')[0] ? document.getElementsByTagName('base')[0].href : '';
            const restUrl = baseUrl + 'rest/wiki/validate_code?code=' + encodeURIComponent(code);

            fetch(restUrl)
                .then(response => response.json())
                .then(data => {
                    codeInput.dataset.validating = 'false';
                    if (!data.valid) {
                        showError(codeInput, data.message);
                    } else {
                        clearError(codeInput);
                        codeInput.classList.add('is-valid');
                    }
                })
                .catch(error => {
                    console.error('Error validating code:', error);
                    codeInput.dataset.validating = 'false';
                });
        });

        codeInput.addEventListener('input', function () {
            clearError(codeInput);
            this.classList.remove('is-valid');
        });

        if (form) {
            form.addEventListener('submit', function (e) {
                if (codeInput.classList.contains('is-invalid') || codeInput.dataset.validating === 'true') {
                    e.preventDefault();
                    e.stopPropagation();
                    codeInput.focus();
                }
            });
        }
    }

    function showError(input, message) {
        let errorDiv = document.getElementById('code-error');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.id = 'code-error';
            errorDiv.className = 'invalid-feedback d-block';
            input.parentNode.appendChild(errorDiv);
        }
        errorDiv.textContent = message;
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
    }

    function clearError(input) {
        const errorDiv = document.getElementById('code-error');
        if (errorDiv) {
            errorDiv.remove();
        }
        input.classList.remove('is-invalid');
    }
});
