import { Plugin, PluginKey } from '@milkdown/kit/prose/state';
import { $prose } from '@milkdown/kit/utils';

const PASTE_SANITIZER_KEY = new PluginKey('paste-sanitizer');

/**
 * Shows a toast notification to the user
 */
function showToast(message, type = 'warning') {
  const existingToast = document.querySelector('.milkdown-paste-toast');
  if (existingToast) {
    existingToast.remove();
  }

  const toast = document.createElement('div');
  toast.className = `milkdown-paste-toast milkdown-paste-toast--${type}`;
  toast.innerHTML = `
    <div class="milkdown-paste-toast__icon">
      ${type === 'warning' ? '⚠️' : type === 'error' ? '❌' : 'ℹ️'}
    </div>
    <div class="milkdown-paste-toast__message">${message}</div>
    <button class="milkdown-paste-toast__close" aria-label="Close">&times;</button>
  `;

  document.body.appendChild(toast);

  const closeBtn = toast.querySelector('.milkdown-paste-toast__close');
  closeBtn.addEventListener('click', () => toast.remove());

  setTimeout(() => {
    toast.classList.add('milkdown-paste-toast--visible');
  }, 10);

  setTimeout(() => {
    toast.classList.remove('milkdown-paste-toast--visible');
    setTimeout(() => toast.remove(), 300);
  }, 8000);
}

/**
 * Checks if a URL is a local file URL
 */
function isLocalFileUrl(url) {
  if (!url) return false;
  return url.startsWith('file:///') || url.startsWith('file://');
}

/**
 * Sanitizes HTML content by removing images with local file URLs
 * Returns the sanitized HTML and a flag indicating if images were removed
 */
function sanitizeHtmlContent(html) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(html, 'text/html');
  let imagesRemoved = 0;

  const images = doc.querySelectorAll('img');
  images.forEach(img => {
    const src = img.getAttribute('src');
    if (isLocalFileUrl(src)) {
      img.remove();
      imagesRemoved++;
    }
  });

  const bgElements = doc.querySelectorAll('[style*="background"]');
  bgElements.forEach(el => {
    const style = el.getAttribute('style');
    if (style && style.includes('file:///')) {
      el.style.backgroundImage = 'none';
      imagesRemoved++;
    }
  });

  return {
    html: doc.body.innerHTML,
    imagesRemoved
  };
}

/**
 * Creates the paste sanitizer plugin
 */
export function createPasteSanitizerPlugin(translations = {}) {
  const t = translations.pasteSanitizer || {
    localImagesRemoved: 'Images from local files (Word, etc.) cannot be pasted directly. Please upload them using the image button.',
    localImagesRemovedPlural: 'images from local files were removed. Please upload them using the image button.',
  };

  return $prose(() => {
    return new Plugin({
      key: PASTE_SANITIZER_KEY,
      props: {
        handlePaste(view, event) {
          const clipboardData = event.clipboardData;
          if (!clipboardData) return false;

          const html = clipboardData.getData('text/html');
          if (!html) return false;

          if (!html.includes('file:///') && !html.includes('file://')) {
            return false;
          }

          const { html: sanitizedHtml, imagesRemoved } = sanitizeHtmlContent(html);

          if (imagesRemoved > 0) {
            const message = imagesRemoved === 1
              ? t.localImagesRemoved
              : `${imagesRemoved} ${t.localImagesRemovedPlural}`;

            showToast(message, 'warning');

            if (sanitizedHtml.trim()) {
              const tempDiv = document.createElement('div');
              tempDiv.innerHTML = sanitizedHtml;
              const text = tempDiv.textContent || tempDiv.innerText || '';

              if (text.trim()) {
                const { state, dispatch } = view;
                const tr = state.tr.insertText(text);
                dispatch(tr);
              }
            }

            event.preventDefault();
            return true;
          }

          return false;
        }
      }
    });
  });
}

export const pasteSanitizerPlugin = createPasteSanitizerPlugin();
