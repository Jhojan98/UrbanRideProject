<template>
  <div class="home">
    <!-- Hero Section (main with call to action) -->
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


    <!-- Benefits Section -->
    <section class="benefits">
      <h2 class="section-title">{{ $t('home.discover') }}</h2>
      <div class="benefits-grid">
        <!-- Each benefit-card is a service benefit -->
        <div class="benefit-card">
          <i class="fas fa-heart benefit-icon"></i>
          <h3>{{ $t('home.cards.health.title') }}</h3>
          <p>{{ $t('home.cards.health.text') }}</p>
        </div>
        <div class="benefit-card">
          <i class="fas fa-leaf benefit-icon"></i>
          <h3>{{ $t('home.cards.environment.title') }}</h3>
          <p>{{ $t('home.cards.environment.text') }}</p>
        </div>
        <div class="benefit-card">
          <i class="fas fa-bolt benefit-icon"></i>
          <h3>{{ $t('home.cards.efficiency.title') }}</h3>
          <p>{{ $t('home.cards.efficiency.text') }}</p>
        </div>
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
  // TODO: Implement navigation to info page
  router.push('/about');
};
</script>

<style lang="scss" scoped>
@import "@/styles/home.scss";
</style>
