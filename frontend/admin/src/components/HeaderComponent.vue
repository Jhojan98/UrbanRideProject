<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>
      <nav class="nav">
        <router-link :to="{name: 'stationsDashboard'}" class="nav-link">
          Dashboard de Estaciones
        </router-link>
        <router-link :to="{name: 'usersDashboard'}" class="nav-link">
          Dashboard de Usuarios
        </router-link>
        <router-link :to="{name: 'adminManagement'}" class="nav-link">
          Gestión de Admin
        </router-link>
        <router-link :to="{name: 'register'}" class="nav-link">
          Registro de Admins
        </router-link>
      </nav>
      <div class="auth-buttons">
        <button class="lang-switch" @click="toggleLocale">{{ currentLocaleLabel }}</button>
        <button class="logout-btn" @click="logout">{{ t('dashboard.logout') }}</button>
        <toggle-theme />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import ToggleTheme from './ToggleTheme.vue'
import { useI18n } from 'vue-i18n'
import useAuthStore from '@/stores/auth'

const router = useRouter()
const { locale, t } = useI18n({ useScope: 'global' })
const authStore = useAuthStore()

// Alternar entre idiomas español e inglés
const toggleLocale = () => {
  locale.value = locale.value === 'es' ? 'en' : 'es'
}

// Etiqueta del idioma actual
const currentLocaleLabel = computed(() => (locale.value === 'es' ? 'ES' : 'EN'))

// Manejar el cierre de sesión
const logout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

