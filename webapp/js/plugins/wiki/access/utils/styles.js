/**
 * Stylesheet of the access picker, injected into its shadow root. Taken from the identitypicker
 * plugin so the modal carries the same design, screens and animations; only the parts its screens
 * use are kept, plus the suggestion dropdown of the attribute fields.
 */
export const pickerStyles = `
:host {
  --ip-bg: #fff;
  --ip-text: #0d0c22;
  --ip-text-extra-light: #444444;
  --ip-close-hover: #000;
  --ip-border: #dee2e6;
  --ip-input-focus: rgba(36, 36, 36, 0.534);
  --ip-input-box-shadow: 0px 4px 4px 0px rgba(6, 3, 24, 0.03);
  --ip-input-disabled: #f5f5f5;
  --ip-button-bg: #000000;
  --ip-button-text: #ffffff;
  --ip-button-hover: #565564;
  --ip-button-close-bg: #eef0f1;
  --ip-button-close-text: #0d0c22;
  --ip-button-close-hover: #eef0f1;
  --ip-button-disabled: #f5f5f5;
  --ip-button-text-disabled: #c0c0c0;
  --ip-result-details-color: #555;
  --ip-result-item-hover-bg: #f1f7f8;
  --ip-table-td-hover-bg: #fcfcfc;
}

* { color: inherit; }
*, *::before, *::after { box-sizing: border-box; }
.d-none { display: none !important; }

.ip-modal {
  display: none;
  position: fixed;
  z-index: 1050;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  transition: background-color 0.3s ease;
  justify-content: center;
  align-items: center;
  font-size: 15px;
  line-height: 1.5;
  color: var(--ip-text);
}

.ip-modal-content {
  display: flex;
  flex-direction: column;
  background-color: var(--ip-bg);
  color: var(--ip-text);
  padding: 0px;
  border-radius: 30px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  transform: scale(0.9);
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55), width 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  position: relative;
}

.ip-modal-content.wide-view { max-width: 900px; }

.ip-close, .ip-back { transition: transform 0.2s ease; }

.ip-modal-open .ip-modal-content {
  transform: scale(1);
  opacity: 1;
}

.ip-header {
  position: sticky;
  top: 0;
  background-color: var(--ip-bg);
  z-index: 10;
  flex-shrink: 0;
}

.ip-header .ip-header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24px 40px 0px;
  border-bottom: 1px solid var(--ip-border);
  padding-bottom: 24px;
}

.ip-back {
  font-size: 24px;
  cursor: pointer;
  color: #333;
  transition: transform 0.2s ease;
}

.ip-back:hover { transform: translateX(-3px); }

.ip-modal h2 {
  color: var(--ip-text);
  margin: 0;
  font-weight: 800;
  font-size: 20px;
}

.ip-main-container {
  display: flex;
  flex-grow: 1;
  overflow: hidden;
}

.ip-scrollable-content {
  flex-grow: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(155, 155, 155, 0.5) transparent;
  min-width: 0;
}

.ip-scrollable-content::-webkit-scrollbar { width: 8px; }
.ip-scrollable-content::-webkit-scrollbar-thumb { background-color: rgba(155, 155, 155, 0.5); }

.ip-content-area { padding: 24px 40px 32px; }

.ip-modal button,
.ip-modal .ip-button {
  background-color: var(--ip-button-bg);
  color: var(--ip-button-text);
  padding: 12px 20px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  letter-spacing: 0.5px;
  overflow: hidden;
  border: none;
  position: relative;
}

.ip-modal button:disabled,
.ip-modal .ip-button:disabled {
  background-color: var(--ip-button-disabled);
  color: var(--ip-button-text-disabled);
  cursor: not-allowed;
}

.ip-modal .ip-button-light {
  background-color: var(--ip-button-close-bg);
  color: var(--ip-button-close-text);
}

.ip-modal .ip-button-light:hover { background-color: var(--ip-button-close-hover); }

.ip-modal button:hover:not(:disabled),
.ip-modal .ip-button:hover:not(:disabled) {
  background-color: var(--ip-button-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.ip-modal .ip-button-light:hover:not(:disabled) { background-color: var(--ip-button-close-hover); color: var(--ip-button-close-text); }

.ip-modal button:active { transform: translateY(0); box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2); }

.ip-modal button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%);
  transform-origin: 50% 50%;
}

.ip-modal button:focus:not(:active)::after,
.ip-modal .ip-button:focus:not(:active)::after {
  animation: ripple 1s ease-out;
}

.ip-modal .ip-button-rounded {
  border-radius: 30px;
  height: 30px;
  width: 30px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes ripple {
  0% { transform: scale(0, 0); opacity: 1; }
  20% { transform: scale(25, 25); opacity: 1; }
  100% { opacity: 0; transform: scale(40, 40); }
}

.ip-search-option {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 15px;
}

.ip-search-option label {
  display: flex;
  align-items: center;
  width: 250px;
  padding: 10px 15px;
  border: 1px solid var(--ip-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ip-search-option label:hover { border-color: var(--ip-input-focus); }

.ip-search-option input[type="radio"] {
  margin-right: 10px;
  margin-top: 0;
  margin-bottom: 0;
  align-self: center;
}

.ip-search-option .ip-mode-text { display: block; line-height: 1.4; }

.ip-search-option .ip-mode-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.ip-search-option .ip-mode-help {
  display: block;
  font-size: 12.5px;
  color: var(--ip-text-extra-light);
  margin-top: 4px;
}

.ip-search-option input[type="radio"]:checked ~ .ip-mode-text .ip-mode-title { font-weight: 800; }

.ip-search-option label:has(input[type="radio"]:checked) {
  border-color: var(--ip-input-focus);
  box-shadow: var(--ip-input-box-shadow);
}

.ip-modal input[type="text"],
.ip-modal input[type="email"],
.ip-modal input[type="date"],
.ip-modal select {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: 1px solid var(--ip-border);
  border-radius: 8px;
  background-color: var(--ip-bg);
  color: var(--ip-text);
  transition: all 0.3s ease;
}

.ip-modal input[type="text"]:disabled,
.ip-modal select:disabled {
  background-color: var(--ip-input-disabled);
  cursor: not-allowed;
}

.ip-modal input:focus,
.ip-modal select:focus {
  outline: none !important;
  border-color: var(--ip-input-focus);
  box-shadow: var(--ip-input-box-shadow);
}

.ip-modal input:hover,
.ip-modal select:hover { border-color: var(--ip-input-focus); }

.ip-input-group { margin-bottom: 15px; position: relative; }

.ip-input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
}

.ip-container-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.ip-fieldset {
  border: 1px solid var(--ip-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

details.ip-fieldset > summary {
  cursor: pointer;
  font-weight: 700;
  font-size: 16px;
}

details.ip-fieldset > summary:hover { color: var(--ip-close-hover); }
details.ip-fieldset[open] > summary { margin-bottom: 16px; }
details.ip-fieldset > .ip-form-row { margin-bottom: 0; }

.ip-form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.ip-form-input {
  flex: 1 1 calc(50% - 10px);
  min-width: 200px;
  position: relative;
}

.ip-form-input label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.ip-info-message {
  padding: 12px 40px 12px 16px;
  margin-bottom: 20px;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.ip-info-message-main { margin: 0; font-weight: bold; }
.ip-info-message-description { font-size: 12px; margin: 0; }

.ip-info-message.info {
  background-color: #e3f2fd;
  color: #0d47a1;
  border-left: 4px solid #2196f3;
}

.ip-info-message.error {
  background-color: #ffebee;
  color: #b71c1c;
  border-left: 4px solid #f44336;
}

.ip-info-message.success {
  background-color: #e8f5e9;
  color: #1b5e20;
  border-left: 4px solid #4caf50;
}

.ip-info-message.warning {
  background-color: #fff3cd;
  color: #664d03;
  border-left: 4px solid #ffc107;
}

.ip-info-message:hover { transform: translateY(-2px); }

.ip-info-message-close {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: inherit;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.ip-info-message-close:hover { opacity: 1; }

.ip-modal .ip-info-message-close { background: none; padding: 0 6px; border-radius: 0; }
.ip-modal .ip-info-message-close:hover { background: none; transform: translateY(-50%); box-shadow: none; }
.ip-modal .ip-info-message-close::after { content: none; }

.ip-results-list {
  list-style-type: none;
  padding: 0;
  margin: 0 0 20px 0;
  border: 1px solid var(--ip-border);
  border-radius: 8px;
  overflow: hidden;
}

.ip-result-item {
  padding: 20px;
  margin: 0;
  border-bottom: 1px solid var(--ip-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.3s ease;
}

.ip-result-item:last-child { border-bottom: none; }

.ip-result-item:hover {
  background-color: var(--ip-table-td-hover-bg);
  cursor: pointer;
}

.ip-result-left {
  flex: 1;
  margin-right: 1rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.ip-result-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.ip-result-right input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: var(--ip-button-bg);
  cursor: pointer;
}

.ip-result-header,
.ip-result-details {
  width: 100%;
  text-align: left;
}

.ip-result-header h3 {
  color: var(--ip-text);
  margin: 0;
  font-size: 14px !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: none;
}

.ip-result-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--ip-result-details-color);
  font-size: 13px;
}

.ip-result-details p { margin: 8px 0 0; line-height: 1.4; }

.ip-search-criteria {
  margin-bottom: 20px;
  padding: 10px 15px;
  background-color: var(--ip-bg);
  border: 1px solid var(--ip-border);
  border-radius: 8px;
}

.ip-search-criteria p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: var(--ip-text);
}

.ip-criteria-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ip-tag-criteria {
  background-color: var(--ip-bg);
  border: 1px solid var(--ip-border);
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 13px;
  color: var(--ip-text);
}

.ip-tag-criteria strong { margin-right: 4px; }

.ip-search-container,
.ip-results-container {
  transition: opacity 0.3s ease, transform 0.3s ease;
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
}

.ip-search-container.active,
.ip-results-container.active {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.ip-result-item {
  animation: fadeIn 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

.ip-result-item:nth-child(1) { animation-delay: 0.1s; }
.ip-result-item:nth-child(2) { animation-delay: 0.2s; }
.ip-result-item:nth-child(3) { animation-delay: 0.3s; }
.ip-result-item:nth-child(4) { animation-delay: 0.4s; }
.ip-result-item:nth-child(5) { animation-delay: 0.5s; }
.ip-result-item:nth-child(6) { animation-delay: 0.6s; }
.ip-result-item:nth-child(7) { animation-delay: 0.7s; }
.ip-result-item:nth-child(8) { animation-delay: 0.8s; }
.ip-result-item:nth-child(9) { animation-delay: 0.9s; }
.ip-result-item:nth-child(10) { animation-delay: 1s; }

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.ip-modal button:focus { animation: pulse 0.5s ease-in-out; }

.ip-loading-container {
  display: none;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  justify-content: center;
  align-items: center;
  z-index: 1000;
  flex-direction: column;
  opacity: 0;
  transform: scale(0.95);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.ip-loading-container.show {
  opacity: 1;
  transform: scale(1);
}

.ip-loader {
  width: 40px;
  height: 40px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #000000;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.ip-loading-message {
  margin-top: 20px;
  font-size: 16px;
  color: #333;
  text-align: center;
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.3s ease 0.1s, transform 0.3s ease 0.1s;
}

.ip-loading-container.show .ip-loading-message {
  opacity: 1;
  transform: translateY(0);
}

.ip-confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.ip-confirm-dialog {
  padding: 30px;
  max-width: 400px;
  width: 90%;
}

.ip-confirm-dialog h2 { margin: 0 0 10px 0; }
.ip-confirm-dialog p { margin: 0 0 20px 0; }

.ip-confirm-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@keyframes slideDownWithShadow {
  from {
    transform: translateY(-20px);
    opacity: 0;
    box-shadow: 0 0 0 rgba(0, 0, 0, 0);
  }
  to {
    transform: translateY(0);
    opacity: 1;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.js-suggestions {
  position: fixed;
  z-index: 1090;
  display: flex;
  flex-direction: column;
  background-color: var(--ip-bg);
  border: 1px solid var(--ip-border);
  border-radius: 8px;
  overflow-x: hidden;
  overflow-y: auto;
  transform-origin: top center;
  animation: slideDownWithShadow 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards;
  scrollbar-width: thin;
  scrollbar-color: rgba(155, 155, 155, 0.5) transparent;
}

.js-suggestions.d-none { animation: none; }
.js-suggestions::-webkit-scrollbar { width: 8px; }
.js-suggestions::-webkit-scrollbar-thumb { background-color: rgba(155, 155, 155, 0.5); }

.ip-modal .js-suggestions button {
  flex-shrink: 0;
  background: none;
  color: var(--ip-text);
  border: none;
  border-radius: 0;
  text-align: left;
  padding: 10px 12px;
  font-size: 14px;
  font-weight: 400;
  letter-spacing: normal;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ip-modal .js-suggestions button:hover,
.ip-modal .js-suggestions button.active {
  background-color: var(--ip-result-item-hover-bg);
  transform: none;
  box-shadow: none;
}

.ip-modal .js-suggestions button::after { content: none; }

@media (max-width: 640px) {
  .ip-search-option { flex-direction: column; }
  .ip-search-option label { width: 100%; }
  .ip-header .ip-header-container { margin: 20px 24px 0; padding-bottom: 18px; }
  .ip-content-area { padding: 20px 24px 24px; }
}
`;
