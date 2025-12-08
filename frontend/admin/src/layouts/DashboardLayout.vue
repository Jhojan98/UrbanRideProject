<template>
    <div class="dashboard-layout page-content">
        <h2 class="dashboard-title">{{$t('dashboard.stations.title')}}</h2>
        <div class="dashboard-grid">
            <div class="grid-left">
                <StationInfo
                    :stations="stationsList"
                    @show-bikes="handleShowBikes"
                />
                <BikeInfo
                    v-if="selectedStation"
                    :station="selectedStation"
                    class="bike-info-wrapper"
                />
            </div>
            <div class="grid-right">
                <MapComponent v-if="stationsList.length > 0" :stations="stationsList" />
                <div v-else class="loading-map">{{ $t('dashboard.stations.loadingMap') }}</div>
            </div>
        </div>
    </div>

</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import StationInfo from '@/components/stations-dashboard/StationInfo.vue'
import BikeInfo from '@/components/stations-dashboard/BikeInfo.vue'
import MapComponent from '@/components/stations-dashboard/MapComponent.vue'
import { useStationStore } from '@/stores/stationStore'
import { StationWebSocketService, type AdminStationUpdate } from '@/services/StationWebSocketService'

const stationStore = useStationStore()
const wsService = new StationWebSocketService()

// Usar directamente el store para mantener reactividad
const stationsList = computed(() => stationStore.stations)

const selectedStationId = ref<number | null>(null)
const selectedStation = computed(() => stationStore.stations.find(s => s.idStation === selectedStationId.value) || null)

onMounted(async () => {
    console.log('🚀 Cargando estaciones desde backend...')
    await stationStore.fetchStations()
    console.log('✅ Estaciones cargadas:', stationStore.stations.length)
    wsService.registerStations(stationStore.stations)

    wsService.connect((stationId: number, adminData: AdminStationUpdate) => {
        // Actualizar directamente en el store para mantener reactividad
        const stationIndex = stationStore.stations.findIndex(s => s.idStation === stationId)
        if (stationIndex !== -1) {
            const station = stationStore.stations[stationIndex]
            station.availableElectricBikes = adminData.availableElectricBikes
            station.availableMechanicBikes = adminData.availableMechanicBikes
            station.cctvStatus = adminData.cctvStatus
            station.panicButtonStatus = adminData.panicButtonStatus
            station.lightingStatus = adminData.lightingStatus

            console.log(`[Dashboard] 🔄 Estación ${stationId} actualizada vía WebSocket:`, adminData)
        } else {
            console.warn(`[Dashboard] ⚠️ Estación ${stationId} no encontrada`)
        }
    })
})

onUnmounted(() => {
    wsService.disconnect()
})

function handleShowBikes(stationId: number) {
    selectedStationId.value = stationId
}
</script>
<style lang="scss" scoped>
.dashboard-title { margin: 0 0 1rem; }
.bike-info-wrapper { margin-top: 1rem; }
.dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: start;
}
.grid-left { display: flex; flex-direction: column; }
.grid-right { min-height: 100%; }
@media (max-width: 1100px) {
    .dashboard-grid { grid-template-columns: 1fr; }
    .grid-right { order: -1; margin-bottom: 2rem; }
}
</style>
