<template>
  <header class="header">
    <div class="header-content">

      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <nav class="nav">
        <router-link :to="{name: 'home'}" class="nav-link">{{ $t('nav.home') }}</router-link>
        <router-link :to="{name: 'maps'}" class="nav-link">{{ $t('nav.maps') }}</router-link>
        <router-link :to="{name: 'profile'}" class="nav-link">{{ $t('nav.profile') }}</router-link>
      </nav>

      <div class="auth-buttons">
        <!-- Mostrar botones de Login/Signup solo en home y cuando no esté autenticado -->
        <template v-if="!isAuthenticated && !isAuthRoute">
          <router-link :to="{ name: 'login' }" class="btn-primary">{{ $t('nav.login') }}</router-link>
          <router-link :to="{ name: 'signup' }" class="btn-primary">{{ $t('nav.signup') }}</router-link>
        </template>

        <!-- Mostrar botón de Logout cuando esté autenticado -->
        <button v-if="isAuthenticated" @click="logout" class="btn-primary btn-logout">
          {{ $t('nav.logout') }}
        </button>

        <div class="lang-switcher">
          <select v-model="locale" @change="persistLocale" class="lang-select">
            <option value="es">ES</option>
            <option value="en">EN</option>
          </select>
        </div>

        <toggle-theme />
      </div>

    </div>
  </header>
</template>
//TODO: Convertir el logo a webp para optimizar carga

<script setup lang="ts">
import { computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import ToggleTheme from './ToggleTheme.vue';
import userAuth from '@/stores/auth';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const authStore = userAuth();

// Usar storeToRefs para mantener la reactividad del store
const { token } = storeToRefs(authStore);

// Verificar si el usuario está autenticado
const isAuthenticated = computed(() => !!token.value);

// Verificar si estamos en una ruta de autenticación (login, signup, verify-email)
const isAuthRoute = computed(() => {
  const authRoutes = ['login', 'signup', 'verify-email'];
  return authRoutes.includes(route.name as string);
});

// Manejar el cierre de sesión
const logout = async () => {
  await authStore.logout();
  router.push({ name: 'home' });
};

const { locale } = useI18n();
const persistLocale = () => {
  localStorage.setItem('locale', locale.value);
};
watch(locale, persistLocale);
</script>


<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

