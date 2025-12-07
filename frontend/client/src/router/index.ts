import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import userAuth from '@/stores/auth'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import SignupView from '@/views/SignupView.vue'
import VerifyOtpView from '@/views/VerifyOtpView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ReservationView from '@/views/ReservationView.vue'
import PaymentMethodsComponent from '@/components/payments/PaymentMethodsComponent.vue'
import DestinationMapComponent from '@/components/DestinationMapComponent.vue'
import ReportProblemComponent from '@/components/ReportProblemComponent.vue'
import PaymentSuccesComponent from '@/components/payments/PaymentSuccesComponent.vue'
import PaymentCancelComponent from '@/components/payments/PaymentCancelComponent.vue'


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
    path: '/verify-email',
    name: 'verify-email',
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
    path: '/plan-your-trip',
    name: 'plan-your-trip',
    component: ReservationView,
    meta:{ requireAuth: false, layout: 'main' }
  },

  {
    path: '/destination',
    name: 'destination',
    component: DestinationMapComponent,
    meta: {requireAuth: false, layout: 'main'}
  },
  {
    path: '/report-problem',
    name: 'report-problem',
    component: ReportProblemComponent,
    meta: {requireAuth: true, layout: 'main'}

  },
  {
    path: '/pago/success',
    name: 'payment-success',
    component: PaymentSuccesComponent,
    meta: { requireAuth: true, layout: 'main' }
  },
  {
    path: '/pago/cancel',
    name: 'payment-cancel',
    component: PaymentCancelComponent,
    meta: { requireAuth: true, layout: 'main' }
  },
  {
    path: '/pago/methods',
    name: 'payment-methods',
    component: PaymentMethodsComponent,
    meta: { requireAuth: true, layout: 'main' }
  }



]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// Guard global para proteger rutas autenticadas
router.beforeEach((to, from, next) => {
  const authStore = userAuth()
  const isAuthenticated = !!authStore.token

  // Si la ruta requiere autenticación y no está autenticado
  if (to.meta.requireAuth && !isAuthenticated) {
    // Redirigir a login
    next({ name: 'login' })
  }
  // Si ya está autenticado e intenta acceder a rutas de auth
  else if (isAuthenticated && (to.name === 'login' || to.name === 'signup')) {
    // Redirigir a home
    next({ name: 'home' })
  }
  else {
    next()
  }
})

export default router
