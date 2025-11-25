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
.main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
}

.layout-content {
    flex: 1;
    /* Dejar espacio superior equivalente al header fijo (header tiene padding y 40px de logo).
       Evita que el contenido quede debajo del header cuando éste está position: fixed. */
    padding: 1rem;
    padding-top: 80px; /* ajuste seguro; reduce si tu header es más pequeño */
}
</style>