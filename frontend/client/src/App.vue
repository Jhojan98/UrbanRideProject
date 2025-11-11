<template>
  <router-view v-slot="{ Component }">
    <component :is="layoutComponent">
      <component :is="Component" />
    </component>
  </router-view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import BlankLayout from '@/layouts/BlankLayout.vue'

const route = useRoute()

const layoutComponent = computed(() => {
  const layout = route.meta.layout as string | undefined
  if (!layout) return MainLayout
  return layout === 'blank' ? BlankLayout : MainLayout
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