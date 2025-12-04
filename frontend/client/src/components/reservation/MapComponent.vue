<template>
  <div class="map-wrapper">
    <div id="map" :class="['map-container', { 'map-disabled': showAuthModal }]"></div>

    <!-- Modal: requiere iniciar sesión para acceder al mapa -->
    <div v-if="showAuthModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-content">
          <div class="success-icon">🔒</div>
          <h3>Acceso restringido</h3>
          <p>Para acceder a nuestros servicios y ver el mapa, debes iniciar sesión primero.</p>
          <div style="display:flex;gap:12px;justify-content:center;margin-top:18px;">
            <button class="butn-primary" @click="goToLogin">Aceptar</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Overlay: solo muestra info cuando hay ruta seleccionada (origen + destino) -->
    <div v-if="originPlain && destinationPlain" class="map-overlay">
      <div class="overlay-section">
        <h4>📍 Estación Origen</h4>
        <div>
          <div class="row"><strong>{{ originPlain.name ?? '—' }}</strong></div>
          <div class="row">🚲 {{ originPlain.free_spots ?? '—' }} bicicletas</div>
        </div>
      </div>

      <div class="overlay-section">
        <h4>🅿️ Estación Destino</h4>
        <div>
          <div class="row"><strong>{{ destinationPlain.name ?? '—' }}</strong></div>
          <div class="row">📍 {{ destinationPlain.free_spots ?? '—' }} puestos libres</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import userAuth from '@/stores/auth'
import { storeToRefs } from 'pinia'
import L, { Map as LeafletMap, Marker, Polyline } from 'leaflet'
import { onMounted, onUnmounted, ref } from 'vue'
import L, { type Map as LeafletMap } from 'leaflet'
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

// Datos locales de prueba para estaciones (cuando WebSocket no está disponible)
const mockStations = [
  { idStation: 10, nameStation: "Estación Centro", latitude: 4.1430, longitude: -73.6290, availableSlots: 5, totalSlots: 10, type: 'bike', mechanical: 3, electric: 2, slots: [] as any[] },
  { idStation: 11, nameStation: "Parque Sikuani", latitude: 4.1475, longitude: -73.6260, availableSlots: 2, totalSlots: 10, type: 'bike', mechanical: 1, electric: 1, slots: [] as any[] },
  { idStation: 12, nameStation: "Zona Universitaria", latitude: 4.1505, longitude: -73.6235, availableSlots: 0, totalSlots: 10, type: 'bike', mechanical: 0, electric: 0, slots: [] as any[] },
  { idStation: 1, nameStation: "Metro Estación Central", latitude: 4.1425, longitude: -73.6312, availableSlots: 5, totalSlots: 10, type: 'metro', mechanical: 0, electric: 0, slots: [] as any[] },
  { idStation: 2, nameStation: "Metro Sikuani", latitude: 4.1480, longitude: -73.6270, availableSlots: 2, totalSlots: 10, type: 'metro', mechanical: 0, electric: 0, slots: [] as any[] }
]

const props = defineProps<{
  origin?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
  destination?: { latitude: number; longitude: number; name?: string; free_spots?: number; status?: string } | null
}>()

// No longer emit station-click from the map; clicks only show info in the overlay

const map = ref<LeafletMap | null>(null)
const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)
const stationsRendered = ref<boolean>(false) // flag para evitar re-renderizar
const clickedStation = ref<any | null>(null)

// Auth modal: si no está autenticado, mostrar modal que obliga a iniciar sesión
const router = useRouter()
const authStore = userAuth()
const { token } = storeToRefs(authStore)
// Temporalmente deshabilitamos la restricción de login para facilitar
// la comprobación de la obtención/renderizado de estaciones.
// Antes: showAuthModal dependía de `token`; ahora forzamos `false`.
const showAuthModal = ref<boolean>(false)

// Comentado temporalmente: evitar que cambios en el token cierren/abran el modal
// watch(token, (val) => {
//   showAuthModal.value = !val
// })

function goToLogin() {
  router.push({ name: 'login' }).catch(() => void 0)
}

// Nota: no existe función de "cancelar" para evitar que el usuario interactúe sin iniciar sesión.

