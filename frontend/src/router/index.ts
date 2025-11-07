import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import OTPVerificationView from '@/views/OTPVerificationView.vue' // ← Nueva importación
// import SignupView from '@/views/SignupView.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupView
  },
  {
    path: '/verify-otp',
    name: 'verify-otp',
    component: OTPVerificationView // ← Nueva ruta
  }
]
//TODO: Descomentar cuando esté el signup listo

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
