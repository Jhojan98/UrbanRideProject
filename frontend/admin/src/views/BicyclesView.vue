<template>
    <div class="bicycles-layout page-content">
        <h2 class="bicycles-title">Dashboard de Bicicletas</h2>
        <div class="bicycles-grid">
            <div class="grid-left">
                <BicycleList :bikes="bikesList" />
            </div>
            <div class="grid-right">
                <BicycleMapComponent
                    v-if="bikesList.length > 0"
                    :bikes="bikesList"
                    :factory="bicycleFactory"
                />
                <div v-else class="loading-map">Cargando mapa de bicicletas...</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import BicycleList from '@/components/bicycles-dashboard/BicycleList.vue'
import BicycleMapComponent from '@/components/bicycles-dashboard/BicycleMapComponent.vue'
import { useBikeStore } from '@/stores/bikeStore'

const bikeStore = useBikeStore()

// Usar directamente los getters del store (son reactivos)
const bikesList = computed(() => bikeStore.bikes)
const bicycleFactory = computed(() => bikeStore.factory)

onMounted(async () => {
    console.log('🚲 Cargando bicicletas desde backend...')

    // Cargar bicicletas iniciales
    await bikeStore.fetchBikes()
    console.log('✅ Bicicletas cargadas:', bikeStore.bikes.length)
    console.log('📊 Datos de bicicletas:', bikeStore.bikes)

    // Conectar WebSocket para actualizaciones en tiempo real
    bikeStore.connectWebSocket()
})

onUnmounted(() => {
    console.log('🔌 Desconectando WebSocket de bicicletas')
    bikeStore.disconnectWebSocket()
})
</script>

<style lang="scss" scoped>
.bicycles-title {
    margin: 0 0 1rem;
}

.bicycles-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: start;
}

.grid-left {
    display: flex;
    flex-direction: column;
}

.grid-right {
    min-height: 100%;
}

.loading-map {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 500px;
    background: var(--color-surface);
    border: 1px solid var(--color-border-light);
    border-radius: 8px;
    color: var(--color-text-secondary-light);
}

@media (max-width: 1100px) {
    .bicycles-grid {
        grid-template-columns: 1fr;
    }

    .grid-right {
        order: -1;
        margin-bottom: 2rem;
    }
}
</style>
