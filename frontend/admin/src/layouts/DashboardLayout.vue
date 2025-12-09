<template>
    <div class="dashboard-layout page-content">
        <h2 class="dashboard-title">{{ $t('dashboard.stations.title') }}</h2>

        <!-- NUEVO: Filtro por Ciudad -->
        <StationFilter
            :total-count="allStations.length"
            :filtered-count="filteredStations.length"
            @filter-change="handleCityFilter"
        />

        <div class="dashboard-grid">
            <div class="grid-left">
                <StationInfo
                    :stations="filteredStations"
                    @show-bikes="handleShowBikes"
                />
                <BikeInfo
                    v-if="selectedStation"
                    :station="selectedStation"
                    class="bike-info-wrapper"
                />
            </div>
            <div class="grid-right">
                <MapComponent v-if="filteredStations.length > 0" :stations="filteredStations" />
                <div v-else-if="allStations.length === 0" class="loading-map">
                    {{ $t('dashboard.stations.loadingMap') }}
                </div>
                <div v-else class="empty-filter">
                    <span class="material-symbols-outlined">location_off</span>
                    <p>{{ $t('dashboard.stations.filter.noResults') }}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import StationInfo from '@/components/stations-dashboard/StationInfo.vue'
import BikeInfo from '@/components/stations-dashboard/BikeInfo.vue'
import MapComponent from '@/components/stations-dashboard/MapComponent.vue'
import StationFilter from '@/components/stations-dashboard/StationFilter.vue'
import { useStationStore } from '@/stores/stationStore'
import { useCityStore } from '@/stores/cityStore'
import { StationWebSocketService, type AdminStationUpdate } from '@/services/StationWebSocketService'

const stationStore = useStationStore()
const cityStore = useCityStore()
const wsService = new StationWebSocketService()

// Estado del filtro
const selectedCityId = ref<number | null>(null)

// Todas las estaciones (sin filtro)
const allStations = computed(() => stationStore.stations)

// Estaciones filtradas por ciudad
const filteredStations = computed(() => {
    if (selectedCityId.value === null) {
        return allStations.value
    }
    return allStations.value.filter(station => {
        const cityId = station.idCity || (station as any).k_id_city
        return cityId === selectedCityId.value
    })
})

const selectedStationId = ref<number | null>(null)

const selectedStation = computed(() => {
    return filteredStations.value.find(s => s.idStation === selectedStationId.value) || null
})

// Handler del filtro
function handleCityFilter(cityId: number | null) {
    selectedCityId.value = cityId
    // Limpiar selección de estación si no está en el filtro
    if (selectedStationId.value && !filteredStations.value.find(s => s.idStation === selectedStationId.value)) {
        selectedStationId.value = null
    }
}

onMounted(async () => {
    console.log('🚀 Cargando datos del dashboard...')

    // Cargar ciudades y estaciones en paralelo
    await Promise.all([
        cityStore.fetchCities(),
        stationStore.fetchStations()
    ])

    console.log('✅ Ciudades cargadas:', cityStore.cities.length)
    console.log('✅ Estaciones cargadas:', stationStore.stations.length)

    wsService.registerStations(stationStore.stations)
    wsService.connect((stationId: number, adminData: AdminStationUpdate) => {
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
.dashboard-title {
    margin: 0 0 1rem;
}

.bike-info-wrapper {
    margin-top: 1rem;
}

.dashboard-grid {
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

.loading-map,
.empty-filter {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 2rem;
    background: var(--color-background-light);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    min-height: 400px;

    .material-symbols-outlined {
        font-size: 4rem;
        color: var(--color-gray-medium);
        margin-bottom: 1rem;
    }

    p {
        font-size: 1.1rem;
        color: var(--color-text-secondary-light);
        margin: 0;
    }
}

html[data-theme="dark"] {
    .loading-map,
    .empty-filter {
        background: var(--color-surface-dark);

        p {
            color: var(--color-text-secondary-dark);
        }
    }
}

@media (max-width: 1100px) {
    .dashboard-grid {
        grid-template-columns: 1fr;
    }

    .grid-right {
        order: -1;
        margin-bottom: 2rem;
    }
}
</style>
