//Aquí se puede poner la land page

<template>
  <nav>
    <router-link to="/">Home</router-link> |
    <router-link to="/about">About</router-link>
    <!-- Botón para cambiar entre tema claro y oscuro -->
    <ToggleTheme />
  </nav>
  <router-view/>
  
</template>

<script setup lang="ts">
// Importamos el componente que controla el tema
import ToggleTheme from './components/ToggleTheme.vue';
</script>

<style lang="scss">
@import "./styles/forms.scss";

/* Estilos globales para aplicar tema */
body {
  background: var(--bg);
  color: var(--text);
  margin: 0;
  font-family: Avenir, Helvetica, Arial, sans-serif;
}

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
