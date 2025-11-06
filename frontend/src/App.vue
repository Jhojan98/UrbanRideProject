<template>

<div id="app">

  <Header />
    <main class="main-content">
      <router-view />
    </main>
    
    <Footer />
  </div>
</template>

<script>
// Importa los componentes Header y Footer 
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'

export default {
  name: 'App', 
  components: {
    Header, 
    Footer  
  }
}
</script>

<style>
/* Estilos globales 

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