// Para marcadores de ruta (origen/destino)
let originMarker: Marker | null = null
let destMarker: Marker | null = null
let routeLine: Polyline | null = null

const originPlain = computed(() => (props.origin ? JSON.parse(JSON.stringify(props.origin)) : null))
const destinationPlain = computed(() => (props.destination ? JSON.parse(JSON.stringify(props.destination)) : null))

// NOTE: always show all stations; selection will be done via dropdowns

function renderStationMarkers() {
  if (!map.value || stationsRendered.value) return
  stationFactory.getAllMarkers().forEach(m => {
    m.render(map.value as LeafletMap)
    // show station info in the overlay when marker is clicked (does not select origin/destination)
    try {
      m.onClick((st) => {
        const payload = {
          id: (st as any).idStation ?? (st as any).id,
          name: (st as any).nameStation ?? (st as any).name,
          latitude: st.latitude,
          longitude: st.longitude,
          free_spots: (st as any).availableSlots ?? (st as any).free_spots,
          type: (st as any).type ?? 'bike'
        }
        // toggle: click same station again to clear
        if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
        else clickedStation.value = payload
      })
    } catch (e) {
      void e
    }
  })
  stationsRendered.value = true
}

function hideStationMarkers() {
  // Remover todos los marcadores de estaciones del mapa
  stationFactory.getAllMarkers().forEach(m => m.remove())
}

function addOriginMarker(o: { latitude: number; longitude: number; name?: string }) {
  if (!map.value) return
  if (originMarker) {
    originMarker.remove()
    originMarker = null
  }
  console.log('🔵 Agregando marcador ORIGEN en:', { lat: o.latitude, lng: o.longitude, name: o.name })
  console.log('🔵 Verificar estaciones mock:', mockStations.map(s => ({ name: s.nameStation, lat: s.latitude, lng: s.longitude })))
  
  // Usar icono estándar de Leaflet con color verde para origen
  const greenIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })
  
  originMarker = L.marker([o.latitude, o.longitude], { icon: greenIcon }).addTo(map.value as any)
  if (o.name) originMarker.bindPopup(`<strong>Origen</strong><br/>${o.name}<br/>Lat: ${o.latitude}, Lng: ${o.longitude}`)
  setTimeout(() => originMarker?.openPopup(), 200)
}

