/**
 * Extension de la Toolbar Milkdown pour supporter les dropdowns
 *
 * Permet d'ajouter des items de toolbar avec des menus déroulants
 * tout en conservant la compatibilité avec l'API Milkdown existante.
 */

const DROPDOWN_CONFIG = {
  GAP: 8,
  VIEWPORT_MARGIN: 16
};

/**
 * Gestionnaire singleton pour les dropdowns de la toolbar
 */
export class ToolbarDropdownManager {
  #openDropdown = null;
  #dropdowns = new Map();
  #ignoreNextClick = false;

  constructor() {
    this.#initEventListeners();
  }

  #initEventListeners() {
    document.addEventListener('click', this.#handleClickOutside.bind(this), true);
    document.addEventListener('mousedown', this.#handleMouseDown.bind(this), true);
  }

  #handleClickOutside(e) {
    if (this.#ignoreNextClick) {
      this.#ignoreNextClick = false;
      return;
    }

    if (e.target.closest('.toolbar-dropdown-container, .toolbar-dropdown')) {
      return;
    }

    const button = e.target.closest('button');
    if (button && this.#dropdowns.has(button)) {
      return;
    }

    this.closeAll();
  }

  #handleMouseDown(e) {
    const dropdown = e.target.closest('.toolbar-dropdown-container, .toolbar-dropdown');
    const button = e.target.closest('button');

    if (dropdown || (button && this.#dropdowns.has(button))) {
      e.stopPropagation();
    }
  }

  #createDropdownItem(option, ctx, buttonElement, isDisabled = false, isActive = false) {
    const item = document.createElement('button');
    item.className = 'toolbar-dropdown-item';
    item.type = 'button';
    item.dataset.key = option.key;

    if (isDisabled) {
      item.classList.add('disabled');
      item.setAttribute('disabled', 'true');
    }

    if (isActive) {
      item.classList.add('active');
    }

    if (option.icon) {
      const iconSpan = document.createElement('span');
      iconSpan.className = 'toolbar-dropdown-icon';
      iconSpan.innerHTML = option.icon;
      item.appendChild(iconSpan);
    }

    const content = document.createElement('div');
    content.className = 'toolbar-dropdown-content';

    const label = document.createElement('div');
    label.className = 'toolbar-dropdown-label';
    label.textContent = option.label;
    content.appendChild(label);

    if (option.description) {
      const desc = document.createElement('div');
      desc.className = 'toolbar-dropdown-description';
      desc.textContent = option.description;
      content.appendChild(desc);
    }

    item.appendChild(content);

