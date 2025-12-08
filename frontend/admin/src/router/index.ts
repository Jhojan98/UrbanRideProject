import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import RegisterView from '@/views/RegisterView.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import BicyclesView from '@/views/BicyclesView.vue'

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
    component: DashboardLayout,
    meta: { isAuthenticated: true, layout: 'main' }
  },
  {
    path: '/bicycles-dashboard',
    name: 'bicyclesDashboard',
    component: BicyclesView,
    meta: { isAuthenticated: true,layout: 'main' }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { isAuthenticated: true, layout: 'main' }
  },
  {
    path: '/users-dashboard',
    name: 'usersDashboard',
    component: UserLayout,
    meta: { isAuthenticated: true, layout: 'main' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
