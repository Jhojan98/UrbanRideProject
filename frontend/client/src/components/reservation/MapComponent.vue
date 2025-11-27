<template>
  <div class="map-wrapper">
    <div id="map" class="map-container"></div>

    <!-- Overlay con datos recibidos -->
    <div class="map-overlay">
      <div class="overlay-section">
        <h4>Origen</h4>
        <div v-if="originPlain">
          <div class="row"><strong>Nombre:</strong> {{ originPlain.name ?? '—' }}</div>
          <div class="row"><strong>Bicis:</strong> {{ originPlain.free_spots ?? '—' }}</div>
          <div class="row"><strong>Estado:</strong> {{ originPlain.status ?? '—' }}</div>
          <div class="row"><strong>Coords:</strong> {{ originPlain.latitude }}, {{ originPlain.longitude }}</div>
        </div>
        <div v-else class="muted">No seleccionado</div>
      </div>

      <div class="overlay-section">
        <h4>Destino</h4>
        <div v-if="destinationPlain">
          <div class="row"><strong>Nombre:</strong> {{ destinationPlain.name ?? '—' }}</div>
          <div class="row"><strong>Puestos libres:</strong> {{ destinationPlain.free_spots ?? '—' }}</div>
          <div class="row"><strong>Estado:</strong> {{ destinationPlain.status ?? '—' }}</div>
          <div class="row"><strong>Coords:</strong> {{ destinationPlain.latitude }}, {{ destinationPlain.longitude }}</div>
        </div>
        <div v-else class="muted">No seleccionado</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps, watch, computed } from 'vue'
import L, { Map as LeafletMap, Marker, Polyline } from 'leaflet'
import 'leaflet/dist/leaflet.css'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerIcon from 'leaflet/dist/images/marker-icon.png'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

// Arreglar íconos por defecto
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

const props = defineProps<{
  origin?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
  destination?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
}>()

const map = ref<LeafletMap | null>(null)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)

// Para marcadores de ruta (origen/destino)
let originMarker: Marker | null = null
let destMarker: Marker | null = null
let routeLine: Polyline | null = null

const originPlain = computed(() => (props.origin ? JSON.parse(JSON.stringify(props.origin)) : null))
const destinationPlain = computed(() => (props.destination ? JSON.parse(JSON.stringify(props.destination)) : null))

function renderStationMarkers() {
  if (!map.value) return
  stationFactory.getAllMarkers().forEach(m => m.render(map.value as LeafletMap))
}

function addOriginMarker(o: { latitude: number; longitude: number; name?: string }) {
  if (!map.value) return
  if (originMarker) {
    originMarker.remove()
    originMarker = null
  }
  originMarker = L.marker([o.latitude, o.longitude]).addTo(map.value)
  if (o.name) originMarker.bindPopup(`<strong>Origen</strong><br/>${o.name}`)
  setTimeout(() => originMarker?.openPopup(), 200)
}

function addDestMarker(d: { latitude: number; longitude: number; name?: string }) {
  if (!map.value) return
  if (destMarker) {
    destMarker.remove()
    destMarker = null
  }
  destMarker = L.marker([d.latitude, d.longitude]).addTo(map.value)
  if (d.name) destMarker.bindPopup(`<strong>Destino</strong><br/>${d.name}`)
  setTimeout(() => destMarker?.openPopup(), 200)
}

function drawLine() {
  if (!map.value) return
  if (!props.origin || !props.destination) {
    if (routeLine) {
      routeLine.remove()
      routeLine = null
    }
    return
  }

  if (routeLine) {
    routeLine.remove()
    routeLine = null
  }

  routeLine = L.polyline(
    [[props.origin.latitude, props.origin.longitude], [props.destination.latitude, props.destination.longitude]],
    { color: 'blue', weight: 4, opacity: 0.85 }
  ).addTo(map.value)
}

function fitMap() {
  if (!map.value) return

  if (props.origin && props.destination) {
    const bounds = L.latLngBounds([
      [props.origin.latitude, props.origin.longitude],
      [props.destination.latitude, props.destination.longitude],
    ])
    map.value.fitBounds(bounds, { padding: [40, 40] })
    return
  }

  if (props.origin && !props.destination) {
    map.value.setView([props.origin.latitude, props.origin.longitude], 14)
    return
  }

  if (!props.origin && props.destination) {
    map.value.setView([props.destination.latitude, props.destination.longitude], 14)
    return
  }
}

onMounted(() => {
  const initialCenter: [number, number] = [4.1514, -73.6370]

  map.value = L.map('map', {
    center: initialCenter,
    zoom: 13,
    zoomControl: true,
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  }).addTo(map.value as LeafletMap)

  // Conectar estaciones via WebSocket
  stationWsService.connect(
    (stations) => {
      console.log(`🏁 Bulk estaciones recibido: ${stations.length}`)
      renderStationMarkers()
    },
    (station) => {
      console.log(`♻️ Estación actualizada: ${station.idStation}`)
      renderStationMarkers()
    }
  )

  // Conectar al WebSocket de bicicletas
  wsService.connect((factory: BicycleFactory) => {
    if (!map.value) return
    factory.getAllMarkers().forEach(marker => {
      marker.render(map.value as LeafletMap)
    })
    console.log(`🚲 Total de bicicletas en el mapa: ${factory.size()}`)
  })

  console.log('🗺️ Mapa inicializado y WebSocket conectado')
})

onUnmounted(() => {
  wsService.disconnect()
  stationWsService.disconnect()
  bicycleFactory.clear()
  stationFactory.clear()

  if (originMarker) { originMarker.remove(); originMarker = null }
  if (destMarker) { destMarker.remove(); destMarker = null }
  if (routeLine) { routeLine.remove(); routeLine = null }

  if (map.value) {
    map.value.remove()
    map.value = null
  }

  console.log('🗺️ Mapa y WebSocket desconectados')
})

// Watchers para origen/destino
watch(() => props.origin, (o) => {
  console.log('MapComponent received origin:', o)
  if (!map.value) return
  if (o) addOriginMarker(o as any)
  else if (originMarker) { originMarker.remove(); originMarker = null }
  drawLine()
  fitMap()
}, { immediate: true })

watch(() => props.destination, (d) => {
  console.log('MapComponent received destination:', d)
  if (!map.value) return
  if (d) addDestMarker(d as any)
  else if (destMarker) { destMarker.remove(); destMarker = null }
  drawLine()
  fitMap()
}, { immediate: true })
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.map-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 8px;
  padding: 10px;
  width: 260px;
  font-size: 13px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
  z-index: 1000;
}

.map-overlay h4 { margin: 0 0 6px 0; font-size: 13px; }
.overlay-section { margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
.overlay-section:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.row { margin: 3px 0; }
.muted { color: #666; font-style: italic; font-size: 12px; }

@media (max-width: 768px) {
  .map-container {
    min-height: 50vh;
  }
}
</style>