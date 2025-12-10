<template>
  <MainLayout>
    <SessionWarning />
    <router-view />
  </MainLayout>
</template>

<script setup  lang="ts">
import MainLayout from '@/layouts/MainLayout.vue'
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const idleTimer = ref<number | null>(null)
const IDLE_LIMIT_MS = 5 * 60 * 1000 // 5 minutos

const clearIdleTimer = () => {
  if (idleTimer.value) {
    clearTimeout(idleTimer.value)
    idleTimer.value = null
  }
}

const handleIdle = () => {
  if (authStore.token) {
    console.warn('[Idle] Sesión expirada por inactividad')
    authStore.logout()
    router.push({ name: 'login' })
  }
}

const resetIdleTimer = () => {
  clearIdleTimer()
  idleTimer.value = window.setTimeout(handleIdle, IDLE_LIMIT_MS)
}

const activityEvents = ['mousemove', 'keydown', 'click', 'touchstart']

onMounted(() => {
  activityEvents.forEach(event => window.addEventListener(event, resetIdleTimer))
  resetIdleTimer()
})

onUnmounted(() => {
  activityEvents.forEach(event => window.removeEventListener(event, resetIdleTimer))
  clearIdleTimer()
})
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
