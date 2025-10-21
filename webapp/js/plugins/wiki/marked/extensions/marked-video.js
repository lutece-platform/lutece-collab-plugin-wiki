(function() {
    'use strict';

    const markedVideo = {
        renderer: {
            paragraph: function(token) {
                const text = token.raw || '';
                const match = text.match(/^::video\{src="([^"]+)"\}\s*$/);
                if (match) {
                    const src = match[1];
                    return '<video controls class="wiki-video">\n<source src="' + src + '">\n</video>\n';
                }
                return false;
            }
        }
    };

    window.markedVideo = markedVideo;
})();
