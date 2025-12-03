<template>
  <div class="user-dashboard-layout">
    <!-- Barra lateral de navegación -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>Dashboard de Usuarios</h2>
      </div>
      <nav class="sidebar-nav">
        <button
          @click="activeComponent = 'users'"
          :class="['nav-item', { active: activeComponent === 'users' }]"
        >
          <span>Usuarios</span>
        </button>
        <button
          @click="activeComponent = 'fines'"
          :class="['nav-item', { active: activeComponent === 'fines' }]"
        >
          <span>Multas</span>
        </button>
        <button
          @click="activeComponent = 'cac'"
          :class="['nav-item', { active: activeComponent === 'cac' }]"
        >
          <span>Quejas y Comentarios</span>
        </button>
        <button
          @click="activeComponent = 'travel'"
          :class="['nav-item', { active: activeComponent === 'travel' }]"
        >
          <span>Quejas y Comentarios</span>
        </button>
      </nav>
    </aside>

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
import UserListComponent from '@/components/users-dashboard/UserListComponent.vue'
import FinesComponent from '@/components/users-dashboard/FinesComponent.vue'
import CaCComponent from '@/components/users-dashboard/CaCComponent.vue'
import TravelComponent from '@/components/users-dashboard/TravelComponent.vue'

const activeComponent = ref<'users' | 'fines' | 'cac' | 'travel'>('users')
</script>

<style scoped>
.user-dashboard-layout {
  display: flex;
  height: 100vh;
  background-color: var(--color-gray-very-light);
  margin: 0;
  padding: 0;
}

.sidebar {
  width: 250px;
  background: var(--color-primary-light);
  color: var(--color-white);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  margin: 0;
  padding: 0;
}

.sidebar-header {
  padding: 2rem 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-white);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  text-align: left;
  font-weight: 500;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
  color: var(--color-white);
}

.nav-item.active {
  background-color: var(--color-green-light);
  color: var(--color-white);
  font-weight: 600;
  border-left: 4px solid var(--color-white);
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

  .sidebar {
    width: 100%;
    height: auto;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    flex-direction: column;
    padding: 0.75rem;
    text-align: center;
  }
}

html[data-theme="dark"] .user-dashboard-layout {
  background-color: var(--color-background-dark);
}

html[data-theme="dark"] .main-content {
  background-color: var(--color-background-dark);
}

html[data-theme="dark"] .sidebar {
  background: var(--color-primary-dark);
}
</style>
