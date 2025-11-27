<template>
  <div class="ultima-milla">

    <h2>Viaje de Última Milla</h2>
    <p class="sub">Selecciona tu punto de partida y la estación de destino</p>

    <!-- ORIGEN -->
    <div class="select-box">
      <label>📍 Punto de Partida (Metro / Estación)</label>
      <select v-model="selectedOrigin">
        <option :value="null" disabled>Seleccione un punto</option>
        <option 
          v-for="m in metros" 
          :key="m.id"
          :value="m"
        >
          {{ m.name }}
        </option>
      </select>
    </div>

    <!-- Mostrar info origen (solo disponibles/estado, sin coordenadas) -->
    <div v-if="selectedOrigin" class="info-card">
      <h3>{{ selectedOrigin.name }}</h3>
      <p>🚲 Bicicletas disponibles: <strong>{{ selectedOrigin.free_spots ?? '—' }}</strong></p>
      <p :class="['status', (selectedOrigin.status ?? '').toLowerCase()]">
        Estado: {{ selectedOrigin.status ?? 'N/A' }}
      </p>
    </div>

    <!-- DESTINO -->
    <div class="select-box">
      <label>🚲 Estación de Bicicletas (Destino)</label>
      <select v-model="selectedDest">
        <option :value="null" disabled>Seleccione estación</option>
        <option 
          v-for="s in stations" 
          :key="s.id"
          :value="s"
        >
          {{ s.name }} — {{ s.free_spots }} puestos libres
        </option>
      </select>
    </div>

    <!-- DETALLES DE LA ESTACIÓN DESTINO -->
    <div v-if="selectedDest" class="info-card">
      <h3>{{ selectedDest.name }}</h3>

      <p>🚲 Puestos libres: <strong>{{ selectedDest.free_spots }}</strong></p>

      <p :class="['status', selectedDest.status.toLowerCase()]">
        Estado: {{ selectedDest.status }}
      </p>

      <p>📍 Coordenadas: {{ selectedDest.lat }}, {{ selectedDest.lng }}</p>
    </div>

    <button 
      class="btn-confirm"
      :disabled="!selectedOrigin || !selectedDest"
      @click="confirm"
    >
      Confirmar Ruta
    </button>

  </div>
</template>

<script setup lang="ts">
import { ref, watch, defineEmits } from "vue"

// mocks con coordenadas en Villavicencio
const metros = [
  // si quieres que el origen tenga free_spots/status, añade esos campos
  { id: 1, name: "Metro Estación Central", lat: 4.1425, lng: -73.6312, free_spots: 5, status: "ACTIVE" },
  { id: 2, name: "Metro Sikuani", lat: 4.1480, lng: -73.6270, free_spots: 2, status: "ACTIVE" }
]

const stations = [
  { id: 10, name: "Estación Centro", free_spots: 5, status: "ACTIVE", lat: 4.1430, lng: -73.6290 },
  { id: 11, name: "Parque Sikuani", free_spots: 2, status: "ACTIVE", lat: 4.1475, lng: -73.6260 },
  { id: 12, name: "Zona Universitaria", free_spots: 0, status: "FULL", lat: 4.1505, lng: -73.6235 }
]

// estado: bind directo a objetos (no ids)
const selectedOrigin = ref<any | null>(null)
const selectedDest = ref<any | null>(null)

const emit = defineEmits<{
  confirm: [{ origin: any; destination: any }]
  "update:origin": [any | null]
  "update:destination": [any | null]
}>()

// emitir objetos completos para que MapComponent reciba lat/lng directamente
watch(selectedOrigin, (o) => {
  console.log("UltimaMilla emit origin:", o);
  emit("update:origin", o ?? null)
}, { immediate: true })

watch(selectedDest, (d) => {
  console.log("UltimaMilla emit destination:", d);
  emit("update:destination", d ?? null)
}, { immediate: true })

function confirm() {
  emit("confirm", {
    origin: selectedOrigin.value,
    destination: selectedDest.value
  })
}
</script>

<style scoped>
/* ...existing styles... */
.ultima-milla { padding: 1.5rem; }
.select-box { margin-bottom: 1rem; display:flex; flex-direction:column; }
select { padding:.5rem; border-radius:6px; border:1px solid #ccc; }
.info-card { background:#fff; padding:1rem; border-radius:10px; margin-top:1rem; box-shadow:0 2px 4px rgba(0,0,0,.1); }
.status.active { color: green; font-weight: bold; }
.status.full { color: red; font-weight: bold; }
.btn-confirm { margin-top:1rem; width:100%; padding:.7rem; background:#2E7D32; color:white; border-radius:8px; border:none; }
</style>