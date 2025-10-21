import mermaid from 'mermaid';

mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
  logLevel: 'error',
  flowchart: {
    htmlLabels: false,
    useMaxWidth: false,
    curve: 'basis'
  }
});

function uuid() {
  return `mermaid-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

export async function renderMermaidPreview(code, applyPreview) {
  const id = uuid();

  try {
    await mermaid.parse(code);

    const { svg } = await mermaid.render(id, code);

    const container = document.createElement('div');
    container.className = 'mermaid-preview-wrapper';
    container.dataset.mermaidId = id;

    const svgContainer = document.createElement('div');
    svgContainer.className = 'mermaid-svg-container';
    svgContainer.innerHTML = svg;
    container.appendChild(svgContainer);

    applyPreview(container);

    setTimeout(() => {
      const actualContainer = document.querySelector(`[data-mermaid-id="${id}"]`);
      if (actualContainer) {
        setupWheelZoom(actualContainer);
      }
    }, 100);
  } catch (error) {
    console.error('Mermaid render error:', error);

    const errorContainer = document.createElement('div');
    errorContainer.className = 'mermaid-error-container';
    errorContainer.innerHTML = `
      <div class="mermaid-error">
        <strong>Mermaid Syntax Error:</strong><br>
        ${error.message}
      </div>
    `;

    applyPreview(errorContainer);
  }
}

function setupWheelZoom(container) {
  const svgContainer = container.querySelector('.mermaid-svg-container');
  if (!svgContainer) return;

  const svg = svgContainer.querySelector('svg');
  if (!svg) return;

  let scale = 1;
  let translateX = 0;
  let translateY = 0;
  let isDragging = false;
  let startX = 0;
  let startY = 0;

  function updateTransform() {
    svg.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scale})`;
    svg.style.transformOrigin = 'center';
  }

  svgContainer.addEventListener('wheel', (e) => {
    e.preventDefault();

    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    scale = Math.min(Math.max(scale * delta, 0.5), 3);

    updateTransform();
  });

  const handleMouseMove = (e) => {
    if (!isDragging) return;
    translateX = e.clientX - startX;
    translateY = e.clientY - startY;
    updateTransform();
  };

  const handleMouseUp = () => {
    if (isDragging) {
      isDragging = false;
      svgContainer.style.cursor = 'grab';
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    }
  };

  svgContainer.addEventListener('mousedown', (e) => {
    if (e.button === 0) {
      isDragging = true;
      startX = e.clientX - translateX;
      startY = e.clientY - translateY;
      svgContainer.style.cursor = 'grabbing';
      e.preventDefault();
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }
  });

  svgContainer.addEventListener('dblclick', () => {
    scale = 1;
    translateX = 0;
    translateY = 0;
    updateTransform();
  });

  svgContainer.style.cursor = 'grab';
}
