
<template>
  <router-view v-slot="{ Component }">
    <component :is="layoutComponent">
      <component :is="Component" />
    </component>
  </router-view>

  <!-- Componente de notificaciones global -->
  <NotificationPopup />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTripStore } from '@/services/travelNotifications'
import userAuth from '@/stores/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import BlankLayout from '@/layouts/BlankLayout.vue'
import NotificationPopup from '@/components/reservation/NotificationPopup.vue'

const route = useRoute()
const router = useRouter()
const tripStore = useTripStore()
const authStore = userAuth()

// Idle logout (5 minutos)
const idleTimer = ref<number | null>(null)
const IDLE_LIMIT_MS = 5 * 60 * 1000
const activityEvents = ['mousemove', 'keydown', 'click', 'touchstart']

const clearIdleTimer = () => {
  if (idleTimer.value) {
    clearTimeout(idleTimer.value)
    idleTimer.value = null
  }
}

const handleIdle = () => {
  if (authStore.token) {
    console.warn('[Idle] Sesión expirada por inactividad (cliente)')
    authStore.logout()
    router.push({ name: 'login' })
  }
}

const resetIdleTimer = () => {
  clearIdleTimer()
  idleTimer.value = window.setTimeout(handleIdle, IDLE_LIMIT_MS)
}

const layoutComponent = computed(() => {
  const layout = route.meta.layout as string | undefined
  if (!layout) return MainLayout
  return layout === 'blank' ? BlankLayout : MainLayout
})

onMounted(() => {
  // Initialize SSE connection globally when app mounts
  tripStore.connectToSSE()
  // Attach lifecycle listeners to reconnect when needed
  tripStore.attachLifecycleListeners()
  console.log('[App] SSE connected globally')

  // Idle timer listeners
  activityEvents.forEach(event => window.addEventListener(event, resetIdleTimer))
  resetIdleTimer()
})

onUnmounted(() => {
  // Disconnect SSE when app unmounts
  tripStore.disconnect()
  console.log('[App] SSE disconnected')

  activityEvents.forEach(event => window.removeEventListener(event, resetIdleTimer))
  clearIdleTimer()
})
</script>

<style lang="scss">
@import "@/styles/global.scss";

/* Reset: removes margins and padding */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box; /* padding and border are included in width/height */
}

body {
  font-family: 'Arial', sans-serif;
  background-color: #FFFFFF;
  color: #004E61;
  line-height: 1.6;
}

#app {
  min-height: 100vh; /* Ocupa toda la altura de la ventana */
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1; /*  footer al fondo */
}
</style>
