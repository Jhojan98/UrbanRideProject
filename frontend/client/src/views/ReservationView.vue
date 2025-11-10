<template>
  <div class="reservation">
    <div class="reservation-card">
      <h1 class="reservation-title">Tiempo restante de reserva</h1>
      <div class="timer">{{ timeLeft }}</div>
      <div class="timer-label">minutos</div>
      
      <div class="trip-details">
        <h3>Detalles del viaje</h3>
        <div class="detail-item">
          <span class="detail-label">Tipo de viaje:</span>
          <span class="detail-value">{{ tripDetails.type }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Costo estimado:</span>
          <span class="detail-value">{{ tripDetails.estimatedCost }}</span>
        </div>
      </div>
      
      <button class="btn-confirm" @click="confirmReservation">Confirmar Reserva</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

interface TripDetails {
  type: string;
  estimatedCost: string;
}

const router = useRouter();
const timeLeft = ref<string>('10:00');
const tripDetails = ref<TripDetails>({
  type: 'Última Milla',
  estimatedCost: '$5.00'
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
  timerInterval = setInterval(() => {
    if (currentTime.value > 0) {
      currentTime.value--;
      timeLeft.value = formatTime(currentTime.value);
    } else {
      if (timerInterval) {
        clearInterval(timerInterval);
      }
      // Redirigir cuando se acabe el tiempo
      router.push('/');
    }
  }, 1000);
};

const confirmReservation = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
  // Aquí iría la lógica para confirmar la reserva
  router.push('/');
};

onMounted(() => {
  startTimer();
});

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
});
</script>

<style scoped>
.reservation {
  min-height: calc(100vh - 140px);
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: #f8f9fa;
}

.reservation-card {
  background: #FFFFFF;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.reservation-title {
  color: #004E61;
  margin-bottom: 2rem;
  font-size: 1.5rem;
}

.timer {
  font-size: 4rem; /* Tiempo grande y visible */
  font-weight: bold;
  color: #2E7D32;
  margin-bottom: 0.5rem;
}

.timer-label {
  color: #666;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.trip-details {
  background-color: #f8f9fa; /* Fondo gris claro para destacar */
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  text-align: left; /* Alinea el texto a la izquierda dentro de este contenedor */
}

.trip-details h3 {
  color: #004E61;
  margin-bottom: 1rem;
  text-align: center; /* Pero el título centrado */
}

.detail-item {
  display: flex;
  justify-content: space-between; /* Etiqueta a la izquierda, valor a la derecha */
  margin-bottom: 0.5rem;
}

.detail-label {
  color: #666;
  font-weight: 500;
}

.detail-value {
  color: #004E61;
  font-weight: bold;
}

.btn-confirm {
  background-color: #2E7D32;
  color: #FFFFFF;
  border: none;
  padding: 12px 32px;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  width: 100%; /* Botón de ancho completo */
  transition: background-color 0.3s;
}

.btn-confirm:hover {
  background-color: #266629;
}
</style>