import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const proxyConfig = {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
  '/auth': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
  '/login': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    bypass: (req) => {
      if (req.method === 'GET') {
        return '/index.html';
      }
    },
  },
  '/register': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    bypass: (req) => {
      if (req.method === 'GET') {
        return '/index.html';
      }
    },
  },
};

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: proxyConfig,
  },
  preview: {
    proxy: proxyConfig,
  },
})
