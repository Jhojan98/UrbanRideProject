

import { createRouter, createWebHistory } from 'vue-router'

// Importa los componentes de las vistas 
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import StationDetail from '../views/StationDetail.vue'
import Reservation from '../views/Reservation.vue'
import Profile from '../views/Profile.vue'


//  ruta a un componente
const routes = [
  { path: '/', name: 'Home', component: Home }, // Página principal
  { path: '/login', name: 'Login', component: Login }, // Página de login
  { path: '/station/:id', name: 'StationDetail', component: StationDetail }, // Detalles de estación con parámetro :id
  { path: '/reservation', name: 'Reservation', component: Reservation }, // Página de reserva
  { path: '/profile', name: 'Profile', component: Profile } // Perfil de usuario
]

// Crea el router 
const router = createRouter({
  history: createWebHistory(), 
  routes 
})

// Exporta el router para usarlo en main.js
export default router