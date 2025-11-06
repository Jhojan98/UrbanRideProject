//Aquí se puede poner la land page
<template>
  <header class="main-header">
    <nav class="header-menu">
      <router-link to="/">Home</router-link>
      <router-link to="/about">About</router-link>
      <router-link :to="{ name: 'Login' }">Iniciar Sesión</router-link>

      <ToggleTheme />
    </nav>
  </header>

  <main class="page-content">
    <router-view />
  </main>
</template>

<script setup lang="ts">


import BalanceComponent from './components/BalanceComponent.vue';
import ToggleTheme from './components/ToggleTheme.vue';
BalanceComponent
</script>

<style lang="scss">
@import "./styles/global.scss";
@import "./styles/forms.scss";

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
