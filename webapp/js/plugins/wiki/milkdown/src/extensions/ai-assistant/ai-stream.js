/**
 * Starts an AI stream: posts the request, then reads the answer token by token.
 * @param {string} url the init endpoint, relative to the site
 * @param {Object} body the request
 * @param {Function} onToken called with the answer so far, after each token
 * @param {Object} messages the AI assistant translations
 * @returns {{done: Promise<string>, stop: Function}} the full answer once completed, and a way to stop listening
 */
export function streamAi(url, body, onToken, messages) {
  let source = null;
  let stopped = false;

  const done = (async () => {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).catch(() => {
      throw new Error(messages.connectionError);
    });
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.error || messages.error);
    }
    const { stream_id: streamId } = await response.json();
    if (stopped) {
      return '';
    }

    return new Promise((resolve, reject) => {
      let answer = '';
      source = new EventSource(`rest/wiki/ai/features/stream/events/${streamId}`);
      source.addEventListener('token', (event) => {
        answer += JSON.parse(event.data).token;
        onToken(answer);
      });
      source.addEventListener('completed', () => {
        source.close();
        resolve(answer);
      });
      source.addEventListener('error', (event) => {
        source.close();
        let message = messages.connectionError;
        try {
          message = JSON.parse(event.data).error || messages.error;
        } catch {
        }
        reject(new Error(message));
      });
    });
  })();

  return {
    done,
    stop: () => {
      stopped = true;
      source?.close();
    },
  };
}

/**
 * Removes a code fence the model may have put around its whole answer.
 * @param {string} answer the answer
 * @returns {string} the answer without the fence
 */
export function unwrapFence(answer) {
  const match = answer.trim().match(/^```(?:markdown|md)?\s*\n([\s\S]*?)\n```$/i);
  return match ? match[1] : answer.trim();
}
