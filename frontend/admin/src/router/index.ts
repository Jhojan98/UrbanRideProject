import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import RegisterView from '@/views/RegisterView.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import AdminManagementView from '@/views/AdminManagementView.vue'
import BicyclesView from '@/views/BicyclesView.vue'
import RedistributionComponent from '@/components/redistribution/RedistributionComponent.vue'

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
    path: '/bicycles-dashboard',
    name: 'bicyclesDashboard',
    component: BicyclesView
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
  },
  {
    path: '/admin-management',
    name: 'adminManagement',
    component: AdminManagementView,
    meta: { layout: 'main' }
  },
  
  /*{
    path: '/admin-dashboard',
    name: 'adminDashboard',
    component: MainLayout,
    meta: { layout: 'main' }
  }*/
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
