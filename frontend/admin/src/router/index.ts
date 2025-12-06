import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import RegisterView from '@/views/RegisterView.vue'
import UserLayout from '@/layouts/UserLayout.vue'
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'login',
    component: LoginView,
    meta: { layout: 'main' }
  },
  {
    path: '/stations-dashboard',
    name: 'stationsDashboard',
    component: DashboardLayout
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { layout: 'main' }
  },
  {
  path: '/users-dashboard',
  name: 'usersDashboard',
  component: UserLayout,
  meta: { layout: 'main' }
}
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
