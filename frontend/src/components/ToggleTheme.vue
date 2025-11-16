<!-- ThemeToggle.vue -->
<template>
  <button @click="toggleTheme">
    {{ isDark ? 'Claro' : 'Oscuro' }}
  </button>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const THEME_KEY = 'app-theme'

const isDark = ref(false)

// Cambia el atributo data-theme en <html> para que los estilos CSS reaccionen al modo claro/oscuro
const applyTheme = (dark) => {
  const root = document.documentElement
  if (dark) {
    root.setAttribute('data-theme', 'dark')
    root.classList.remove('light')
    root.classList.add('dark')
  } else {
    root.setAttribute('data-theme', 'light')
    root.classList.remove('dark')
    root.classList.add('light')
  }
  localStorage.setItem(THEME_KEY, dark ? 'dark' : 'light')
}

const toggleTheme = () => {
  isDark.value = !isDark.value
}

watch(isDark, (newVal) => applyTheme(newVal))

onMounted(() => {
  const saved = localStorage.getItem(THEME_KEY)
  isDark.value = saved === 'dark'
})
</script>

<style>
:root.light {
  --bg: #fff;
  --text: #000;
}
:root.dark {
  --bg: #121212;
  --text: #fff;
}
body {
  background: var(--bg);
  color: var(--text);
}
</style>
