
import { defineConfig } from 'vite'

// Importa el plugin de Vue para Vite
import vue from '@vitejs/plugin-vue'

export default defineConfig({

  plugins: [vue()],

  // Configuración del servidor 
  server: {
    port: 5173, // Puerto en el que se ejecutará el servidor
    host: true  
  },
  // Configuración para la construcción
  build: {
    outDir: 'dist', // Carpeta de salida para los archivos construidos
    sourcemap: false 
  }
})