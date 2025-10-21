(function() {
    'use strict';

    const typeMap = {
        'NOTE': 'info',
        'TIP': 'success',
        'IMPORTANT': 'primary',
        'WARNING': 'warning',
        'CAUTION': 'danger'
    };

    const markedAlert = {
        renderer: {
            blockquote: function(token) {
                const body = this.parser.parse(token.tokens);

                // Case 1: [!TYPE] followed by <br> - <p>[!NOTE]<br>content</p>
                const matchWithBr = body.match(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]<br>\s*/i);
                if (matchWithBr) {
                    const alertType = matchWithBr[1].toUpperCase();
                    const alertClass = typeMap[alertType] || 'info';
                    const content = body.replace(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]<br>\s*/i, '<p>');
                    return '<div class="alert alert-' + alertClass + '" role="alert">\n' + content + '</div>\n';
                }

                // Case 2: [!TYPE] alone on its line - <p>[!NOTE]</p><p>content</p>
                const matchSeparate = body.match(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]<\/p>/i);
                if (matchSeparate) {
                    const alertType = matchSeparate[1].toUpperCase();
                    const alertClass = typeMap[alertType] || 'info';
                    const content = body.replace(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]<\/p>\s*/i, '');
                    return '<div class="alert alert-' + alertClass + '" role="alert">\n' + content + '</div>\n';
                }

                // Case 3: [!TYPE] with content on same line - <p>[!NOTE]content</p> or <p>[!NOTE] content</p>
                const matchInline = body.match(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]\s*/i);
                if (matchInline) {
                    const alertType = matchInline[1].toUpperCase();
                    const alertClass = typeMap[alertType] || 'info';
                    const content = body.replace(/^<p>\\?\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]\s*/i, '<p>');
                    return '<div class="alert alert-' + alertClass + '" role="alert">\n' + content + '</div>\n';
                }

                return '<blockquote>\n' + body + '</blockquote>\n';
            }
        }
    };

    window.markedAlert = markedAlert;
})();
