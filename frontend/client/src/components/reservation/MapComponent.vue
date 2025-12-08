<template>
  <div class="map-wrapper">
    <div id="map" :class="['map-container', { 'map-disabled': showAuthModal }]"></div>

    <!-- Modal: requires login to access the map -->
    <div v-if="showAuthModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-content">
          <div class="success-icon">🔒</div>
          <h3>{{ $t('reservation.map.authRequired') }}</h3>
          <p>{{ $t('reservation.map.authMessage') }}</p>
          <div style="display:flex;gap:12px;justify-content:center;margin-top:18px;">
            <button class="butn-primary" @click="goToLogin">{{ $t('reservation.map.accept') }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Overlay: solo muestra info cuando hay ruta seleccionada (origen + destino) -->
    <div v-if="originPlain && destinationPlain" class="map-overlay">
      <div class="overlay-section">
        <h4>📍 {{ $t('reservation.map.originStation') }}</h4>
        <div>
          <div class="row"><strong>{{ originPlain.nameStation ?? '—' }}</strong></div>
          <div class="row">🚲 {{ originPlain.availableSlots ?? '—' }} {{ $t('reservation.map.availableBikes') }}</div>
        </div>
      </div>

      <div class="overlay-section">
        <h4>🅿️ {{ $t('reservation.map.destinationStation') }}</h4>
        <div>
          <div class="row"><strong>{{ destinationPlain.nameStation ?? '—' }}</strong></div>
          <div class="row">📍 {{ destinationPlain.availableSlots ?? '—' }} {{ $t('reservation.map.availableSlots') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import L from 'leaflet'
import type { Map as LeafletMap, Marker, Polyline } from 'leaflet'
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

// Fix default icons
// Remove internal icon method with safe cast
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow
})

import { StationFactory } from '@/patterns/StationFlyweight'
import { StationWebSocketService } from '@/services/StationWebSocketService'
import { useStationStore } from '@/stores/station'
import type { Station } from '@/models/Station'

// Interfaz para props de origen/destino
interface StationPoint {
  idStation?: number
  nameStation?: string
  latitude: number
  longitude: number
  availableSlots?: number
  type?: string
}

const props = defineProps<{
  origin?: StationPoint | null
  destination?: StationPoint | null
  useSockets?: boolean
  initialStations?: Station[]
}>()

// Do not emit station-click from map; clicks only show info in overlay

const { t: $t, locale } = useI18n()
const stationStore = useStationStore()
const map = ref<LeafletMap | null>(null)
const isMounted = ref<boolean>(false)
const stationFactory = new StationFactory()
const stationWsService = new StationWebSocketService(stationFactory)
// Flag to avoid redundant station re-rendering
const stationsRendered = ref<boolean>(false)
interface ClickedStation {
  id: number | string
  name: string
  latitude: number
  longitude: number
  free_spots: number
  type: string
}
const clickedStation = ref<ClickedStation | null>(null)
const pendingTimeouts = new Set<number>()

// Auth modal: if not authenticated, show modal that requires login
const router = useRouter()
// const authStore = userAuth()
// const { token } = storeToRefs(authStore)
// Temporarily disable login restriction to facilitate
// checking station retrieval/rendering.
// Before: showAuthModal depended on `token`; now we force `false`.
const showAuthModal = ref<boolean>(false)

// Temporarily commented: prevent token changes from opening/closing modal
// watch(token, (val) => {
//   showAuthModal.value = !val
// })

function goToLogin() {
  router.push({ name: 'login' }).catch(() => void 0)
}

// Note: no "cancel" function exists to prevent user interaction without logging in.

// For route markers (origin/destination)
let originMarker: Marker | null = null
let destMarker: Marker | null = null
let routeLine: Polyline | null = null

const originPlain = computed(() => props.origin ? {
  nameStation: props.origin.nameStation ?? 'Origin',
  availableSlots: props.origin.availableSlots ?? 0
} : null)

const destinationPlain = computed(() => props.destination ? {
  nameStation: props.destination.nameStation ?? 'Destination',
  availableSlots: props.destination.availableSlots ?? 0
} : null)

// Helper: register timeout for cleanup in onUnmounted
function setMountedTimeout(callback: () => void, delay: number): number {
  const timeoutId = window.setTimeout(() => {
    if (isMounted.value) {
      callback()
    }
    pendingTimeouts.delete(timeoutId)
  }, delay)
  pendingTimeouts.add(timeoutId)
  return timeoutId
}

// NOTE: always show all stations; selection will be done via dropdowns

function addOriginMarker(o: StationPoint) {
  if (!map.value || !isMounted.value) return
  if (originMarker) {
    originMarker.remove()
    originMarker = null
  }
  console.log('🔵 Adding ORIGIN marker at:', { lat: o.latitude, lng: o.longitude, nameStation: o.nameStation })

  // Use standard Leaflet icon with green color for origin
  const greenIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })

  originMarker = L.marker([o.latitude, o.longitude], { icon: greenIcon }).addTo(map.value as LeafletMap)
  if (o.nameStation) {
    const popupText = `<strong>${$t('reservation.map.markerOrigin')}</strong><br/>${o.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: o.latitude.toFixed(4), lng: o.longitude.toFixed(4) })}`
    originMarker.bindPopup(popupText)
  }
  setMountedTimeout(() => originMarker?.openPopup(), 200)
}

