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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import BicycleList from '@/components/bicycles-dashboard/BicycleList.vue'
import BicycleMapComponent from '@/components/bicycles-dashboard/BicycleMapComponent.vue'
import { useBikeStore } from '@/stores/bikeStore'
import type Bike from '@/models/Bike'

const bikeStore = useBikeStore()

const bikes = ref<Bike[]>([])
const bikesList = computed(() => bikes.value)
const bicycleFactory = computed(() => bikeStore.factory)

onMounted(async () => {
    console.log('🚲 Cargando bicicletas desde backend...')

    // Cargar bicicletas iniciales
    await bikeStore.fetchBikes()
    bikes.value = bikeStore.allBikes

    console.log('✅ Bicicletas cargadas:', bikes.value.length)

    // Conectar WebSocket para actualizaciones en tiempo real
    bikeStore.connectWebSocket()
})

onUnmounted(() => {
    console.log('🔌 Desconectando WebSocket de bicicletas')
    bikeStore.disconnectWebSocket()
})

// Observar cambios en el store
import { watch } from 'vue'
watch(() => bikeStore.allBikes, (newBikes) => {
    bikes.value = newBikes
    console.log('🔄 Bicicletas actualizadas:', newBikes.length)
}, { deep: true })
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
