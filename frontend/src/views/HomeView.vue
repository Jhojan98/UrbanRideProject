<template>
  <div class="home">
    <!-- Sección Hero (principal con llamada a la acción) -->
    <section class="hero">
      <div class="hero-content">
        
        <div class="hero-left">
          <h1 class="hero-title">Rodar tu vida, respirar tu ciudad.</h1>
          <p class="hero-subtitle">Tu ciudad a tu ritmo, de forma sostenible.</p>
          <div class="hero-buttons">
            <button class="butn-primary" @click="goToStationsMap">Ver Mapa de Estaciones</button>
            <button class="btn-secondary" @click="learnMore">Aprende Más</button>
          </div>
        </div>

        <div class="hero-right">
          <img src="@/assets/ciclovia1.jpg" alt="Imagen eco bike" class="hero-img" />
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

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

interface Benefit {
  title: string;
  description: string;
}

const router = useRouter();
const benefits = ref<Benefit[]>([
  {
    title: 'Salud y Bienestar',
    description: 'Mejora tu condición física y reduce el estrés pedaleando por la ciudad.'
  },
  {
    title: 'Impacto Ambiental',
    description: 'Contribuye a un aire más limpio y a una ciudad más verde eligiendo la bicicleta.'
  },
  {
    title: 'Movilidad Eficiente',
    description: 'Evita el tráfico y llega a tu destino de forma rápida y flexible.'
  }
]);

const goToStationsMap = () => {
  // TODO: Implementar navegación al mapa de estaciones
  router.push('/stations-map');
};

const learnMore = () => {
  // TODO: Implementar navegación a la página de información
  router.push('/about');
};
</script>

<style lang="scss">
@import "@/styles/home.scss";
</style>