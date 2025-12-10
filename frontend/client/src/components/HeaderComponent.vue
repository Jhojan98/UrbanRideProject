<template>
  <header class="header" :class="{ 'header-mobile': isMobile, 'sidebar-open': isSidebarOpen }">
    <!-- Mobile Header: Hamburger + Logo + Utilities -->
    <div v-if="isMobile" class="mobile-header">
      <button class="hamburger-btn" @click="isSidebarOpen = !isSidebarOpen" :aria-label="$t('common.menu')">
        <span></span>
        <span></span>
        <span></span>
      </button>

      <div class="logo-mobile">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <!-- Mobile utilities (visible in header) -->
      <div class="mobile-header-utilities">
        <div class="lang-switcher">
          <select v-model="locale" @change="persistLocale" class="lang-select">
            <option value="es">ES</option>
            <option value="en">EN</option>
          </select>
        </div>
        <toggle-theme />
      </div>
    </div>

    <!-- Backdrop para cerrar sidebar -->
    <div v-if="isMobile && isSidebarOpen" class="sidebar-backdrop" @click="isSidebarOpen = false"></div>

    <!-- Desktop Header Content -->
    <div v-if="!isMobile" class="header-content">
      <div class="logo">
        <img src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span class="logo-text">ECORIDE</span>
      </div>

      <!-- Desktop Navigation -->
      <nav class="nav">
        <template v-if="isAuthenticated">
          <router-link :to="{name: 'home'}" class="nav-link">{{ $t('nav.home') }}</router-link>
          <router-link :to="{name: 'maps'}" class="nav-link">{{ $t('nav.maps') }}</router-link>
          <router-link :to="{name: 'profile'}" class="nav-link">{{ $t('nav.profile') }}</router-link>
        </template>
      </nav>

      <!-- Desktop Auth Buttons -->
      <div class="auth-buttons">
        <template v-if="showAuthButtons">
          <router-link :to="{ name: 'login' }" class="btn-primary">{{ $t('nav.login') }}</router-link>
        </template>
        <button v-if="showLogoutButton" @click="logout" class="btn-primary btn-logout">
          {{ $t('nav.logout') }}
        </button>
      </div>

      <!-- Desktop Utilities (Theme + Language) -->
      <div class="header-utilities">
        <div class="lang-switcher">
          <select v-model="locale" @change="persistLocale" class="lang-select">
            <option value="es">ES</option>
            <option value="en">EN</option>
          </select>
        </div>
        <toggle-theme />
      </div>
    </div>

    <!-- Mobile Sidebar Navigation -->
    <div class="mobile-sidebar" :class="{ 'sidebar-visible': isSidebarOpen }">
      <!-- Close button -->
      <button class="close-btn" @click="isSidebarOpen = false" :aria-label="$t('common.close')">
        ✕
      </button>

      <!-- Mobile Navigation Links -->
      <nav class="mobile-nav">
        <template v-if="isAuthenticated">
          <router-link :to="{name: 'home'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.home') }}</router-link>
          <router-link :to="{name: 'maps'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.maps') }}</router-link>
          <router-link :to="{name: 'profile'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.profile') }}</router-link>
        </template>
      </nav>

      <!-- Mobile Auth Buttons -->
      <div class="mobile-auth-buttons">
        <template v-if="showAuthButtons">
          <router-link :to="{ name: 'login' }" class="btn-primary" @click="closeSidebarOnMobile">
            {{ $t('nav.login') }}
          </router-link>
        </template>

        <button v-if="showLogoutButton" @click="logout" class="btn-primary btn-logout">
          {{ $t('nav.logout') }}
        </button>
      </div>
    </div>
  </header>
</template>
//TODO: Convertir el logo a webp para optimizar carga

<script setup lang="ts">
import { computed, watch, ref, onMounted, onUnmounted } from 'vue';
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

// Estado del sidebar
const isSidebarOpen = ref(false);
const isMobile = ref(window.innerWidth <= 768);

// Verificar si el usuario está autenticado
const isAuthenticated = computed(() => {
  const auth = !!token.value;
  console.log('[Header] isAuthenticated:', auth, 'token:', token.value);
  return auth;
});

// Verificar si estamos en una ruta de autenticación (login, signup, verify-email)
const isAuthRoute = computed(() => {
  const authRoutes = ['login', 'signup', 'verify-email'];
  const isAuth = authRoutes.includes(route.name as string);
  console.log('[Header] isAuthRoute:', isAuth, 'route:', route.name);
  return isAuth;
});

// Mostrar botones de login/signup solo cuando NO esté autenticado y NO esté en ruta de auth
const showAuthButtons = computed(() => {
  const show = !isAuthenticated.value && !isAuthRoute.value;
  console.log('[Header] showAuthButtons:', show);
  return show;
});

// Mostrar botón de logout solo cuando esté autenticado
const showLogoutButton = computed(() => {
  const show = isAuthenticated.value;
  console.log('[Header] showLogoutButton:', show);
  return show;
});

// Cerrar sidebar automáticamente en móviles al cambiar de ruta
watch(() => route.path, () => {
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
});

// Manejar el cierre de sesión
const logout = async () => {
  await authStore.logout();
  router.push({ name: 'home' });
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
};

// Cerrar sidebar al hacer click en un link en móviles
const closeSidebarOnMobile = () => {
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
};

// Detectar cambios en el tamaño de la ventana
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768;
  // Cerrar sidebar si se cambia a desktop
  if (!isMobile.value && isSidebarOpen.value) {
    isSidebarOpen.value = false;
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

const { locale } = useI18n();
const persistLocale = () => {
  localStorage.setItem('locale', locale.value);
};
watch(locale, persistLocale);
</script>


<style lang="scss" scoped>
@import "@/styles/header.scss";
</style>

