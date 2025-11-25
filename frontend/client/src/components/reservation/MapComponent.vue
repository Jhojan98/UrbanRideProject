<template>
    <div class="map-wrapper">
        <div id="map" class="map-container" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import L, { Map as LeafletMap } from 'leaflet'
import 'leaflet/dist/leaflet.css'
// Fix de íconos por defecto de Leaflet para bundlers (evita 404 de marker-icon.png)
// Importamos las imágenes y configuramos el Default Icon
// Nota: Esto no afecta nuestros DivIcon personalizados para bicicletas
// pero corrige los markers de ejemplo/otros que usen el ícono por defecto
//
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon from 'leaflet/dist/images/marker-icon.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

// El prototipo cambia entre versiones; forzamos mergeOptions con rutas explícitas
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2x,
    iconUrl: markerIcon,
    shadowUrl: markerShadow
})
import { BicycleFactory } from '@/patterns/BicycleFlyweight'
import { BicycleWebSocketService } from '@/services/BicycleWebSocketService'

// Arreglar rutas de íconos por defecto de Leaflet en bundlers
// Ajuste para íconos: en proyectos con vue-cli los assets de leaflet se sirven desde /img
// Si los íconos no aparecen, se puede configurar via CSS o copiar los archivos.
// De momento mantenemos configuración por defecto.

const map = ref<LeafletMap | null>(null)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)

onMounted(() => {
    // Centro inicial: Villavicencio (Meta, Colombia)
    const initialCenter: [number, number] = [4.1514, -73.6370]

    map.value = L.map('map', {
        center: initialCenter,
        zoom: 13,
        zoomControl: true,
    })

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution:
            '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map.value as LeafletMap)

    // Marcadores de ejemplo en Villavicencio (puedes reemplazarlos por estaciones reales)
    const stations: Array<{ name: string; coords: [number, number] }> = [
        { name: 'Estación Centro', coords: [4.1519, -73.6365] },
        { name: 'Parque Sikuani', coords: [4.1460, -73.6500] },
        { name: 'Zona Universitaria', coords: [4.1630, -73.6220] },
        { name: 'Estación Sur', coords: [4.1390, -73.6550] },
    ]

    if (map.value) {
        stations.forEach((s) => {
            L.marker(s.coords).addTo(map.value as LeafletMap)
                .bindPopup(`<b>${s.name}</b>`)
        })
    }

    // Conectar al WebSocket y renderizar bicicletas cuando lleguen
    wsService.connect((factory: BicycleFactory) => {
        if (!map.value) return

        // Renderizar todos los marcadores de bicicletas en el mapa
        factory.getAllMarkers().forEach(marker => {
            marker.render(map.value as LeafletMap)
        })

        console.log(`🚲 Total de bicicletas en el mapa: ${factory.size()}`)
    })

    console.log('🗺️ Mapa inicializado y WebSocket conectado')
})

onUnmounted(() => {
    // Desconectar WebSocket
    wsService.disconnect()
    
    // Limpiar marcadores de bicicletas
    bicycleFactory.clear()

    // Destruir mapa
    if (map.value) {
        map.value.remove()
        map.value = null
    }

    console.log('🗺️ Mapa y WebSocket desconectados')
})
</script>

<style scoped>
.map-wrapper {
    width: 100%;
    height: 100%;
}

.map-container {
    width: 100%;
    /* Ocupa viewport menos header/footer aprox. */
    min-height: calc(100vh - 140px);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

@media (max-width: 768px) {
    .map-container {
        min-height: 50vh;
    }
}
</style>