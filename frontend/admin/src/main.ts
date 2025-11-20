import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import {messages} from '@/lang/messages'
import { createI18n } from 'vue-i18n'
import '@/styles/global.scss'

const pinia = createPinia()
const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: 'es',
    fallbackLocale: 'en',
    messages
})

const app = createApp(App)
app.use(router)
app.use(pinia)
app.use(i18n)
app.mount('#app')