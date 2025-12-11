<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2>{{ title }}</h2>
    </div>
    <nav class="sidebar-nav">
      <button
        v-for="item in navItems"
        :key="item.value"
        @click="$emit('update:activeComponent', item.value)"
        :class="['nav-item', { active: activeComponent === item.value }]"
      >
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

defineProps<{
  title: string
  activeComponent: string
  navItems: Array<{ value: string; label: string }>
}>()

defineEmits<{
  'update:activeComponent': [value: string]
}>()
</script>

<style scoped>
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

@media (max-width: 768px) {
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

html[data-theme="dark"] .sidebar {
  background: var(--color-primary-dark);
}
</style>
