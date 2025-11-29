<template>
  <div class="home">
    <!-- Sección Hero (principal con llamada a la acción) -->
    <section class="hero">
      <div class="hero-content">

        <div class="hero-left">
          <h1 class="hero-title">{{ $t('home.hero.title') }}</h1>
          <p class="hero-subtitle">{{ $t('home.hero.subtitle') }}</p>
          <div class="hero-buttons">
            <router-link :to="{ name: 'maps' }" class="buttn-primary">
              {{ $t('home.hero.buttons.stations') }}
            </router-link>
              <button class="btn-secondary" @click="learnMore">{{ $t('home.hero.buttons.learnMore') }}</button>
          </div>
        </div>

        <div class="hero-right">
          <img src="@/assets/ciclovia1.webp" alt="Imagen eco bike" class="hero-img" />
        </div>

      </div>
    </section>


    <!-- Sección de Beneficios -->
    <section class="benefits">
      <h2 class="section-title">{{ $t('home.discover') }}</h2>
      <div class="benefits-grid">
        <!-- Cada benefit-card es un beneficio del servicio -->
        <div class="benefit-card">
          <h3>{{ $t('home.cards.health.title') }}</h3>
          <p>{{ $t('home.cards.health.text') }}</p>
        </div>
        <div class="benefit-card">
          <h3>{{ $t('home.cards.environment.title') }}</h3>
          <p>{{ $t('home.cards.environment.text') }}</p>
        </div>
        <div class="benefit-card">
          <h3>{{ $t('home.cards.efficiency.title') }}</h3>
          <p>{{ $t('home.cards.efficiency.text') }}</p>
        </div>
      </div>
    </section>

    <!-- Sección de Estaciones -->
    <section class="stations">
      <div class="stations-header">
        <h2 class="section-title">{{ $t('home.stations.title') }}</h2>
        <button class="btn-outline">{{ $t('home.stations.button') }}</button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
// import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';


const router = useRouter();
const { t: $t } = useI18n();

const learnMore = () => {
  // TODO: Implementar navegación a la página de información
  router.push('/about');
};
</script>

<style lang="scss" scoped>
@import "@/styles/home.scss";
</style>