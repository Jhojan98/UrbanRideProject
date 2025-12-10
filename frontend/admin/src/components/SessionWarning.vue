<template>
  <div v-if="showWarning && isAuthenticated" class="session-warning">
    <span class="warning-icon">⚠️</span>
    <span class="warning-text">Tu sesión expirará en {{ remainingMinutes }} minuto{{ remainingMinutes !== 1 ? 's' : '' }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const remainingTime = ref(0);
const INACTIVITY_TIMEOUT = 10 * 60 * 1000; // 10 minutos
const WARNING_THRESHOLD = 2 * 60 * 1000; // Mostrar warning 2 minutos antes

const isAuthenticated = computed(() => !!authStore.token);

const remainingMinutes = computed(() => {
  return Math.ceil(remainingTime.value / 60000);
});

const showWarning = computed(() => {
  return remainingTime.value > 0 && remainingTime.value <= WARNING_THRESHOLD;
});

let updateInterval: number | null = null;

const updateRemainingTime = () => {
  const lastActivity = localStorage.getItem('lastActivity');
  if (!lastActivity || !authStore.token) {
    remainingTime.value = 0;
    return;
  }

  const timeSinceLastActivity = Date.now() - parseInt(lastActivity);
  remainingTime.value = INACTIVITY_TIMEOUT - timeSinceLastActivity;

  if (remainingTime.value < 0) {
    remainingTime.value = 0;
  }
};

onMounted(() => {
  updateRemainingTime();
  updateInterval = window.setInterval(updateRemainingTime, 5000); // Actualizar cada 5 segundos
});

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval);
  }
});
</script>

<style scoped>
.session-warning {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  animation: slideIn 0.3s ease-out;
}

.warning-icon {
  font-size: 1.5rem;
}

.warning-text {
  font-weight: 600;
  color: #856404;
  font-size: 0.95rem;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .session-warning {
    top: 10px;
    right: 10px;
    left: 10px;
    padding: 0.75rem 1rem;
  }

  .warning-text {
    font-size: 0.85rem;
  }
}
</style>
