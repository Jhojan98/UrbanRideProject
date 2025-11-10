import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import VerifyOtpView from '@/views/VerifyOtpView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ReservationView from '@/views/ReservationView.vue'
import BalanceComponent from '@/components/BalanceComponent.vue'
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requireAuth: false }
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupView,
    meta: {requireAuth: false}
  },
  {
    path: '/verify-otp',
    name: 'verify-otp',
    component: VerifyOtpView
  },
  {
    path: '/my-profile',
    name: 'profile',
    component: ProfileView,
    meta:{ requireAuth: true }
  },
  {
    path: '/reservation',
    name: 'reservation',
    component: ReservationView,
    meta:{ requireAuth: true }
  }, {
    path: '/balance',
    name: 'balance',
    component: BalanceComponent,
    meta:{ requireAuth: true }
  }
]
//TODO: Descomentar cuando esté el signup listo

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
