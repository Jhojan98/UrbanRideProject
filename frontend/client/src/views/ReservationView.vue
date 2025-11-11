<template>
  <div class="reservation-view">
    <MapComponent v-if="!hasActiveReservation" />
    <ConfirmationComponent v-if="hasActiveReservation" />
  </div>
</template>

<script setup lang="ts">
import ConfirmationComponent from '@/components/reservation/ConfirmationComponent.vue';
import MapComponent from '@/components/reservation/MapComponent.vue';
import { useReservation } from '@/composables/useReservation';

const { hasActiveReservation } = useReservation();
</script>

<style scoped>
.reservation-view {
  width: 100%;
  min-height: calc(100vh - 140px);
}
</style>
<style lang="">
    
</style>