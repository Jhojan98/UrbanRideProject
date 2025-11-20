import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import useAuthStore from '@/stores/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'login',
    component: LoginView,
    meta: { layout: 'main', requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardLayout,
    // meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// Navigation guard para proteger rutas autenticadas
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  // Restaurar sesión desde localStorage si existe
  authStore.restoreSession()
  
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !authStore.isAuthenticated) {
    // Redirigir al login si la ruta requiere autenticación y no está autenticado
    next({ name: 'login' })
  } else if (to.name === 'login' && authStore.isAuthenticated) {
    // Redirigir al dashboard si ya está autenticado e intenta ir al login
    next({ name: 'dashboard' })
  } else {
    next()
  }
})

export default router
