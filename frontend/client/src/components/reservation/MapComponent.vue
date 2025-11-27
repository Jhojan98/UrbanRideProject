<template>
  <div class="map-wrapper">
    <div id="map" class="map-container"></div>

    <!-- Overlay informativa -->
    <div class="map-overlay">
      <div class="overlay-section">
        <h4>Origen</h4>
        <div v-if="propsOrigin">
          <div class="row"><strong>Nombre:</strong> {{ propsOrigin.name ?? '—' }}</div>
          <div class="row"><strong>Bicis:</strong> {{ propsOrigin.free_spots ?? '—' }}</div>
          <div class="row"><strong>Estado:</strong> {{ propsOrigin.status ?? '—' }}</div>
          <div class="row"><strong>Coords:</strong> {{ propsOrigin.lat }}, {{ propsOrigin.lng }}</div>
          <pre class="json">{{ propsOriginJson }}</pre>
        </div>
        <div v-else class="muted">No seleccionado</div>
      </div>

      <div class="overlay-section">
        <h4>Destino</h4>
        <div v-if="propsDestination">
          <div class="row"><strong>Nombre:</strong> {{ propsDestination.name ?? '—' }}</div>
          <div class="row"><strong>Puestos libres:</strong> {{ propsDestination.free_spots ?? '—' }}</div>
          <div class="row"><strong>Estado:</strong> {{ propsDestination.status ?? '—' }}</div>
          <div class="row"><strong>Coords:</strong> {{ propsDestination.lat }}, {{ propsDestination.lng }}</div>
          <pre class="json">{{ propsDestinationJson }}</pre>
        </div>
        <div v-else class="muted">No seleccionado</div>
      </div>
    </div>
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
import { StationFactory } from '@/patterns/StationFlyweight'
import { StationWebSocketService } from '@/services/StationWebSocketService'

// Arreglar rutas de íconos por defecto de Leaflet en bundlers
// Ajuste para íconos: en proyectos con vue-cli los assets de leaflet se sirven desde /img
// Si los íconos no aparecen, se puede configurar via CSS o copiar los archivos.
// De momento mantenemos configuración por defecto.

const map = ref<LeafletMap | null>(null)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)

function renderStationMarkers() {
    if (!map.value) return
    stationFactory.getAllMarkers().forEach(m => m.render(map.value as LeafletMap))
}

onMounted(() => {
  map.value = L.map("map", {
    center: [4.1514, -73.6370],
    zoom: 13,
    preferCanvas: true
  });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution:
            '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map.value as LeafletMap)

    // Conectar estaciones via WebSocket (bulk + updates)
    stationWsService.connect(
        (stations) => {
            // Bulk inicial recibido
            console.log(`🏁 Bulk estaciones recibido: ${stations.length}`)
            renderStationMarkers()
        },
        (station) => {
            // Actualización incremental de una estación
            console.log(`♻️ Estación actualizada: ${station.idStation}`)
            renderStationMarkers()
        }
    )

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
    stationWsService.disconnect()
    
    // Limpiar marcadores de bicicletas
    bicycleFactory.clear()
    // Limpiar marcadores de estaciones
    stationFactory.clear()

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
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  min-height: calc(100vh - 140px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

/* overlay informativa en el mapa */
.map-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255,255,255,0.98);
  border-radius: 8px;
  padding: 10px;
  width: 260px;
  font-size: 13px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.12);
  z-index: 1000;
}

.map-overlay h4 { margin: 0 0 6px 0; font-size: 13px; }
.overlay-section { margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
.overlay-section:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.row { margin: 3px 0; }
.muted { color: #666; font-style: italic; font-size: 12px; }
.json { background: #f7f7f7; padding:6px; border-radius:6px; max-height:120px; overflow:auto; font-size:12px; }
</style>