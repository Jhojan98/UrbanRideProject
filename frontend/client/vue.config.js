const { defineConfig } = require('@vue/cli-service')

// Vue CLI carga automáticamente .env de la raíz del proyecto
// No necesitamos dotenv manual, Vue lo maneja por defecto

module.exports = defineConfig({
  transpileDependencies: true,
  // Proxy API calls in dev to avoid CORS while keeping the backend origin configurable
  devServer: {
    proxy: {
      '^/api': {
        target: process.env.VUE_APP_API_URL || 'http://localhost:8090',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
        logLevel: 'debug'
      }
    }
  }
})