function addDestMarker(d: StationPoint) {
  if (!map.value || !isMounted.value) return
  if (destMarker) {
    destMarker.remove()
    destMarker = null
  }
  console.log('🔵 Adding DESTINATION marker at:', { lat: d.latitude, lng: d.longitude, nameStation: d.nameStation })

  // Use standard Leaflet icon with red color for destination
  const redIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })

  destMarker = L.marker([d.latitude, d.longitude], { icon: redIcon }).addTo(map.value as LeafletMap)
  if (d.nameStation) {
    const popupText = `<strong>${$t('reservation.map.markerDestination')}</strong><br/>${d.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: d.latitude.toFixed(4), lng: d.longitude.toFixed(4) })}`
    destMarker.bindPopup(popupText)
  }
  setMountedTimeout(() => destMarker?.openPopup(), 200)
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

  try {
    routeLine = L.polyline(
      [[props.origin.latitude, props.origin.longitude], [props.destination.latitude, props.destination.longitude]],
      { color: 'blue', weight: 4, opacity: 0.85 }
    ).addTo(map.value as LeafletMap)
  } catch (error) {
    console.warn('[Map] Error drawing route line:', error)
    routeLine = null
  }
}

function fitMap() {
  if (!map.value || !isMounted.value) return

  try {
    if (props.origin && props.destination) {
      const bounds = L.latLngBounds([
        [props.origin.latitude, props.origin.longitude],
        [props.destination.latitude, props.destination.longitude],
      ])
      map.value.fitBounds(bounds, { padding: [40, 40], animate: false })
      return
    }

    if (props.origin && !props.destination) {
      map.value.setView([props.origin.latitude, props.origin.longitude], 14, { animate: false })
      return
    }

    if (!props.origin && props.destination) {
      map.value.setView([props.destination.latitude, props.destination.longitude], 14, { animate: false })
      return
    }
  } catch (error) {
    console.warn('[Map] Error fitting map bounds:', error)
  }
}

// Render stations from store on the map
function renderStationsFromStore() {
  const stations = stationStore.allStations

  if (!stations || stations.length === 0) {
    console.warn('[Map] No stations available in store')
    return false
  }

  console.log('[Map] Rendering stations from store (StationFactory):', stations.length)

  stations.forEach(s => {
    try {
      const marker = stationFactory.getStationMarker(s as any)
      marker.setTranslator($t)
      marker.render(map.value as LeafletMap)
    } catch (e) {
      console.warn('[Map] Error rendering station via factory:', e)
    }
  })

  stationsRendered.value = true
  console.log('[Map] stations rendered from store, count:', stations.length)
  return true
}

