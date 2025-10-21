(function() {
    'use strict';

    var mermaidId = 0;
    var mermaidLoaded = false;
    var mermaidLoading = false;
    var pendingContainers = [];

    function initPanZoom(container) {
        var svg = container.querySelector('svg');
        if (!svg) return;
        var scale = 1;
        var panX = 0;
        var panY = 0;
        var isPanning = false;
        var startX, startY;
        svg.style.cursor = 'grab';
        container.addEventListener('wheel', function(e) {
            e.preventDefault();
            var delta = e.deltaY > 0 ? 0.9 : 1.1;
            scale *= delta;
            scale = Math.min(Math.max(0.5, scale), 4);
            svg.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + scale + ')';
        });
        container.addEventListener('mousedown', function(e) {
            isPanning = true;
            startX = e.clientX - panX;
            startY = e.clientY - panY;
            svg.style.cursor = 'grabbing';
        });
        container.addEventListener('mousemove', function(e) {
            if (!isPanning) return;
            panX = e.clientX - startX;
            panY = e.clientY - startY;
            svg.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + scale + ')';
        });
        container.addEventListener('mouseup', function() {
            isPanning = false;
            svg.style.cursor = 'grab';
        });
        container.addEventListener('mouseleave', function() {
            isPanning = false;
            svg.style.cursor = 'grab';
        });
    }

    function renderBlocks(container) {
        var blocks = container.querySelectorAll('.wiki-mermaid-pending');
        blocks.forEach(function(wrapper) {
            var code = wrapper.dataset.code;
            var id = wrapper.id + '-svg';
            var body = wrapper.querySelector('.wiki-mermaid-body');
            wrapper.classList.remove('wiki-mermaid-pending');
            mermaid.render(id, code).then(function(result) {
                body.innerHTML = result.svg;
                initPanZoom(body);
                wrapper.querySelector('.wiki-mermaid-fullscreen').addEventListener('click', function() {
                    if (window.openMermaidLightbox) openMermaidLightbox(body);
                });
            }).catch(function(err) {
                body.innerHTML = '<pre class="text-danger m-0">' + err.message + '</pre>';
            });
        });
    }

    function loadMermaid(callback) {
        if (mermaidLoaded && window.mermaid) {
            callback();
            return;
        }
        if (window.mermaid) {
            mermaidLoaded = true;
            mermaid.initialize({ startOnLoad: false, theme: 'default' });
            callback();
            return;
        }
        if (mermaidLoading) {
            pendingContainers.push(callback);
            return;
        }
        mermaidLoading = true;
        var script = document.createElement('script');
        script.src = 'js/plugins/wiki/marked/dependencies/mermaid.min.js';
        script.onload = function() {
            mermaidLoaded = true;
            mermaidLoading = false;
            mermaid.initialize({ startOnLoad: false, theme: 'default' });
            callback();
            pendingContainers.forEach(function(cb) { cb(); });
            pendingContainers = [];
        };
        script.onerror = function(e) {
            mermaidLoading = false;
            console.error('Failed to load mermaid.min.js', e);
        };
        document.head.appendChild(script);
    }

    function renderMermaidBlocks(container) {
        var blocks = container.querySelectorAll('.wiki-mermaid-pending');
        if (blocks.length === 0) return;
        loadMermaid(function() {
            renderBlocks(container);
        });
    }

    var markedMermaid = {
        renderer: {
            code: function(token) {
                if (token.lang === 'mermaid') {
                    var id = 'mermaid-' + Date.now() + '-' + (mermaidId++);
                    var escapedCode = token.text.replace(/"/g, '&quot;');
                    return '<div class="card my-3 wiki-mermaid-pending" id="' + id + '" data-code="' + escapedCode + '">' +
                        '<div class="card-header d-flex justify-content-between align-items-center py-2">' +
                        '<small class="text-muted">mermaid</small>' +
                        '<button class="wiki-mermaid-fullscreen btn btn-sm btn-light" title="Plein écran"><i class="ti ti-arrows-maximize"></i></button>' +
                        '</div>' +
                        '<div class="card-body wiki-mermaid-body"><div class="wiki-mermaid-loading">Chargement du diagramme...</div></div>' +
                        '</div>';
                }
                return false;
            }
        }
    };

    window.markedMermaid = markedMermaid;
    window.renderMermaidBlocks = renderMermaidBlocks;
})();
