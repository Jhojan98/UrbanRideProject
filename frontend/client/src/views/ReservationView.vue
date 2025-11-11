<template>
  <div class="reservation-view">
    <template v-if="!hasActiveReservation">
      <div class="reservation-grid">
        <aside class="left-panel">
          <ReserveFormComponent />
        </aside>
        <section class="map-panel">
          <MapComponent />
        </section>
      </div>
    </template>
    <ConfirmationComponent v-else />
  </div>
</template>

<script setup lang="ts">
import ConfirmationComponent from '@/components/reservation/ConfirmationComponent.vue';
import MapComponent from '@/components/reservation/MapComponent.vue';
import ReserveFormComponent from '@/components/reservation/ReserveFormComponent.vue';
import { useReservation } from '@/composables/useReservation';

const { hasActiveReservation } = useReservation();
</script>

<style scoped>
.reservation-view {
  width: 100%;
  min-height: calc(100vh - 140px);
}

.reservation-grid {
  display: grid;
  grid-template-columns: 360px 1fr; /* panel izquierdo fijo, mapa flexible */
  gap: 1rem;
  align-items: start;
}

.left-panel {
  position: sticky;
  top: 84px; /* deja espacio del header fijo */
}

.map-panel {
  width: 100%;
}

@media (max-width: 992px) {
  .reservation-grid {
    grid-template-columns: 1fr; /* apilar en móvil */
  }
  .left-panel {
    position: static;
    top: auto;
  }
}
</style>
<style lang="">
    
</style>