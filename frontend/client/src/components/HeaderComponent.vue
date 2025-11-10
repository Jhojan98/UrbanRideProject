<template>
  <header class="header">
    <div class="header-content">

      <div class="logo">
        <img src="@/assets/ecorideHeader.png" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <nav class="nav">
        <router-link :to="{name: 'home'}" class="nav-link">Inicio</router-link>
        <router-link :to="{name: 'maps'}" class="nav-link">Mapa</router-link>
        <router-link to="/profile" class="nav-link">Perfil</router-link>

      </nav>

      <div class="auth-buttons">
        <router-link :to="{ name: 'login' }" class="btn-primary">Iniciar Sesión</router-link>
        <router-link :to="{ name: 'signup' }" class="btn-primary">Registrarse</router-link>

        <toggle-theme />
      </div>


    </div>
  </header>
</template>
//TODO: Convertir el logo a webp para optimizar carga

<script setup>
import ToggleTheme from './ToggleTheme.vue';
</script>


<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

