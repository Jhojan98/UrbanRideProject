<template>
  <div class="user-dashboard-layout">
    <!-- Barra lateral de navegación -->
    <SidebarComponent
      title="Dashboard de Usuarios"
      :active-component="activeComponent"
      :nav-items="navItems"
      @update:active-component="(value: string) => activeComponent = value as 'users' | 'fines' | 'cac' | 'travel'"
    />

    <!-- Área de contenido principal -->
    <main class="main-content">
      <UserListComponent v-if="activeComponent === 'users'" />
      <FinesComponent v-else-if="activeComponent === 'fines'" />
      <CaCComponent v-else-if="activeComponent === 'cac'" />
      <TravelComponent v-else-if="activeComponent === 'travel'" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SidebarComponent from '@/components/users-dashboard/SidebarComponent.vue'
import UserListComponent from '@/components/users-dashboard/UserListComponent.vue'
import FinesComponent from '@/components/users-dashboard/FinesComponent.vue'
import CaCComponent from '@/components/users-dashboard/CaCComponent.vue'
import TravelComponent from '@/components/users-dashboard/TravelComponent.vue'

const activeComponent = ref<'users' | 'fines' | 'cac' | 'travel'>('users')

const navItems = [
  { value: 'users', label: 'Usuarios' },
  { value: 'fines', label: 'Multas' },
  { value: 'cac', label: 'Quejas y Comentarios' },
  { value: 'travel', label: 'Viajes' }
]
</script>

<style scoped>
.user-dashboard-layout {
  display: flex;
  height: 100vh;
  background-color: var(--color-gray-very-light);
  margin: 0;
  padding: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  background-color: var(--color-gray-very-light);
  margin: 0;
}

@media (max-width: 768px) {
  .user-dashboard-layout {
    flex-direction: column;
  }
}

html[data-theme="dark"] .user-dashboard-layout {
  background-color: var(--color-background-dark);
}

html[data-theme="dark"] .main-content {
  background-color: var(--color-background-dark);
}
</style>
