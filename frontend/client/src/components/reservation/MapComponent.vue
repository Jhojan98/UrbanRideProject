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
import { ref, onMounted, onUnmounted, defineProps, watch, computed } from "vue";
import L, { Map as LeafletMap, Marker, Polyline } from "leaflet";
import "leaflet/dist/leaflet.css";

const props = defineProps<{
  origin?: { lat: number; lng: number; name?: string; free_spots?: number; status?: string } | null;
  destination?: { lat: number; lng: number; name?: string; free_spots?: number; status?: string } | null;
}>();

const map = ref<LeafletMap | null>(null);
let originMarker: Marker | null = null;
let destMarker: Marker | null = null;
let routeLine: Polyline | null = null;

const propsOrigin = computed(() => props.origin ?? null);
const propsDestination = computed(() => props.destination ?? null);

const propsOriginJson = computed(() => propsOrigin.value ? JSON.stringify(propsOrigin.value, null, 2) : "");
const propsDestinationJson = computed(() => propsDestination.value ? JSON.stringify(propsDestination.value, null, 2) : "");

onMounted(() => {
  map.value = L.map("map", {
    center: [4.1514, -73.6370],
    zoom: 13,
    preferCanvas: true
  });
  map.value = L.map("map", {
    center: [4.1514, -73.6370],
    zoom: 13,
    preferCanvas: true
  });

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
  }).addTo(map.value);
});

onUnmounted(() => {
  if (originMarker) { originMarker.remove(); originMarker = null; }
  if (destMarker) { destMarker.remove(); destMarker = null; }
  if (routeLine) { routeLine.remove(); routeLine = null; }
  map.value?.remove();
  map.value = null;
});

function clearRouteLine() {
  if (routeLine) {
    routeLine.remove();
    routeLine = null;
  }
}

function addOriginMarker(o: { lat: number; lng: number; name?: string }) {
  if (!map.value) return;
  if (originMarker) { originMarker.remove(); originMarker = null; }
  originMarker = L.marker([o.lat, o.lng]).addTo(map.value);
  if (o.name) originMarker.bindPopup(`<strong>Origen</strong><br/>${o.name}`);
  setTimeout(() => originMarker?.openPopup(), 200);
}

function addDestMarker(d: { lat: number; lng: number; name?: string }) {
  if (!map.value) return;
  if (destMarker) { destMarker.remove(); destMarker = null; }
  destMarker = L.marker([d.lat, d.lng]).addTo(map.value);
  if (d.name) destMarker.bindPopup(`<strong>Destino</strong><br/>${d.name}`);
  setTimeout(() => destMarker?.openPopup(), 200);
}

function drawLine() {
  if (!map.value) return;
  if (!props.origin || !props.destination) {
    clearRouteLine();
    return;
  }
  clearRouteLine();
  routeLine = L.polyline(
    [
      [props.origin.lat, props.origin.lng],
      [props.destination.lat, props.destination.lng],
    ],
    { color: "blue", weight: 4, opacity: 0.85 }
  ).addTo(map.value);
}

function fitMap() {
  if (!map.value) return;
  if (props.origin && props.destination) {
    const bounds = L.latLngBounds([
      [props.origin.lat, props.origin.lng],
      [props.destination.lat, props.destination.lng],
    ]);
    map.value.fitBounds(bounds, { padding: [40, 40] });
    return;
  }
  if (props.origin && !props.destination) {
    map.value.setView([props.origin.lat, props.origin.lng], 14);
    return;
  }
  if (!props.origin && props.destination) {
    map.value.setView([props.destination.lat, props.destination.lng], 14);
    return;
  }
}

// logs para depuración
watch(() => props.origin, (o) => {
  console.log("MapComponent received origin:", o);
  if (!map.value) return;
  if (o) addOriginMarker(o);
  else if (originMarker) { originMarker.remove(); originMarker = null; }
  drawLine();
  fitMap();
}, { immediate: true });

watch(() => props.destination, (d) => {
  console.log("MapComponent received destination:", d);
  if (!map.value) return;
  if (d) addDestMarker(d);
  else if (destMarker) { destMarker.remove(); destMarker = null; }
  drawLine();
  fitMap();
}, { immediate: true });
</script>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  min-height: calc(100vh - 140px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
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