onMounted(() => {
  const initialCenter: [number, number] = [4.1514, -73.6370]

  map.value = L.map('map', {
    center: initialCenter,
    zoom: 13,
    zoomControl: true,
    // Disable all zoom/transition animations to avoid
    // errors when unmounting or during rapid changes.
    zoomAnimation: false,
    markerZoomAnimation: false,
    fadeAnimation: false,
    inertia: false,
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    // Reduce work during zoom/redraws
    updateWhenIdle: true,
    keepBuffer: 0,
  }).addTo(map.value as LeafletMap)

  // Fetch and render stations from store
  if (props.useSockets === false) {
    // Fetch stations from backend via store
    stationStore.fetchStations().then((stations) => {
      if (stations && stations.length > 0) {
        renderStationsFromStore()
      } else {
        console.warn('[Map] No stations loaded from store')
        if (props.initialStations && props.initialStations.length) {
          console.log('[Map] Using initialStations prop as fallback')
          props.initialStations.forEach(st => {
            const marker = stationFactory.getStationMarker(st as any)
            marker.render(map.value as LeafletMap)
          })
          stationsRendered.value = true
        }
      }
    }).catch((err) => {
      console.error('[Map] Error fetching stations from store:', err)
      if (props.initialStations && props.initialStations.length) {
        console.log('[Map] Using initialStations prop as fallback after error')
        props.initialStations.forEach(st => {
          const marker = stationFactory.getStationMarker(st as any)
          marker.render(map.value as LeafletMap)
        })
        stationsRendered.value = true
      }
    })
  } else {
    console.log('[Map] WebSocket mode enabled, waiting for WS data')
    stationsRendered.value = false
  }

  // Attempt to connect to station WebSocket for real-time bike availability updates
  // Stations are loaded from the store (renderStationsFromStore), WS only updates bike counts
  try {
    stationWsService.connect((station, factory) => {
      // Update incremental: telemetry received, update single station marker
      try {
        const mk = factory.getMarkerById(station.idStation);
        if (mk) {
          mk.update(station);
          mk.updatePopupContent();
        }
        console.log(`[Map] Station ${station.idStation} updated: ⚡ ${station.electric}, ⚙️ ${station.mechanical}`);
      } catch (e) { console.error('[Map] Error updating station from WS telemetry', e) }
    })
  } catch (e) { console.warn('[Map] Could not connect to Stations WS', e) }

  // Temporary debug: check WS state and number of stored stations
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] Stations cache count:', stationWsService.getStationCount());
      console.log('[Map DEBUG] Stations from service:', stationWsService.getStations());
    } catch (err) {
      console.warn('[Map DEBUG] Error reading Stations WS state', err);
    }
  }, 2000);

  // Re-check later in case connection takes time to establish
  setTimeout(() => {
    try {
      console.log('[Map DEBUG] (later) Stations WS connected?', stationWsService.getIsConnected());
      console.log('[Map DEBUG] (later) Station factory size:', stationWsService.getFactory().size());
      console.log('[Map DEBUG] (later) Stations cache count:', stationWsService.getStationCount());
    } catch (err) { void err }
  }, 6000);
  // Station WebSocket (fallback, not essential)
  // stationWsService.connect(...) - commented out to avoid noise

  // Mark component as mounted
  isMounted.value = true
})

onUnmounted(() => {
  // Mark component as unmounted immediately
  isMounted.value = false

  // Clean up all pending timeouts
  pendingTimeouts.forEach(timeoutId => clearTimeout(timeoutId))
  pendingTimeouts.clear()

  // Disconnect WebSocket if connected
  try { stationWsService.disconnect() } catch (e) { void e }

  stationFactory.clear()

  if (originMarker) { originMarker.remove(); originMarker = null }
  if (destMarker) { destMarker.remove(); destMarker = null }
  if (routeLine) { routeLine.remove(); routeLine = null }

  clickedStation.value = null

  if (map.value) {
    // Stop any animation in progress before removing the map
    try {
      map.value.stop()
    } catch (e) { void e }

    // Remove all event listeners
    try {
      map.value.off()
    } catch (e) { void e }

    // Finally remove the map
    try {
      map.value.remove()
    } catch (e) { void e }

    map.value = null
  }
})

// Watcher for changes in hasRouteSelected (show/hide stations)
// No automatic hiding of stations when a route is selected. Selection
// of origin/destination is handled via dropdowns per product requirement.

// Watchers for origin/destination (draw line and adjust view)
watch(() => props.origin, () => {
  if (!map.value || !isMounted.value) return
  try {
    if (props.origin) {
      addOriginMarker(props.origin)
    } else if (originMarker) {
      originMarker.remove()
      originMarker = null
    }
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error in origin watcher:', error)
  }
})

watch(() => props.destination, () => {
  if (!map.value || !isMounted.value) return
  try {
    if (props.destination) {
      addDestMarker(props.destination)
    } else if (destMarker) {
      destMarker.remove()
      destMarker = null
    }
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error in destination watcher:', error)
  }
})

// Ensure line and view update whenever either endpoint changes together
watch(() => [props.origin, props.destination], () => {
  if (!map.value || !isMounted.value) return
  try {
    drawLine()
    fitMap()
  } catch (error) {
    console.warn('[Map] Error updating line and bounds:', error)
  }
})

// Watcher for locale changes: update all popup contents when language changes
watch(() => locale.value, () => {
  if (!isMounted.value) return
  try {
    // Update origin marker popup
    if (props.origin) {
      const popupText = `<strong>${$t('reservation.map.markerOrigin')}</strong><br/>${props.origin.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: props.origin.latitude.toFixed(4), lng: props.origin.longitude.toFixed(4) })}`
      originMarker?.setPopupContent(popupText)
    }
    // Update destination marker popup
    if (props.destination) {
      const popupText = `<strong>${$t('reservation.map.markerDestination')}</strong><br/>${props.destination.nameStation}<br/>${$t('reservation.map.markerCoords', { lat: props.destination.latitude.toFixed(4), lng: props.destination.longitude.toFixed(4) })}`
      destMarker?.setPopupContent(popupText)
    }
    // Update all station popups
    stationFactory.updateAllPopups()
  } catch (error) {
    console.warn('[Map] Error updating popups on locale change:', error)
  }
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

/* Local modal: ensure it stays above the station overlay */
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
  z-index: 2000; /* higher than the station overlay z-index */
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
