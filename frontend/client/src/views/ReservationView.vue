<template>
  <div class="reservation-view">
    <template v-if="!hasActiveReservation">
      <div class="reservation-grid">
        <aside class="left-panel">
          <ReserveFormComponent
            :station="origin"
            :balance="userBalance"
            :origin="origin"
            :destination="destination"
            @update:origin="(o) => origin = o"
            @update:destination="(d) => destination = d"
          />
        </aside>
        <section class="map-panel">
          <MapComponent :origin="origin" :destination="destination" />
        </section>
      </div>
    </template>
    <ConfirmationComponent v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ConfirmationComponent from '@/components/reservation/ConfirmationComponent.vue';
import MapComponent from '@/components/reservation/MapComponent.vue';
import ReserveFormComponent from '@/components/reservation/ReserveFormComponent.vue';
import { useReservation } from '@/composables/useReservation';

const { hasActiveReservation } = useReservation();

// Tipos para coordinar origen/destino entre el formulario y el mapa
interface Endpoint {
  idStation?: number
  latitude: number
  longitude: number
  name?: string
  nameStation?: string
  free_spots?: number
  status?: string
  availableSlots?: number
  totalSlots?: number
  type?: string
  mechanical?: number
  electric?: number
}

const origin = ref<Endpoint | null>(null)
const destination = ref<Endpoint | null>(null)
const userBalance = ref<number>(50000) // Saldo inicial de ejemplo

// TODO: Obtener el balance real del usuario desde el perfil o store
onMounted(() => {
  // Aquí se debería cargar el balance del usuario desde un store (por ejemplo, userStore)
  // Por ahora usamos un valor por defecto
})

// Selection by map click disabled; origin/destination are chosen via dropdown in the form.
</script>


<style lang="scss" scoped>
  @import "@/styles/maps.scss";
</style>
