<template>
  <Transition name="slide-up">
    <div v-if="tripStore.isTripActive" class="trip-in-progress" :class="{ 'minimized': isMinimized }">
      <div class="trip-container">
        <div class="trip-header">
          <div class="trip-icon">
            <span class="bike-icon">🚴</span>
          </div>
          <h2 class="trip-title" v-if="!isMinimized">{{ $t('travel.inProgress.title') }}</h2>
          <div class="header-controls">
            <button class="icon-btn minimize-btn" @click="toggleMinimize" :title="isMinimized ? 'Expandir' : 'Minimizar'">
              {{ isMinimized ? '▲' : '▼' }}
            </button>
          </div>
        </div>

        <div class="trip-content" v-show="!isMinimized">
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

          <!-- Botón provisional para cerrar -->
          <button class="debug-btn" @click="forceStop">
           Force Stop (Debug)
          </button>
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

// Estado local para minimizar
const isMinimized = ref(false);

const updateDuration = () => {
  formattedDuration.value = tripStore.formattedTripDuration;
};

const forceStop = () => {
  // Use the new forceResetState action which actually clears variables
  tripStore.forceResetState();
};

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value;
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
}

.trip-container {
  background: white; /* Fallback */
  background: var(--color-background-light, #ffffff);
  border: 1px solid var(--color-border-light, #e0e0e0);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  color: var(--color-text-primary-light, #333);
}

[data-theme="dark"] .trip-container {
  background: var(--color-surface-dark, #1E2128);
  border-color: var(--color-border-dark, #333);
  color: var(--color-text-primary-dark, #fff);
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
  background: var(--color-primary-light, #2E7D32);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
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
  font-size: 20px;
  font-weight: 700;
  color: inherit;
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
  background: var(--color-background-light, #f5f5f5);
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid transparent;
}

[data-theme="dark"] .info-item {
  background: rgba(255, 255, 255, 0.05);
}

.info-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.7;
  font-weight: 600;
}

.info-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary-light, #2E7D32);
}

[data-theme="dark"] .info-value {
  color: var(--color-green-light, #66BB6A);
}

.time-display {
  font-family: 'Roboto Mono', monospace; /* More modern than Courier New */
  font-size: 24px;
  letter-spacing: 1px;
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
  background: rgba(46, 125, 50, 0.1); /* Light green tint */
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  border: 1px dashed var(--color-primary-light, #2E7D32);
}

[data-theme="dark"] .trip-message {
  background: rgba(102, 187, 106, 0.1);
  border-color: var(--color-green-light, #66BB6A);
}

.trip-message p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: inherit;
}

.debug-btn {
  background: #f44336; /* Red for debug/stop */
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  align-self: center;
  margin-top: 10px;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.debug-btn:hover {
  opacity: 1;
}

.header-controls {
  margin-left: auto;
}

.icon-btn {
  background: transparent;
  border: 1px solid var(--color-border-light, #e0e0e0);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-primary-light, #333);
  transition: all 0.2s;
}

[data-theme="dark"] .icon-btn {
  border-color: var(--color-border-dark, #333);
  color: var(--color-text-primary-dark, #fff);
}

.icon-btn:hover {
  background: rgba(0,0,0,0.05);
}

[data-theme="dark"] .icon-btn:hover {
  background: rgba(255,255,255,0.1);
}

.trip-in-progress.minimized {
  width: auto;
  left: auto;
  right: 20px;
  transform: none;
}

.minimized .trip-container {
  padding: 12px;
  border-radius: 50px;
}

.minimized .trip-header {
  margin-bottom: 0;
  gap: 10px;
}

.minimized .trip-icon {
  width: 32px;
  height: 32px;
}

.minimized .bike-icon {
  font-size: 18px;
}

.minimized .trip-title, 
.minimized .trip-content {
  display: none;
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
    font-size: 18px;
  }

  .time-display {
    font-size: 20px;
  }
}
</style>
