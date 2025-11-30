<template>
  <div class="reservation-view">
    <template v-if="!hasActiveReservation">
      <div class="reservation-grid">
        <aside class="left-panel">
          <ReserveFormComponent
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
import { ref } from 'vue'
import ConfirmationComponent from '@/components/reservation/ConfirmationComponent.vue';
import MapComponent from '@/components/reservation/MapComponent.vue';
import ReserveFormComponent from '@/components/reservation/ReserveFormComponent.vue';
import { useReservation } from '@/composables/useReservation';

const { hasActiveReservation } = useReservation();

// Estado local para coordinar origen/destino entre el formulario y el mapa
const origin = ref<any | null>(null)
const destination = ref<any | null>(null)

// Selection by map click disabled; origin/destination are chosen via dropdown in the form.
</script>


<style lang="scss" scoped>
  @import "@/styles/maps.scss";
</style>