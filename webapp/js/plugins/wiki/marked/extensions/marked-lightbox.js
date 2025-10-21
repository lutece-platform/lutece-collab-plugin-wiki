(function() {
    'use strict';

    var lightbox = null;

    function createLightbox() {
        if (lightbox) return;
        lightbox = document.createElement('div');
        lightbox.className = 'wiki-lightbox';
        lightbox.innerHTML = '<div class="wiki-lightbox-backdrop"></div>' +
            '<div class="wiki-lightbox-container">' +
            '<button class="wiki-lightbox-close" aria-label="Fermer">' +
            '<i class="ti ti-x"></i></button>' +
            '<div class="wiki-lightbox-content"></div>' +
            '</div>';
        document.body.appendChild(lightbox);

        lightbox.querySelector('.wiki-lightbox-backdrop').addEventListener('click', closeLightbox);
        lightbox.querySelector('.wiki-lightbox-container').addEventListener('click', function(e) {
            if (e.target === this || e.target.classList.contains('wiki-lightbox-content')) {
                closeLightbox();
            }
        });
        lightbox.querySelector('.wiki-lightbox-close').addEventListener('click', closeLightbox);
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && lightbox.classList.contains('active')) {
                closeLightbox();
            }
        });
    }

    function initPanZoom(el) {
        var scale = 1;
        var panX = 0;
        var panY = 0;
        var isPanning = false;
        var startX, startY;

        el.style.cursor = 'grab';

        el.addEventListener('wheel', function(e) {
            e.preventDefault();
            var delta = e.deltaY > 0 ? 0.9 : 1.1;
            scale *= delta;
            scale = Math.min(Math.max(0.5, scale), 5);
            el.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + scale + ')';
        });

        el.addEventListener('mousedown', function(e) {
            e.preventDefault();
            isPanning = true;
            startX = e.clientX - panX;
            startY = e.clientY - panY;
            el.style.cursor = 'grabbing';
        });

        window.addEventListener('mousemove', function(e) {
            if (!isPanning) return;
            panX = e.clientX - startX;
            panY = e.clientY - startY;
            el.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + scale + ')';
        });

        window.addEventListener('mouseup', function() {
            isPanning = false;
            el.style.cursor = 'grab';
        });

        el.addEventListener('dblclick', function() {
            scale = 1;
            panX = 0;
            panY = 0;
            el.style.transform = '';
        });
    }

    function openLightbox(element, type) {
        createLightbox();
        var content = lightbox.querySelector('.wiki-lightbox-content');
        content.innerHTML = '';

        if (type === 'image') {
            var img = document.createElement('img');
            img.src = element.src;
            img.className = 'wiki-lightbox-image';
            content.appendChild(img);
            initPanZoom(img);
        } else if (type === 'mermaid') {
            var svg = element.querySelector('svg');
            if (svg) {
                var wrapper = document.createElement('div');
                wrapper.className = 'wiki-lightbox-mermaid';
                wrapper.innerHTML = svg.outerHTML;
                content.appendChild(wrapper);
                initPanZoom(wrapper.querySelector('svg'));
            }
        }

        lightbox.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeLightbox() {
        if (!lightbox) return;
        lightbox.classList.remove('active');
        document.body.style.overflow = '';
    }

    function openMermaidLightbox(mermaidWrapper) {
        openLightbox(mermaidWrapper, 'mermaid');
    }

    function initLightbox(container) {
        container.querySelectorAll('img').forEach(function(img) {
            if (img.dataset.lightbox) return;
            img.dataset.lightbox = 'true';
            img.style.cursor = 'pointer';
            img.addEventListener('click', function() {
                openLightbox(img, 'image');
            });
        });
    }

    window.initLightbox = initLightbox;
    window.openMermaidLightbox = openMermaidLightbox;
})();
