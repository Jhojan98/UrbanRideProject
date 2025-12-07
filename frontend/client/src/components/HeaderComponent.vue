<template>
  <header class="header" :class="{ 'header-mobile': isMobile, 'sidebar-open': isSidebarOpen }">
    <!-- Mobile: Hamburger button -->
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
      <div class="header-spacer"></div>
    </div>

    <!-- Sidebar/Desktop Header -->
    <div class="header-content" :class="{ 'sidebar-visible': isSidebarOpen || !isMobile }">
      <!-- Close button for mobiles -->
      <button v-if="isMobile" class="close-btn" @click="isSidebarOpen = false" :aria-label="$t('common.close')">
        ✕
      </button>

      <div class="logo">
        <img v-if="!isMobile" src="@/assets/ecorideHeader.webp" alt="ecoRideLogo" class="logo-img">
        <span v-if="!isMobile" class="logo-text">ECORIDE</span>
      </div>

      <nav class="nav">
        <!-- Only show navigation when authenticated -->
        <template v-if="isAuthenticated">
          <router-link :to="{name: 'home'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.home') }}</router-link>
          <router-link :to="{name: 'maps'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.maps') }}</router-link>
          <router-link :to="{name: 'profile'}" class="nav-link" @click="closeSidebarOnMobile">{{ $t('nav.profile') }}</router-link>
        </template>
      </nav>

      <div class="auth-buttons">
        <!-- Show Login/Signup buttons only when not authenticated -->
        <template v-if="showAuthButtons">
          <router-link :to="{ name: 'login' }" class="btn-primary" @click="closeSidebarOnMobile">{{ $t('nav.login') }}</router-link>
          <router-link :to="{ name: 'signup' }" class="btn-primary" @click="closeSidebarOnMobile">{{ $t('nav.signup') }}</router-link>
        </template>

        <!-- Show Logout button only when authenticated -->
        <button v-if="showLogoutButton" @click="logout" class="btn-primary btn-logout">
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

    <!-- Backdrop to close sidebar on click -->
    <div v-if="isMobile && isSidebarOpen" class="sidebar-backdrop" @click="isSidebarOpen = false"></div>
  </header>
</template>
//TODO: Convert logo to webp to optimize load

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

// Use storeToRefs to maintain store reactivity
const { token } = storeToRefs(authStore);

// Sidebar state
const isSidebarOpen = ref(false);
const isMobile = ref(window.innerWidth <= 768);

// Check if user is authenticated
const isAuthenticated = computed(() => !!token.value);

// Check if we are on an authentication route (login, signup, verify-email)
const isAuthRoute = computed(() => {
  const authRoutes = ['login', 'signup', 'verify-email'];
  return authRoutes.includes(route.name as string);
});

// Show login/signup buttons only when NOT authenticated and NOT on auth route
const showAuthButtons = computed(() => !isAuthenticated.value && !isAuthRoute.value);

// Show logout button only when authenticated
const showLogoutButton = computed(() => isAuthenticated.value);

// Close sidebar automatically on mobile when route changes
watch(() => route.path, () => {
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
});

// Handle logout
const logout = async () => {
  await authStore.logout();
  router.push({ name: 'home' });
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
};

// Close sidebar on link click on mobile
const closeSidebarOnMobile = () => {
  if (isMobile.value) {
    isSidebarOpen.value = false;
  }
};

// Detect window size changes
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768;
  // Close sidebar if switching to desktop
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

