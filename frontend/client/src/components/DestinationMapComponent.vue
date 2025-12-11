<template>
  <div class="destination-map">
    <div class="map-header">
      <h2>{{ $t('destination.title') }}</h2>
      <p>{{ $t('destination.subtitle') }}</p>
    </div>

    <div class="map-wrapper">
      <div id="destination-map" class="map-container"></div>
    </div>

    <div class="destination-info">
      <div class="info-card">
        <h3>{{ destinationStation.name }}</h3>
        <div class="detail-item">
          <span class="label">{{ $t('destination.address') }}</span>
          <span class="value">{{ destinationStation.address }}</span>
        </div>
        <div class="detail-item">
          <span class="label">{{ $t('destination.availableSlots') }}</span>
          <span class="value">{{ destinationStation.availableSlots }}</span>
        </div>
        <div class="detail-item">
          <span class="label">{{ $t('destination.distance') }}</span>
          <span class="value">{{ destinationStation.distance }}</span>
        </div>
        <div class="detail-item">
          <span class="label">{{ $t('destination.estimatedTime') }}</span>
          <span class="value">{{ destinationStation.estimatedTime }}</span>
        </div>
      </div>

      <button class="butn-primary" @click="confirmDestination">
        {{ $t('destination.confirm') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router'
import L, { type Map as LeafletMap } from 'leaflet'
import 'leaflet/dist/leaflet.css'

const router = useRouter()
useI18n();

// Destination station data (in a real case would come from props or store)
const destinationStation = ref({
  name: 'Centro Commercial Station',
  address: 'Carrera 40 #12-45, Villavicencio',
  availableSlots: 8,
  distance: '1.2 km',
  estimatedTime: '15 min',
  coords: [4.1420, -73.6260] as [number, number]
})

const map = ref<LeafletMap | null>(null)

onMounted(() => {
  // Use nextTick to ensure DOM is ready
  nextTick(() => {
    // Initialize map centered at destination station
    map.value = L.map('destination-map', {
      center: destinationStation.value.coords,
      zoom: 16,
      zoomControl: true,
    })

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map.value)

    // Destination station marker with special icon
    const destinationIcon = L.divIcon({
      html: '<div class="destination-marker">🏁</div>',
      className: 'destination-icon',
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    })

    L.marker(destinationStation.value.coords, { icon: destinationIcon })
      .addTo(map.value as LeafletMap)
      .bindPopup(`
        <div class="destination-popup">
          <h4>${destinationStation.value.name}</h4>
          <p>🏁 Destination Station</p>
          <p>🚲 ${destinationStation.value.availableSlots} free spaces</p>
        </div>
      `)
      .openPopup()

    // Add coverage area circle
    L.circle(destinationStation.value.coords, {
      color: '#007bff',
      fillColor: '#007bff',
      fillOpacity: 0.1,
      radius: 200 // 200 meters
    }).addTo(map.value as LeafletMap)

    // Force map resize
    setTimeout(() => {
      map.value?.invalidateSize()
    }, 100)
  })
})

onUnmounted(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})

const confirmDestination = () => {
  // Confirm destination station
  router.push('/ride/active')
}
</script>

<style lang="scss" scoped>
.destination-map {
  padding: 20px;
  min-height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.map-header {
  margin-bottom: 10px;

  h2 {
    margin: 0 0 8px 0;
    color: #333;
  }

  p {
    margin: 0;
    color: #666;
  }
}

.map-wrapper {
  flex: 1;
  position: relative;
  min-height: 400px; /* Minimum height */
}

.map-container {
  width: 100%;
  height: 100%;
  position: absolute; /* absolute position */
  top: 0;
  left: 0;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.destination-info {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.info-card {
  margin-bottom: 20px;

  h3 {
    margin: 0 0 15px 0;
    color: #333;
  }
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;

  .label {
    color: #666;
    font-weight: 500;
  }

  .value {
    color: #333;
    font-weight: 600;
  }
}

.butn-primary {
  width: 100%;
  margin-top: 10px;
}

// Estilos para el marcador
:deep(.destination-marker) {
  font-size: 20px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}

:deep(.destination-popup) {
  h4 {
    margin: 0 0 8px 0;
    color: #007bff;
  }

  p {
    margin: 4px 0;
  }
}

/* responsive */
@media (max-width: 768px) {
  .destination-map {
    padding: 10px;
    min-height: calc(100vh - 180px);
    gap: 15px;
  }

  .map-wrapper {
    min-height: 300px;
  }

  .detail-item {
    flex-direction: column;
    gap: 5px;
  }
}

/* Modo oscuro */
:global(html[data-theme="dark"]) {
  .destination-map {
    background-color: var(--color-background-dark);
  }

  .map-header {
    h2 {
      color: var(--color-text-primary-dark);
    }

    p {
      color: var(--color-text-secondary-dark);
    }
  }

  .destination-info {
    background-color: var(--color-surface-dark);
    color: var(--color-text-primary-dark);
  }

  .info-card h3 {
    color: var(--color-text-primary-dark);
  }

  .detail-item {
    border-bottom-color: var(--color-border-dark);

    .label {
      color: var(--color-text-secondary-dark);
    }

    .value {
      color: var(--color-text-primary-dark);
    }
  }
}
</style>
