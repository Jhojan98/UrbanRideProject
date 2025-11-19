<template>
  <header class="header">
    <div class="header-content">

      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <nav class="nav">
        <!-- <router-link :to="{name: 'home'}" class="nav-link">Inicio</router-link>
        <router-link :to="{name: 'maps'}" class="nav-link">Mapa</router-link>
        <router-link :to="{name: 'profile'}" class="nav-link">Perfil</router-link> -->
      </nav>

      <div class="auth-buttons">


        <toggle-theme />
      </div>

    </div>
  </header>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import ToggleTheme from './ToggleTheme.vue';


const route = useRoute();
const router = useRouter();


// Usar storeToRefs para mantener la reactividad del store


// Verificar si el usuario está autenticado


// Verificar si estamos en una ruta de autenticación (login, signup, verify-otp)
const isAuthRoute = computed(() => {
  const authRoutes = ['login', 'signup', 'verify-otp'];
  return authRoutes.includes(route.name as string);
});

// Manejar el cierre de sesión
const logout = async () => {

  router.push({ name: 'home' });
};
</script>


<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