    if (!isDisabled) {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        option.onRun(ctx);
        this.close(buttonElement);
      });
    }

    return item;
  }

  createDropdown(buttonElement, options, ctx, headerConfig = null, isItemDisabled = null, isItemActive = null) {
    const container = document.createElement('div');
    container.className = 'toolbar-dropdown-container';

    const dropdown = document.createElement('div');
    dropdown.className = 'toolbar-dropdown';

    const resolvedHeaderConfig = typeof headerConfig === 'function' ? headerConfig() : headerConfig;

    if (resolvedHeaderConfig) {
      const header = document.createElement('div');
      header.className = 'toolbar-dropdown-header';

      const headerText = document.createElement('span');
      headerText.textContent = resolvedHeaderConfig.text;
      header.appendChild(headerText);

      if (resolvedHeaderConfig.counter !== undefined && resolvedHeaderConfig.counter !== null) {
        const counter = document.createElement('span');
        counter.className = 'toolbar-dropdown-header-counter';
        counter.textContent = resolvedHeaderConfig.counter;

        if (resolvedHeaderConfig.counterClass) {
          counter.classList.add(resolvedHeaderConfig.counterClass);
        }

        header.appendChild(counter);
      }

      dropdown.appendChild(header);
    }

    options.forEach(option => {
      const disabled = isItemDisabled ? isItemDisabled(option) : false;
      const active = isItemActive ? isItemActive(option, ctx) : false;
      dropdown.appendChild(this.#createDropdownItem(option, ctx, buttonElement, disabled, active));
    });

    container.appendChild(dropdown);
    this.#dropdowns.set(buttonElement, container);

    return container;
  }

  createCustomDropdown(buttonElement, customContent, headerConfig = null) {
    const container = document.createElement('div');
    container.className = 'toolbar-dropdown-container';

    const dropdown = document.createElement('div');
    dropdown.className = 'toolbar-dropdown toolbar-dropdown-custom';

    const resolvedHeaderConfig = typeof headerConfig === 'function' ? headerConfig() : headerConfig;

    if (resolvedHeaderConfig) {
      const header = document.createElement('div');
      header.className = 'toolbar-dropdown-header';

      const headerText = document.createElement('span');
      headerText.textContent = resolvedHeaderConfig.text;
      header.appendChild(headerText);

      if (resolvedHeaderConfig.counter !== undefined && resolvedHeaderConfig.counter !== null) {
        const counter = document.createElement('span');
        counter.className = 'toolbar-dropdown-header-counter';
        counter.textContent = resolvedHeaderConfig.counter;

        if (resolvedHeaderConfig.counterClass) {
          counter.classList.add(resolvedHeaderConfig.counterClass);
        }

        header.appendChild(counter);
      }

      dropdown.appendChild(header);
    }

    dropdown.appendChild(customContent);

    container.appendChild(dropdown);
    this.#dropdowns.set(buttonElement, container);

    return container;
  }

  #measureDropdown(dropdown) {
    dropdown.style.visibility = 'hidden';
    dropdown.style.display = 'block';
    document.body.appendChild(dropdown);
    const rect = dropdown.getBoundingClientRect();
    dropdown.remove();
    dropdown.style.visibility = '';
    dropdown.style.display = '';
    return rect;
  }

  #calculateVerticalPosition(buttonRect, dropdownHeight, viewportHeight) {
    const { GAP, VIEWPORT_MARGIN } = DROPDOWN_CONFIG;
    const spaceBelow = viewportHeight - buttonRect.bottom - VIEWPORT_MARGIN;
    const spaceAbove = buttonRect.top - VIEWPORT_MARGIN;

    let top, placement;

    if (spaceBelow >= dropdownHeight + GAP) {
      top = buttonRect.bottom + GAP;
      placement = 'bottom';
    } else if (spaceAbove >= dropdownHeight + GAP) {
      top = buttonRect.top - dropdownHeight - GAP;
      placement = 'top';
    } else {
      if (spaceBelow > spaceAbove) {
        top = buttonRect.bottom + GAP;
        placement = 'bottom';
      } else {
        top = Math.max(VIEWPORT_MARGIN, buttonRect.top - dropdownHeight - GAP);
        placement = 'top';
      }
    }

    return {
      top: Math.max(VIEWPORT_MARGIN, Math.min(top, viewportHeight - dropdownHeight - VIEWPORT_MARGIN)),
      placement
    };
  }

  #calculateHorizontalPosition(buttonRect, dropdownWidth, viewportWidth) {
    const { VIEWPORT_MARGIN } = DROPDOWN_CONFIG;
    let left = buttonRect.left;

    if (left + dropdownWidth > viewportWidth - VIEWPORT_MARGIN) {
      left = buttonRect.right - dropdownWidth;
    }

    if (left < VIEWPORT_MARGIN) {
      left = VIEWPORT_MARGIN;
    }

    if (dropdownWidth > viewportWidth - (VIEWPORT_MARGIN * 2)) {
      left = VIEWPORT_MARGIN;
    }

    return Math.max(VIEWPORT_MARGIN, Math.min(left, viewportWidth - dropdownWidth - VIEWPORT_MARGIN));
  }

  #calculatePosition(buttonElement, dropdown) {
    const buttonRect = buttonElement.getBoundingClientRect();
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    const dropdownRect = this.#measureDropdown(dropdown);
    const dropdownWidth = dropdownRect.width || 280;
    const dropdownHeight = dropdownRect.height || 300;

    const vertical = this.#calculateVerticalPosition(buttonRect, dropdownHeight, viewportHeight);
    const left = this.#calculateHorizontalPosition(buttonRect, dropdownWidth, viewportWidth);

    return {
      top: vertical.top,
      left,
      placement: vertical.placement
    };
  }

  open(buttonElement) {
    this.closeAll();

    const dropdown = this.#dropdowns.get(buttonElement);
    if (!dropdown) return;

    const position = this.#calculatePosition(buttonElement, dropdown);

    dropdown.style.position = 'fixed';
    dropdown.style.top = `${position.top}px`;
    dropdown.style.left = `${position.left}px`;
    dropdown.style.zIndex = '10000';
    dropdown.dataset.placement = position.placement;
    dropdown.style.pointerEvents = 'none';

    document.body.appendChild(dropdown);
    dropdown.classList.add('open');
    this.#openDropdown = buttonElement;

    this.#ignoreNextClick = true;

    requestAnimationFrame(() => {
      dropdown.style.pointerEvents = '';
    });
  }

  close(buttonElement) {
    const dropdown = this.#dropdowns.get(buttonElement);
    if (dropdown?.parentNode) {
      dropdown.classList.remove('open');
      dropdown.remove();
    }
    if (this.#openDropdown === buttonElement) {
      this.#openDropdown = null;
    }
  }

  closeAll() {
    this.#dropdowns.forEach((dropdown) => {
      if (dropdown.parentNode) {
        dropdown.classList.remove('open');
        dropdown.remove();
      }
    });
    this.#openDropdown = null;
  }

  isOpen(buttonElement) {
    return this.#openDropdown === buttonElement;
  }

  toggle(buttonElement) {
    if (this.isOpen(buttonElement)) {
      this.close(buttonElement);
    } else {
      this.open(buttonElement);
    }
  }
}

export const toolbarDropdownManager = new ToolbarDropdownManager();

