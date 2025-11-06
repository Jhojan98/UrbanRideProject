<template>
  <div class="home">
    <!-- Sección Hero (principal con llamada a la acción) -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">Rodar tu vida, respirar tu ciudad.</h1>
        <p class="hero-subtitle">Tu ciudad a tu ritmo, de forma sostenible.</p>
        <div class="hero-buttons">
          <!-- Botones que por ahora no hacen nada (solo diseño) -->
          <button class="btn-primary">Ver Mapa de Estaciones</button>
          <button class="btn-secondary">Aprende Más</button>
        </div>
      </div>
    </section>

    <!-- Sección de Beneficios -->
    <section class="benefits">
      <h2 class="section-title">Descubre ecoBici</h2>
      <div class="benefits-grid">
        <!-- Cada benefit-card es un beneficio del servicio -->
        <div class="benefit-card">
          <h3>Salud y Bienestar</h3>
          <p>Mejora tu condición física y reduce el estrés pedaleando por la ciudad.</p>
        </div>
        <div class="benefit-card">
          <h3>Impacto Ambiental</h3>
          <p>Contribuye a un aire más limpio y a una ciudad más verde eligiendo la bicicleta.</p>
        </div>
        <div class="benefit-card">
          <h3>Movilidad Eficiente</h3>
          <p>Evita el tráfico y llega a tu destino de forma rápida y flexible.</p>
        </div>
      </div>
    </section>

    <!-- Sección de Estaciones -->
    <section class="stations">
      <div class="stations-header">
        <h2 class="section-title">Nuestra Red de Estaciones</h2>
        <button class="btn-outline">Ver Mapa Completo</button>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Home' // Nombre de esta página/vista
}
</script>

<style scoped>
.home {
  min-height: calc(100vh - 200px); /* Altura mínima menos header y footer */
}

.hero {
  background: linear-gradient(135deg, #2E7D32 0%, #004E61 100%); /* Degradado verde a azul */
  color: #FFFFFF;
  padding: 4rem 2rem;
  text-align: center;
  margin-top: -80px; /* Compensa el header fijo */
  padding-top: 120px; /* Añade padding superior para que el contenido no quede detrás del header */
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3rem;
  margin-bottom: 1rem;
  font-weight: bold;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9; /* Texto ligeramente transparente */
}

.hero-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap; /* En móviles los botones se apilan */
}

/* Estilos para botones primarios */
.btn-primary {
  background-color: #FBC02D; /* Amarillo */
  color: #004E61; /* Azul oscuro */
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 1rem;
}

.btn-primary:hover {
  background-color: #F9A825; /* Amarillo más oscuro al hover */
}

/* Estilos para botones secundarios */
.btn-secondary {
  background-color: transparent;
  color: #FFFFFF;
  border: 2px solid #FFFFFF;
  padding: 12px 24px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 1rem;
}

.btn-secondary:hover {
  background-color: #FFFFFF;
  color: #004E61; /* Invierte colores al hover */
}

.benefits {
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  color: #004E61;
  font-size: 2.5rem;
  margin-bottom: 3rem;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* Grid responsive */
  gap: 2rem;
}

.benefit-card {
  background: #FFFFFF;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); /* Sombra suave */
  text-align: center;
  transition: transform 0.3s; /* Transición para el efecto hover */
}

.benefit-card:hover {
  transform: translateY(-5px); /* Efecto de levantar al hover */
}

.benefit-card h3 {
  color: #2E7D32;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.benefit-card p {
  color: #666;
  line-height: 1.6;
}

.stations {
  padding: 4rem 2rem;
  background-color: #f8f9fa; /* Fondo gris claro */
}

.stations-header {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap; /* En móviles se apila */
  gap: 1rem;
}

.btn-outline {
  background-color: transparent;
  color: #2E7D32;
  border: 2px solid #2E7D32;
  padding: 12px 24px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.btn-outline:hover {
  background-color: #2E7D32;
  color: #FFFFFF;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem; /* Título más pequeño en móviles */
  }
  
  .hero-buttons {
    flex-direction: column; /* Botones en columna en móviles */
    align-items: center;
  }
  
  .stations-header {
    flex-direction: column;
    text-align: center;
  }
  
  .section-title {
    font-size: 2rem;
  }
}
</style>