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
import type { Station } from '@/models/Station'
import { useStationStore } from '@/stores/stationStore'
import { StationWebSocketService, type AdminStationUpdate } from '@/services/StationWebSocketService'

const stationStore = useStationStore()
const wsService = new StationWebSocketService()

const stations = ref<Station[]>([])
const stationsList = computed(() => stations.value)

const selectedStationId = ref<number | null>(null)
const selectedStation = computed(() => stations.value.find(s => s.idStation === selectedStationId.value) || null)

onMounted(async () => {
    console.log('🚀 Cargando estaciones desde backend...')
    const loaded = await stationStore.fetchStations()
    stations.value = Array.isArray(loaded) ? loaded : []
    wsService.registerStations(stations.value)

    wsService.connect((stationId: number, adminData: AdminStationUpdate) => {
        const st = stations.value.find(s => s.idStation === stationId)
        if (st) {
            st.availableElectricBikes = adminData.availableElectricBikes
            st.availableMechanicBikes = adminData.availableMechanicBikes
            st.cctvStatus = adminData.cctvStatus
            st.panicButtonStatus = adminData.panicButtonStatus
            st.lightingStatus = adminData.lightingStatus
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
