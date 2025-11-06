
import { createApp } from 'vue'
// Importa el componente principal App 
import App from './App.vue'
// Importa el router configurado 
import router from './router'

// Crea la instancia de la aplicación Vue 
const app = createApp(App)

// Usa el router en la aplicación 
app.use(router)


app.mount('#app')