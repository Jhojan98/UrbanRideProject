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


<style lang="scss" scoped>
  @import "@/styles/maps.scss";
</style>