<template>
  <header class="header">
    <div class="header-content">
      <button 
        :class="['hamburger-menu', { active: menuOpen }]" 
        @click="menuOpen = !menuOpen"
        aria-label="Toggle menu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>
      <nav v-if="authStore.token" class="nav">
        <router-link :to="{name: 'stationsDashboard'}" class="nav-link">
          Dashboard de Estaciones
        </router-link>
        <router-link :to="{name: 'bicyclesDashboard'}" class="nav-link">
          Dashboard de Bicicletas
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
        <button v-if="authStore.token" class="logout-btn desktop-only" @click="logout">{{ t('dashboard.logout') }}</button>
        <router-link v-else :to="{name: 'login'}" class="login-btn desktop-only">{{ t('dashboard.login') || 'Iniciar Sesión' }}</router-link>
        <button class="lang-switch" @click="toggleLocale">{{ currentLocaleLabel }}</button>
        <toggle-theme />
      </div>
    </div>
    <nav v-if="authStore.token" v-show="menuOpen" class="mobile-nav active">
      <router-link 
        :to="{name: 'stationsDashboard'}" 
        class="nav-link"
        @click="menuOpen = false"
      >
        Dashboard de Estaciones
      </router-link>
      <router-link 
        :to="{name: 'usersDashboard'}" 
        class="nav-link"
        @click="menuOpen = false"
      >
        Dashboard de Usuarios
      </router-link>
      <router-link 
        :to="{name: 'adminManagement'}" 
        class="nav-link"
        @click="menuOpen = false"
      >
        Gestión de Admin
      </router-link>
      <router-link 
        :to="{name: 'register'}" 
        class="nav-link"
        @click="menuOpen = false"
      >
        Registro de Admins
      </router-link>
      <button v-if="authStore.token" class="logout-btn-mobile" @click="logout">
        <span class="material-symbols-outlined">logout</span>
        {{ t('dashboard.logout') }}
      </button>
    </nav>
    <nav v-else v-show="menuOpen" class="mobile-nav active">
      <router-link :to="{name: 'login'}" class="logout-btn-mobile" @click="menuOpen = false">
        <span class="material-symbols-outlined">login</span>
        {{ t('dashboard.login') || 'Iniciar Sesión' }}
      </router-link>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import ToggleTheme from './ToggleTheme.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const { locale, t } = useI18n({ useScope: 'global' })
const authStore = useAuthStore()

const menuOpen = ref(false)

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

.mobile-nav {
  .nav-link {
    text-decoration: none;
    color: var(--color-background-light);
    font-weight: 500;
    font-size: 0.9rem;
    transition: color 0.3s;
    white-space: nowrap;
    padding: 0.5rem 0;
    display: block;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      color: var(--color-surface-light);
    }

    &.router-link-active {
      color: var(--color-background-light);
      font-weight: bold;
    }
  }

  .logout-btn-mobile {
    width: 100%;
    padding: 0.75rem;
    margin-top: 0.5rem;
    background-color: var(--color-background-light);
    border: none;
    border-radius: 5px;
    color: var(--color-primary-light);
    font-weight: 600;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;

    .material-symbols-outlined {
      font-size: 1.2rem;
    }

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 6px rgba(0,0,0,0.15);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
  }
}
</style>

