<template>
  <MainLayout>
    <SessionWarning />
    <router-view />
  </MainLayout>
</template>

<script setup  lang="ts">
import MainLayout from '@/layouts/MainLayout.vue'
import SessionWarning from '@/components/SessionWarning.vue'
import { useActivityTracker } from '@/composables/useActivityTracker'

// Activar el tracker de actividad globalmente
useActivityTracker()
</script>
<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
</style>
