//Aquí se puede poner la land page

<template>
  <nav>
    <ToggleTheme />
  </nav>
  <router-view/>
  
</template>

<script setup lang="ts">

import ToggleTheme from './components/ToggleTheme.vue';
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
