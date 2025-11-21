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
                <div v-else class="loading-map">Cargando mapa...</div>
            </div>
        </div>
    </div>
    
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import StationInfo from '@/components/dashboard/StationInfo.vue'
import BikeInfo from '@/components/dashboard/BikeInfo.vue'
import MapComponent from '@/components/dashboard/MapComponent.vue'
import {
    Station,
    createBike,
    createStation,
    bikeFlyweightFactory,
    stationFlyweightFactory
} from '@/patterns/flyweight'

// Datos mock usando el patrón Flyweight
// Use an untyped array for internal storage to avoid structural mismatch with private members,
// and expose a typed computed for components that expect Station[]
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const stations = ref<any[]>([])
const stationsList = computed<Station[]>(() => stations.value as Station[])

// Inicializar datos con el patrón Flyweight
onMounted(() => {
    console.log('🚀 Inicializando estaciones con patrón Flyweight...')

    // Estación Centro - Villavicencio
    const station1 = createStation(
        'ST-001',
        'Estación Centro',
        'Cra 9 con Calle 37',
        4.1519,
        -73.6365,
        'metro',
        true,
        true,
        true,
        [
            createBike('B-100', 'Optimal', 'UrbanX', 'electrica', 87, 4.1519, -73.6365, true),
            // B-101 está en viaje a Parque Sikuani - no está aquí, está reservado en ST-002
            createBike('B-102', 'Needs maintenance', 'EcoRide', 'electrica', 55, 4.1518, -73.6364, true),
            createBike('B-103', 'Optimal', 'EcoRide', 'mecanica', undefined, 4.1521, -73.6367, true)
        ]
    )

    // Parque Sikuani
    const station2 = createStation(
        'ST-002',
        'Parque Sikuani',
        'Calle 15 con Cra 23',
        4.1460,
        -73.6500,
        'residencial',
        true,
        false,
        true,
        [
            createBike('B-200', 'Optimal', 'UrbanX', 'electrica', 63, 4.1460, -73.6500, true),
            createBike('B-201', 'Optimal', 'UrbanLite', 'mecanica', undefined, 4.1461, -73.6501, true)
            // B-202 está en viaje a Zona Universitaria - no está aquí, está reservado en ST-003
        ]
    )

    // Zona Universitaria
    const station3 = createStation(
        'ST-003',
        'Zona Universitaria',
        'Barrio Barzal',
        4.1630,
        -73.6220,
        'centro financiero',
        true,
        true,
        true,
        [
            createBike('B-300', 'Optimal', 'UrbanLite', 'mecanica', undefined, 4.1630, -73.6220, true)
            // B-301 está en viaje a Terminal - no está aquí, está reservado en ST-004
        ]
    )

    // Estación Sur - Terminal
    const station4 = createStation(
        'ST-004',
        'Terminal de Transportes',
        'Av 40 con Calle 42',
        4.1390,
        -73.6550,
        'metro',
        false,
        false,
        true,
        [
            createBike('B-400', 'Needs maintenance', 'EcoRide', 'electrica', 25, 4.1390, -73.6550, true),
            createBike('B-401', 'Optimal', 'UrbanLite', 'mecanica', undefined, 4.1391, -73.6551, true)
        ]
    )

    stations.value = [station1, station2, station3, station4]

    // Reservar slots para bicicletas en tránsito
    // B-101 va de Centro (ST-001) a Parque Sikuani (ST-002)
    const bikeB101 = createBike('B-101', 'Optimal', 'UrbanLite', 'mecanica', undefined, 4.1490, -73.6430, false)
    station2.reserveSlot(bikeB101, '15 minutos')
    
    // B-202 va de Parque Sikuani (ST-002) a Zona Universitaria (ST-003)
    const bikeB202 = createBike('B-202', 'Optimal', 'EcoRide', 'electrica', 95, 4.1545, -73.6360, false)
    station3.reserveSlot(bikeB202, '20 minutos')
    
    // B-301 va de Zona Universitaria (ST-003) a Terminal (ST-004)
    const bikeB301 = createBike('B-301', 'Optimal', 'UrbanX', 'electrica', 72, 4.1510, -73.6385, false)
    station4.reserveSlot(bikeB301, '10 minutos')

    // Mostrar estadísticas de optimización
    console.log('📊 Estadísticas de Flyweight:')
    console.log(`   🚲 Flyweights de bicicletas creados: ${bikeFlyweightFactory.getFlyweightCount()}`)
    console.log(`   🏢 Flyweights de estaciones creados: ${stationFlyweightFactory.getFlyweightCount()}`)
    bikeFlyweightFactory.listFlyweights()
})

const selectedStationId = ref<string | null>(null)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const selectedStation = computed(() => stations.value.find((s: any) => s.id === selectedStationId.value) || null)

function handleShowBikes(stationId: string) {
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