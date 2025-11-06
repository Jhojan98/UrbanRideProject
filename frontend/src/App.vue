<template>
  <div class="container-menu">
    <nav class="header-menu">
      <router-link to="/">Home</router-link> |
      <router-link to="/about">About</router-link>
      <router-link :to="{ name: 'Login' }">Iniciar Sesión</router-link>
    </nav>
    <router-view />
  </div>
</template>

/*#app {
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

<style lang="scss">
@import "./styles/styles.scss";
</style>