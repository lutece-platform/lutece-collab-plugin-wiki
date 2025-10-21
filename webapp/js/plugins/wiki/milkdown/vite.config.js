import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    hmr: {
      overlay: true,
    },
    watch: {
      usePolling: true,
      interval: 100,
    },
    port: 3000,
    open: true,
  },

  optimizeDeps: {
    force: true,
  },

  cacheDir: 'node_modules/.vite',

  build: {
    sourcemap: true,
  },
});