function addDestMarker(d: { latitude: number; longitude: number; name?: string }) {
  if (!map.value) return
  if (destMarker) {
    destMarker.remove()
    destMarker = null
  }
  console.log('🔵 Agregando marcador DESTINO en:', { lat: d.latitude, lng: d.longitude, name: d.name })
  
  // Usar icono estándar de Leaflet con color rojo para destino
  const redIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })
  
  destMarker = L.marker([d.latitude, d.longitude], { icon: redIcon }).addTo(map.value as any)
  if (d.name) destMarker.bindPopup(`<strong>Destino</strong><br/>${d.name}<br/>Lat: ${d.latitude}, Lng: ${d.longitude}`)
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
  ).addTo(map.value as any)
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

  // Cargar datos locales de prueba inmediatamente (fallback)
  mockStations.forEach(st => {
    const marker = stationFactory.getStationMarker(st as any)
    marker.render(map.value as LeafletMap)
    try {
      marker.onClick((station) => {
        const payload = {
          id: (station as any).idStation ?? (station as any).id,
          name: (station as any).nameStation ?? (station as any).name,
          latitude: station.latitude,
          longitude: station.longitude,
          free_spots: (station as any).availableSlots ?? (station as any).free_spots,
          type: (station as any).type ?? 'bike'
        }
        if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
        else clickedStation.value = payload
      })
    } catch (e) { void e }
  })
  stationsRendered.value = true

  // Intentar conectar al WebSocket de estaciones y solicitar carga inicial.
  try {
    stationWsService.connect((stations, factory) => {
      // Bulk inicial recibido: renderizar todas las estaciones en el mapa
      try {
        stations.forEach(st => {
          const mk = factory.getStationMarker(st)
          mk.render(map.value as LeafletMap)
          mk.onClick((station) => {
            const payload = {
              id: (station as any).idStation ?? (station as any).id,
              name: (station as any).nameStation ?? (station as any).name,
              latitude: station.latitude,
              longitude: station.longitude,
              free_spots: (station as any).availableSlots ?? (station as any).free_spots,
              type: (station as any).type ?? 'bike'
            }
            if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
            else clickedStation.value = payload
          })
        })
        stationsRendered.value = true
      } catch (e) { console.error('[Map] Error rendering bulk stations', e) }
    }, (station, factory) => {
      // Update incremental: render/update single station
      try {
        const mk = factory.getStationMarker(station)
        mk.render(map.value as LeafletMap)
        mk.onClick((st) => {
          const payload = {
            id: (st as any).idStation ?? (st as any).id,
            name: (st as any).nameStation ?? (st as any).name,
            latitude: st.latitude,
            longitude: st.longitude,
            free_spots: (st as any).availableSlots ?? (st as any).free_spots,
            type: (st as any).type ?? 'bike'
          }
          if (clickedStation.value && clickedStation.value.id === payload.id) clickedStation.value = null
          else clickedStation.value = payload
        })
      } catch (e) { console.error('[Map] Error updating station', e) }
    })
  } catch (e) { console.warn('[Map] No se pudo conectar al Stations WS', e) }

  // Debug temporal: comprobar estado del WS y cantidad de estaciones almacenadas
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] Stations WS has initial bulk?', stationWsService.getHasInitialBulk());
      console.log('[Map DEBUG] Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] Stations cache count:', stationWsService.getStationCount());
      console.log('[Map DEBUG] Stations from service:', stationWsService.getStations());
    } catch (err) {
      console.warn('[Map DEBUG] Error leyendo estado Stations WS', err);
    }
  }, 2000);

  // Re-check más tarde por si la conexión tarda en establecerse
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] (later) Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] (later) Stations WS has initial bulk?', stationWsService.getHasInitialBulk());
      console.log('[Map DEBUG] (later) Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] (later) Stations cache count:', stationWsService.getStationCount());
    } catch (err) { void err }
  }, 6000);
  // WebSocket de estaciones (fallback, no esencial)
  // stationWsService.connect(...) - comentado para evitar ruido

  // WebSocket de bicicletas (opcional)
  // wsService.connect(...) - comentado para evitar ruido
})

onUnmounted(() => {
  // Desconectar WebSocket si se conectó
  try { wsService.disconnect() } catch (e) { void e }
  try { stationWsService.disconnect() } catch (e) { void e }
  
  bicycleFactory.clear()
  stationFactory.clear()

  if (originMarker) { originMarker.remove(); originMarker = null }
  if (destMarker) { destMarker.remove(); destMarker = null }
  if (routeLine) { routeLine.remove(); routeLine = null }

  clickedStation.value = null

  if (map.value) {
    map.value.remove()
    map.value = null
  }
})

// Watcher para cambios en hasRouteSelected (mostrar/ocultar estaciones)
// No automatic hiding of stations when a route is selected. Selection
// of origin/destination is handled via dropdowns per product requirement.

// Watchers para origen/destino (dibujar línea y ajustar vista)
watch(() => props.origin, () => {
  if (!map.value) return
  if (props.origin) {
    addOriginMarker(props.origin as any)
  } else if (originMarker) { 
    originMarker.remove()
    originMarker = null
  }
  drawLine()
  fitMap()
})

watch(() => props.destination, () => {
  if (!map.value) return
  if (props.destination) {
    addDestMarker(props.destination as any)
  } else if (destMarker) { 
    destMarker.remove()
    destMarker = null
  }
  drawLine()
  fitMap()
})

// Ensure line and view update whenever either endpoint changes together
watch(() => [props.origin, props.destination], () => {
  if (!map.value) return
  drawLine()
  fitMap()
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.map-disabled {
  opacity: 0.6;
  pointer-events: none;
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

/* Modal local: aseguramos que quede por encima del overlay de estación */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000; /* superior al z-index del overlay de estación */
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 22px;
  max-width: 480px;
  width: 92%;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0,0,0,0.18);
}

.modal-content h3 { margin: 0 0 8px 0; }

.success-icon { font-size: 36px; margin-bottom: 8px; }
</style>