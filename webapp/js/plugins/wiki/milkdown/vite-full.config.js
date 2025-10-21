import { defineConfig } from 'vite';

export default defineConfig({
  define: {
    'process.env': {},
    'process.env.NODE_ENV': '"production"'
  },
  build: {
    lib: {
      entry: './src/milkdown-bundle.js',
      name: 'MilkdownBundle',
      fileName: 'milkdown-bundle',
      formats: ['umd']
    },
    rollupOptions: {
      output: {
        assetFileNames: 'milkdown-bundle.[ext]',
        manualChunks: undefined,
        inlineDynamicImports: true,
        exports: 'named'
      }
    },
    cssCodeSplit: false
  }
});