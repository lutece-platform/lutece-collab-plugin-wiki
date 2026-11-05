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
    rolldownOptions: {
      output: {
        assetFileNames: 'milkdown-bundle.[ext]',
        exports: 'named'
      }
    },
    cssCodeSplit: false
  }
});