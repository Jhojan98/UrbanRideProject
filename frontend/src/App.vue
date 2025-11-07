<template>

<div id="app">

  <Header />
    <main class="main-content">
      <router-view />
    </main>
    
    <HeaderComponent />
    <FooterComponent />
  </div>
</template>

<script setup>
// Importa los componentes Header y Footer
import HeaderComponent from './components/HeaderComponent.vue';
import FooterComponent from './components/FooterComponent.vue';


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
  margin-top: 80px; /*   header fijo */
}
</style>