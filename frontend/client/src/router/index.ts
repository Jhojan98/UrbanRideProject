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
    component: HomeView,
    meta: { layout: 'main' }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requireAuth: false, layout: 'blank' }
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupView,
    meta: {requireAuth: false, layout: 'blank'}
  },

  {
    path: '/verify-otp',
    name: 'verify-otp',
    component: VerifyOtpView,
    meta: { layout: 'blank' }
  },
  {
    path: '/my-profile',
    name: 'profile',
    component: ProfileView,
    meta:{ requireAuth: true, layout: 'main' }
  },
  {
    path: '/maps',
    name: 'maps',
    component: ReservationView,
    meta:{ requireAuth: true, layout: 'main' }
  },
  {
    path: '/reservation',
    name: 'reservation',
    component: ReservationView,
    meta:{ requireAuth: true, layout: 'main' }
  },
  {
    path: '/balance',
    name: 'balance',
    component: BalanceComponent,
    meta:{ requireAuth: true, layout: 'main' }
  },
  {
    path: '/plan-your-trip',
    name: 'plan-your-trip',
    component: ReservationView,
    meta:{ requireAuth: true, layout: 'main' }
  }


]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
