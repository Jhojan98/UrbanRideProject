import { createRouter, createWebHistory } from 'vue-router'
import Home from './views/Home.vue'
import Admin from './views/Admin.vue'
import User from './views/User.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/admin', name: 'Admin', component: Admin, meta: { requiresAuth: true, role: 'admin' } },
  { path: '/user', name: 'User', component: User, meta: { requiresAuth: true, role: 'user' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = JSON.parse(localStorage.getItem('auth')) || null
  if (to.meta && to.meta.requiresAuth) {
    if (!auth || !auth.logged) return next({ name: 'Home' })
    if (to.meta.role === 'admin' && auth.role !== 'admin') return next({ name: 'User' })
    if (to.meta.role === 'user' && auth.role === 'admin') return next({ name: 'Admin' })
  }
  next()
})

export default router