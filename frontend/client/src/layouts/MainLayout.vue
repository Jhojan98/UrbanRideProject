<template>
    <div class="main-layout">
        <HeaderComponent />

        <main class="layout-content">
            <slot />
        </main>

        <FooterComponent />
    </div>
</template>

<script setup lang="ts">
import HeaderComponent from '@/components/HeaderComponent.vue'
import FooterComponent from '@/components/FooterComponent.vue'
</script>

<style lang="scss" scoped>
@import "@/styles/global.scss";

.main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
}

.layout-content {
    flex: 1;
    /* Dejar espacio superior equivalente al header fijo (60px) */
    padding: 1rem;
    padding-top: 60px;
}

/* Ajustes para mobile */
@media (max-width: 768px) {
    .layout-content {
        padding-top: 60px;
        padding: 0.75rem;
        padding-top: 75px;
    }
}

@media (max-width: 480px) {
    .layout-content {
        padding-top: 56px;
        padding: 0.5rem;
        padding-top: 65px;
    }
}
</style>
