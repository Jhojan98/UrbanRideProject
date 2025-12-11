const { defineConfig } = require('@vue/cli-service')
const path = require('path')
const dotenv = require('dotenv')

// Cargar variables de entorno desde la carpeta padre (frontend/.env)
const envPath = path.resolve(__dirname, '../.env')
dotenv.config({ path: envPath })

console.log('=== VUE CONFIG DEBUG ===')
console.log('ENV Path:', envPath)
console.log('=======================')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '^/api': {
        target: 'http://localhost:8090',
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
