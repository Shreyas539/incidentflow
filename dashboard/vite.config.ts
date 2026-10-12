import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// The browser only ever talks to this dev server (same origin), which forwards
// API calls to the two backend services. That is why no CORS setup is needed.
// Order matters: the first matching prefix wins, so the specific one comes first.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/notifications': { target: 'http://localhost:8081', changeOrigin: true },
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
