<template>
  <Transition name="slide-up">
    <div v-if="tripStore.isTripActive" class="trip-in-progress">
      <div class="trip-container">
        <div class="trip-header">
          <div class="trip-icon">
            <span class="bike-icon">🚴</span>
          </div>
          <h2 class="trip-title">{{ $t('travel.inProgress.title') }}</h2>
        </div>

        <div class="trip-content">
          <div class="trip-info">
            <div class="info-item">
              <span class="info-label">{{ $t('travel.inProgress.duration') }}</span>
              <span class="info-value time-display">{{ formattedDuration }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">{{ $t('travel.inProgress.status') }}</span>
              <span class="info-value status-active">
                <span class="pulse-dot"></span>
                {{ $t('travel.inProgress.active') }}
              </span>
            </div>
          </div>

          <div class="trip-message">
            <p>{{ $t('travel.inProgress.message') }}</p>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useTripStore } from '@/services/travelNotifications';

const { t: $t } = useI18n();
const tripStore = useTripStore();

// Actualizar duración cada segundo
const formattedDuration = ref('00:00:00');
let updateInterval: number | null = null;

const updateDuration = () => {
  formattedDuration.value = tripStore.formattedTripDuration;
};

onMounted(() => {
  updateDuration();
  updateInterval = window.setInterval(updateDuration, 1000);
});

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval);
    updateInterval = null;
  }
});
</script>

<style scoped>
.trip-in-progress {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  max-width: 500px;
  width: 90%;
  animation: pulse-border 2s infinite;
}

.trip-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);
  color: white;
}

.trip-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.trip-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bike-icon {
  font-size: 28px;
  animation: bike-bounce 1s ease-in-out infinite;
}

@keyframes bike-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.trip-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.trip-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.trip-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  background: rgba(255, 255, 255, 0.15);
  padding: 16px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.9;
  font-weight: 600;
}

.info-value {
  font-size: 20px;
  font-weight: 700;
}

.time-display {
  font-family: 'Courier New', monospace;
  font-size: 24px;
  letter-spacing: 2px;
}

.status-active {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  background: #4ade80;
  border-radius: 50%;
  animation: pulse-dot 1.5s ease-in-out infinite;
  box-shadow: 0 0 10px #4ade80;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

.trip-message {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 12px;
  text-align: center;
  backdrop-filter: blur(10px);
}

.trip-message p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.95;
}

@keyframes pulse-border {
  0%, 100% {
    filter: drop-shadow(0 0 0 transparent);
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(102, 126, 234, 0.6));
  }
}

/* Transición de entrada/salida */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.slide-up-enter-from {
  transform: translate(-50%, 100px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translate(-50%, 100px);
  opacity: 0;
}

/* Responsive */
@media (max-width: 600px) {
  .trip-in-progress {
    bottom: 10px;
    width: 95%;
  }

  .trip-container {
    padding: 20px;
  }

  .trip-title {
    font-size: 20px;
  }

  .trip-info {
    grid-template-columns: 1fr;
  }

  .info-item {
    padding: 12px;
  }

  .time-display {
    font-size: 20px;
  }
}
</style>
