const { defineConfig } = require('@vue/cli-service')
const path = require('path')
const dotenv = require('dotenv')

// Cargar variables de entorno desde la carpeta padre (frontend/.env)
const envPath = path.resolve(__dirname, '../.env')
dotenv.config({ path: envPath })

module.exports = defineConfig({
  transpileDependencies: true
})
