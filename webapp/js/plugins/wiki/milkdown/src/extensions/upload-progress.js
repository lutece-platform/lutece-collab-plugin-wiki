const BYTES_PER_MB = 1024 * 1024;

/**
 * Formats a size in megabytes.
 * @param {number} bytes the size, in bytes
 * @returns {string} the size, like "12,4"
 */
function megabytes(bytes) {
  return (bytes / BYTES_PER_MB).toLocaleString(undefined, { maximumFractionDigits: 1 });
}

/**
 * Fills the {name} placeholders of a message.
 * @param {string} pattern the message
 * @param {Object} values the values by name
 * @returns {string} the message
 */
function fill(pattern, values) {
  return pattern.replace(/\{(\w+)\}/g, (match, key) => (values[key] ?? match));
}

/**
 * Returns the message explaining why an upload failed.
 * @param {Error} error the upload error
 * @param {File} file the file
 * @param {Object} t the translations
 * @returns {string} the message
 */
function uploadErrorMessage(error, file, t) {
  const { reasons } = t.fileUpload;
  const details = error?.details || {};
  let reason;
  if (error?.code === 'tooLarge') {
    reason = fill(reasons.tooLarge, { size: megabytes(details.size), max: megabytes(details.max) });
  } else if (error?.code === 'server' && details.message) {
    reason = details.message;
  } else {
    reason = reasons[error?.code] || reasons.unknown;
  }
  return fill(t.fileUpload.failedWithReason, { name: file.name, reason });
}

/**
 * Finds a text in the document.
 * @param {Object} doc the ProseMirror document
 * @param {string} text the text
 * @returns {{from: number, to: number}|null} its range
 */
function findText(doc, text) {
  let range = null;
  doc.descendants((node, pos) => {
    if (range) return false;
    if (node.isText) {
      const index = node.text.indexOf(text);
      if (index >= 0) {
        range = { from: pos + index, to: pos + index + text.length };
      }
    }
    return !range;
  });
  return range;
}

/**
 * Uploads a file while a placeholder in the editor shows the progress, then replaces the placeholder by the result or
 * by the reason of the failure.
 * @param {Object} view the ProseMirror view
 * @param {Object} uploadHandlerRef holds the upload handler (file, onProgress) => url
 * @param {File} file the file
 * @param {Object} t the translations
 * @param {Function} insertResult (tr, from, to, url) => tr, puts the uploaded file in place of the placeholder
 */
export async function uploadWithProgress(view, uploadHandlerRef, file, t, insertResult) {
  const label = (percent) => `⏳ ${fill(t.fileUpload.progress, { name: file.name, percent })}`;
  let current = label(0);
  view.dispatch(view.state.tr.insertText(current));

  const replace = (build) => {
    const range = findText(view.state.doc, current);
    if (range) {
      view.dispatch(build(view.state.tr, range.from, range.to));
    }
  };

  try {
    const url = await uploadHandlerRef.handler(file, (percent) => {
      const next = label(percent);
      if (next !== current) {
        replace((tr, from, to) => tr.insertText(next, from, to));
        current = next;
      }
    });
    replace((tr, from, to) => insertResult(tr, from, to, url));
    view.focus();
  } catch (error) {
    console.error('Upload failed:', error);
    replace((tr, from, to) => tr.insertText(`❌ ${uploadErrorMessage(error, file, t)}`, from, to));
  }
}
