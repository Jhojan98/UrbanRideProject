<template>
  <header class="header">
    <div class="header-content">

      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <nav class="nav">
        <router-link :to="{name: 'home'}" class="nav-link">Inicio</router-link>
        <router-link :to="{name: 'maps'}" class="nav-link">Mapa</router-link>
        <router-link :to="{name: 'profile'}" class="nav-link">Perfil</router-link>
      </nav>

      <div class="auth-buttons">
        <!-- Mostrar botones de Login/Signup solo en home y cuando no esté autenticado -->
        <template v-if="!isAuthenticated && !isAuthRoute">
          <router-link :to="{ name: 'login' }" class="btn-primary">Iniciar Sesión</router-link>
          <router-link :to="{ name: 'signup' }" class="btn-primary">Registrarse</router-link>
        </template>

        <!-- Mostrar botón de Logout cuando esté autenticado -->
        <button v-if="isAuthenticated" @click="logout" class="btn-primary btn-logout">
          Cerrar Sesión
        </button>

        <toggle-theme />
      </div>

    </div>
  </header>
</template>
//TODO: Convertir el logo a webp para optimizar carga

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import ToggleTheme from './ToggleTheme.vue';
import userAuth from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = userAuth();

// Usar storeToRefs para mantener la reactividad del store
const { token } = storeToRefs(authStore);

// Verificar si el usuario está autenticado
const isAuthenticated = computed(() => !!token.value);

// Verificar si estamos en una ruta de autenticación (login, signup, verify-otp)
const isAuthRoute = computed(() => {
  const authRoutes = ['login', 'signup', 'verify-otp'];
  return authRoutes.includes(route.name as string);
});

// Manejar el cierre de sesión
const logout = async () => {
  await authStore.logout();
  router.push({ name: 'home' });
};
</script>


<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

