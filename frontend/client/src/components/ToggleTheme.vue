
<template>
  <button @click="toggleTheme" class="theme-toggle">
    <img 
      v-if="isDark" 
      src="@/assets/icons/dark.png" 
      alt="Tema Claro" 
    />
    <img 
      v-else 
      src="@/assets/icons/light.png" 
      alt="Tema Oscuro" 
    />
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
.theme-toggle {
  background-color: #ffff;
  border: none;
  padding: 0.4rem;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  /* sombra sutil para que no se pierda */
  box-shadow: 0 0 4px rgba(0,0,0,0.25);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

/* ícono */
.theme-toggle img {
  width: 24px;
  height: 24px;
}

/* hover */
.theme-toggle:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 6px rgba(0,0,0,0.35);
  background-color: #ffff;
}

/* click */
.theme-toggle:active {
  transform: scale(0.94);
}

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
  color: white;
}

.theme-toggle img {
  width: 24px;
  height: 24px;
  cursor: pointer;
  background: transparent;
}
</style>
