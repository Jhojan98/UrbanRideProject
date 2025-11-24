import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { messages } from '@/lang/messages'
import{initializeApp} from "firebase/app";
import { createI18n } from 'vue-i18n'
import { createWebSocket } from './services/webSocket'
import '@/styles/global.scss'

const firebaseConfig = {
  apiKey: process.env.VUE_APP_FIREBASE_API_KEY,
  authDomain: process.env.VUE_APP_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.VUE_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.VUE_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.VUE_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.VUE_APP_FIREBASE_APP_ID,
  measurementId: process.env.VUE_APP_FIREBASE_MEASUREMENT_ID
}

const pinia = createPinia()
const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: 'es',
    fallbackLocale: 'en',
    messages
})
initializeApp(firebaseConfig);
createWebSocket
const app = createApp(App)
app.use(router)
app.use(pinia)
app.use(i18n)
app.mount('#app')