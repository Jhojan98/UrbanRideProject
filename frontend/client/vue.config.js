const { defineConfig } = require('@vue/cli-service')

// Vue CLI automatically loads .env from project root
// We don't need manual dotenv, Vue handles it by default

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
