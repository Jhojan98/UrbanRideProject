const { defineConfig } = require('@vue/cli-service')
const path = require('path')
const dotenv = require('dotenv')

// Cargar variables de entorno desde el archivo local .env
const envPath = path.resolve(__dirname, '.env')
dotenv.config({ path: envPath })

// También intentar cargar desde .env.local si existe
const envLocalPath = path.resolve(__dirname, '.env.local')
dotenv.config({ path: envLocalPath })

console.log('=== ADMIN VUE CONFIG DEBUG ===')
console.log('ENV Path:', envPath)
console.log('ENV LOCAL Path:', envLocalPath)
console.log('VUE_APP_API_URL:', process.env.VUE_APP_API_URL)
console.log('==============================')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '^/api': {
        target: process.env.VUE_APP_API_URL || 'http://34.9.26.232:8090',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        },
        onProxyRes(proxyRes) {
          // Limpiar headers CORS conflictivos - solo dejar un valor
          proxyRes.headers['access-control-allow-origin'] = '*'
          // Asegurar que métodos y headers CORS incluyan PATCH
          proxyRes.headers['access-control-allow-methods'] = 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS'
          proxyRes.headers['access-control-allow-headers'] = 'Content-Type,Authorization'
          // Permitir credenciales si es necesario
          proxyRes.headers['access-control-allow-credentials'] = 'true'
        }
      }
    }
  }
})