/**
 * Recherche un bouton dans le DOM par son ID (classe CSS dans l'icône)
 */
function findButtonById(buttonId) {
  if (!buttonId) return null;

  const iconElement = document.querySelector(`.milkdown-toolbar .${buttonId}`);
  if (iconElement) {
    return iconElement.closest('button');
  }

  return null;
}

/**
 * Creates a toolbar item with custom dropdown content
 *
 * @param {string} icon - SVG icon for the toolbar button
 * @param {Function} contentBuilder - Function(ctx, buttonElement) that returns a DOM element
 * @param {Function|null} disabledCheck - Optional function to check if button should be disabled
 * @param {Object|null} headerConfig - Optional header configuration { text, counter, counterClass }
 * @param {string|null} buttonId - Optional unique identifier for the button
 * @returns {Object} Toolbar item configuration object
 */
export function createCustomDropdownToolbarItem(icon, contentBuilder, disabledCheck = null, headerConfig = null, buttonId = null) {
  let buttonElement = null;

  const findBtn = () => {
    if (buttonElement?.isConnected) {
      return buttonElement;
    }

    buttonElement = findButtonById(buttonId);
    return buttonElement;
  };

  const updateButtonDisabledState = (btn, isDisabled) => {
    if (!btn) return;

    if (isDisabled) {
      btn.setAttribute('disabled', 'true');
      btn.setAttribute('aria-disabled', 'true');
    } else {
      btn.removeAttribute('disabled');
      btn.removeAttribute('aria-disabled');
    }
  };

  return {
    icon,
    active: (ctx) => {
      const btn = findBtn();

      if (btn && disabledCheck) {
        const isDisabled = disabledCheck(ctx);
        updateButtonDisabledState(btn, isDisabled);
      }

      return btn && toolbarDropdownManager.isOpen(btn);
    },
    onRun: (ctx) => {
      requestAnimationFrame(() => {
        const btn = findBtn();

        if (!btn) {
          console.warn('[Toolbar Dropdown] Button not found in DOM');
          return;
        }

        btn.dataset.dropdownButton = 'true';

        if (disabledCheck && disabledCheck(ctx)) {
          return;
        }

        if (!toolbarDropdownManager.isOpen(btn)) {
          const customContent = contentBuilder(ctx, btn);
          toolbarDropdownManager.createCustomDropdown(btn, customContent, headerConfig);
        }

        toolbarDropdownManager.toggle(btn);
      });
    }
  };
}

/**
 * Creates a toolbar item with dropdown menu items
 *
 * @param {string} icon - SVG icon for the toolbar button
 * @param {Array} dropdownItems - Array of dropdown menu items
 * @param {Function|null} disabledCheck - Optional function to check if button should be disabled
 * @param {Object|null} headerConfig - Optional header configuration { text, counter, counterClass }
 * @param {Function|null} isItemDisabled - Optional function to check if individual item should be disabled
 * @param {string|null} buttonId - Optional unique identifier (CSS class in icon)
 * @param {Function|null} activeCheck - Optional function to check if button should be active (in addition to dropdown open state)
 * @param {Function|null} isItemActive - Optional function(option, ctx) to check if individual item should be highlighted as active
 * @returns {Object} Toolbar item configuration object
 */
export function createDropdownToolbarItem(icon, dropdownItems, disabledCheck = null, headerConfig = null, isItemDisabled = null, buttonId = null, activeCheck = null, isItemActive = null) {
  let buttonElement = null;

  const findBtn = () => {
    if (buttonElement?.isConnected) {
      return buttonElement;
    }

    buttonElement = findButtonById(buttonId);
    return buttonElement;
  };

  const updateButtonDisabledState = (btn, isDisabled) => {
    if (!btn) return;

    if (isDisabled) {
      btn.setAttribute('disabled', 'true');
      btn.setAttribute('aria-disabled', 'true');
    } else {
      btn.removeAttribute('disabled');
      btn.removeAttribute('aria-disabled');
    }
  };

  return {
    icon,
    active: (ctx) => {
      const btn = findBtn();

      if (btn && disabledCheck) {
        const isDisabled = disabledCheck(ctx);
        updateButtonDisabledState(btn, isDisabled);
      }

      const isDropdownOpen = btn && toolbarDropdownManager.isOpen(btn);
      const isContentActive = activeCheck ? activeCheck(ctx) : false;

      return isDropdownOpen || isContentActive;
    },
    onRun: (ctx) => {
      requestAnimationFrame(() => {
        const btn = findBtn();

        if (!btn) {
          console.warn('[Toolbar Dropdown] Button not found in DOM');
          return;
        }

        btn.dataset.dropdownButton = 'true';

        if (disabledCheck && disabledCheck(ctx)) {
          return;
        }

        if (!toolbarDropdownManager.isOpen(btn)) {
          toolbarDropdownManager.createDropdown(btn, dropdownItems, ctx, headerConfig, isItemDisabled, isItemActive);
        }

        toolbarDropdownManager.toggle(btn);
      });
    }
  };
}
