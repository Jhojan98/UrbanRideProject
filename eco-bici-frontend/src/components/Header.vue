<template>
  <!-- Encabezado de la aplicación -->
  <header class="header">
    <div class="header-content">
      <!-- Logo y nombre de la aplicación -->
      <div class="logo">
        <!-- img muestra el logo - src="/logo.jpg" busca en la carpeta public -->
        <img src="/logo.jpg" alt="ecoBici Logo" class="logo-img">
        <span class="logo-text">ecoBici</span>
      </div>
      <!-- Navegación principal -->
      <nav class="nav">
        <!-- 
          router-link es como un <a> pero para vue-router 
          to="/" indica que va a la página principal
        -->
        <router-link to="/" class="nav-link">Inicio</router-link>
        <router-link to="/" class="nav-link">Mapa</router-link>
        <router-link to="/profile" class="nav-link">Perfil</router-link>
      </nav>
      <!-- Botones de autenticación -->
      <div class="auth-buttons">
        <router-link to="/login" class="auth-link">Iniciar Sesión</router-link>
        <router-link to="/login" class="auth-link">Registrarse</router-link>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'Header' // Nombre del componente (para debugging y devtools)
}
</script>

<style scoped>
/* 
  scoped significa que estos estilos solo aplican a este componente
  Vue automáticamente agrega un atributo de datos único a los elementos
*/

.header {
  background-color: #FFFFFF;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1); /* Sombra suave */
  padding: 1rem 0;
  position: fixed; /* Fija el header en la parte superior */
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000; /* Asegura que esté por encima del contenido */
}

.header-content {
  max-width: 1200px; /* Ancho máximo para pantallas grandes */
  margin: 0 auto; /* Centra el contenido */
  display: flex;
  justify-content: space-between; /* Espacia los elementos */
  align-items: center;
  padding: 0 2rem; /* Padding lateral */
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* Espacio entre logo y texto */
}

.logo-img {
  height: 40px;
  width: auto;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2E7D32; /* Verde de la marca */
}

.nav {
  display: flex;
  gap: 2rem; /* Espacio entre enlaces de navegación */
}

.nav-link {
  text-decoration: none;
  color: #004E61;
  font-weight: 500;
  transition: color 0.3s; /* Transición suave para hover */
}

.nav-link:hover {
  color: #2E7D32;
}

/* Estilo para el enlace activo (página actual) */
.nav-link.router-link-active {
  color: #2E7D32;
  font-weight: bold;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
}

.auth-link {
  text-decoration: none;
  color: #004E61;
  font-weight: 500;
  transition: color 0.3s;
}

.auth-link:hover {
  color: #2E7D32;
}

/* Responsive: en móviles cambiar a columna */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .nav {
    gap: 1rem;
  }
}
</style>