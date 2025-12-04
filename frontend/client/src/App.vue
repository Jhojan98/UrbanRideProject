
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
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTripStore } from '@/services/travelNotifications'
import MainLayout from '@/layouts/MainLayout.vue'
import BlankLayout from '@/layouts/BlankLayout.vue'
import NotificationPopup from '@/components/reservation/NotificationPopup.vue'

const route = useRoute()
const tripStore = useTripStore()

const layoutComponent = computed(() => {
  const layout = route.meta.layout as string | undefined
  if (!layout) return MainLayout
  return layout === 'blank' ? BlankLayout : MainLayout
})

onMounted(() => {
  // Inicializar conexión SSE globalmente cuando la app se monta
  tripStore.connectToSSE()
  // Adjuntar listeners de ciclo de vida para reconectar cuando sea oportuno
  tripStore.attachLifecycleListeners()
  console.log('[App] SSE conectado globalmente')
})

onUnmounted(() => {
  // Desconectar SSE cuando la app se desmonta
  tripStore.disconnect()
  console.log('[App] SSE desconectado')
})
</script>

<style lang="scss">
@import "@/styles/global.scss";

/* Reset: elimina márgenes y padding */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box; /* padding y border se incluyen en el width/height */
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
