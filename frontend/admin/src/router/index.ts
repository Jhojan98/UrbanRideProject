import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import LoginView from "@/views/LoginView.vue";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import RegisterView from "@/views/RegisterView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import AdminManagementView from "@/views/AdminManagementView.vue";
import BicyclesView from "@/views/BicyclesView.vue";
import useAuthStore from "@/stores/auth";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "login",
    component: LoginView,
    meta: { layout: "main" },
  },
  {
    path: "/stations-dashboard",
    name: "stationsDashboard",
    component: DashboardLayout,
    meta: { requiresAuth: true },
  },
  {
    path: "/bicycles-dashboard",
    name: "bicyclesDashboard",
    component: BicyclesView,
    meta: { requiresAuth: true },
  },
  {
    path: "/register",
    name: "register",
    component: RegisterView,
    meta: { requiresAuth: true, layout: "main" },
  },
  {
    path: "/users-dashboard",
    name: "usersDashboard",
    component: UserLayout,
    meta: { layout: "main", requiresAuth: true },
  },
  {
    path: "/admin-management",
    name: "adminManagement",
    component: AdminManagementView,
    meta: { layout: "main", requiresAuth: true },
  },

  /*{
    path: '/admin-dashboard',
    name: 'adminDashboard',
    component: MainLayout,
    meta: { layout: 'main' }
  }*/
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

// Navigation guard para proteger rutas y gestionar expiración por inactividad
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

  // Verificar si el token ha expirado por inactividad
  if (authStore.token && authStore.checkTokenExpiration()) {
    // Token expirado, redirigir a login
    next({ name: 'login' });
    return;
  }

  if (requiresAuth && !authStore.token) {
    // Redirigir a login si la ruta requiere autenticación y no hay token
    next({ name: 'login' });
  } else if (to.name === 'login' && authStore.token) {
    // Si ya está autenticado y intenta ir a login, redirigir a dashboard
    next({ name: 'stationsDashboard' });
  } else {
    // Renovar actividad en cada navegación
    if (authStore.token) {
      authStore.renewActivity();
    }
    next();
  }
});

export default router;
