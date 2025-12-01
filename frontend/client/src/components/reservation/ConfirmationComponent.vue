<template>
  <div class="reservation">
    <div class="reservation-card">
      <h1 class="reservation-title">{{ $t('reservation.confirmation.remainingTime') }}</h1>
      <div class="timer">{{ timeLeft }}</div>
      <div class="timer-label">{{ $t('common.minutes') }}</div>
      
      <div class="trip-details">
        <h3>{{ $t('reservation.confirmation.tripDetails') }}</h3>
        <div class="detail-item">
          <span class="detail-label">{{ $t('reservation.confirmation.bikeType') }}</span>
          <span class="detail-value">{{ tripDetails.bikeType }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ $t('reservation.confirmation.tripType') }}</span>
          <span class="detail-value">{{ tripDetails.type }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">{{ $t('reservation.confirmation.estimatedCost') }}</span>
          <span class="detail-value">{{ tripDetails.estimatedCost }}</span>
        </div>
      </div>
      
      <button class="butn-primary" @click="confirmReservation">{{ $t('reservation.confirmation.confirm') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useReservation } from '@/composables/useReservation';

interface TripDetails {
  type: string;
  estimatedCost: string;
  bikeType: string;
}

const router = useRouter();
const { hasActiveReservation, getTripType, getEstimatedCost, getBikeType, clearReservation } = useReservation();

const timeLeft = ref<string>('10:00');
useI18n();
const tripDetails = ref<TripDetails>({
  type: 'Última Milla',
  estimatedCost: '$5.00',
  bikeType: 'Mecánica'
});

let timerInterval: number | null = null;
const startTime = 10 * 60; // 10 minutos en segundos
const currentTime = ref<number>(startTime);

const formatTime = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
};

const startTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
  
  currentTime.value = startTime;
  timeLeft.value = formatTime(startTime);
  
  timerInterval = setInterval(() => {
    if (currentTime.value > 0) {
      currentTime.value--;
      timeLeft.value = formatTime(currentTime.value);
    } else {
      if (timerInterval) {
        clearInterval(timerInterval);
      }
      clearReservation();
      // Redirigir cuando se acabe el tiempo
      router.push('/');
    }
  }, 1000);
};

const confirmReservation = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
  clearReservation();
  // Aquí iría la lógica para confirmar la reserva
  router.push('/');
};

// Actualizar detalles del viaje cuando haya una reserva activa
watch(hasActiveReservation, (isActive) => {
  if (isActive) {
    tripDetails.value = {
      type: getTripType.value,
      estimatedCost: getEstimatedCost.value,
      bikeType: getBikeType.value
    };
    startTimer();
  }
}, { immediate: true });

onMounted(() => {
  if (hasActiveReservation.value) {
    tripDetails.value = {
      type: getTripType.value,
      estimatedCost: getEstimatedCost.value,
      bikeType: getBikeType.value
    };
    startTimer();
  }
});

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
});
</script>

<style lang="scss">
@import "@/styles/global.scss";
@import "@/styles/reservation.scss";
</style>