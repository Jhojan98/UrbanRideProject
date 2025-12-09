<template>
  <Transition name="slide-fade">
    <div v-if="tripStore.isVisible && tripStore.notification" class="notification-popup" :class="notificationClass">
      <div class="notification-content">
        <div class="notification-icon">
          <span v-if="tripStore.notification.type === 'EXPIRED_TRAVEL'">⏰</span>
          <span v-else-if="tripStore.notification.type === 'START_TRAVEL'">🚴</span>
          <span v-else-if="tripStore.notification.type === 'END_TRAVEL'">✅</span>
        </div>
        <div class="notification-body">
          <h3 class="notification-title">{{ notificationTitle }}</h3>
          <p class="notification-message">{{ notificationMessage }}</p>
        </div>
        <button class="close-btn" @click="tripStore.closeNotification()" :aria-label="$t('reservation.notifications.closeLabel')">
          ×
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useTripStore } from '@/services/travelNotifications';

const { t: $t } = useI18n();
const tripStore = useTripStore();

const notificationClass = computed(() => {
  if (!tripStore.notification) return '';

  switch (tripStore.notification.type) {
    case 'EXPIRED_TRAVEL':
      return 'notification-error';
    case 'START_TRAVEL':
      return 'notification-success';
    case 'END_TRAVEL':
      return 'notification-info';
    default:
      return '';
  }
});

const notificationTitle = computed(() => {
  if (!tripStore.notification) return '';

  switch (tripStore.notification.type) {
    case 'EXPIRED_TRAVEL':
      return $t('reservation.notifications.expiredTravel');
    case 'START_TRAVEL':
      return $t('reservation.notifications.startTravel');
    case 'END_TRAVEL':
      return $t('reservation.notifications.endTravel');
    default:
      return $t('reservation.notifications.defaultNotification');
  }
});

const notificationMessage = computed(() => {
  if (!tripStore.notification) return '';

  switch (tripStore.notification.type) {
    case 'EXPIRED_TRAVEL':
      return $t('reservation.notifications.expiredTravelMsg');
    case 'START_TRAVEL':
      return $t('reservation.notifications.startTravelMsg');
    case 'END_TRAVEL':
      return $t('reservation.notifications.endTravelMsg');
    default:
      return tripStore.notification.message;
  }
});

// Auto-close after 8 seconds
let autoCloseTimer: number | null = null;

const startAutoCloseTimer = () => {
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer);
  }
  autoCloseTimer = window.setTimeout(() => {
    tripStore.closeNotification();
  }, 8000);
};

// Observar cambios en isVisible para reiniciar el timer
computed(() => {
  if (tripStore.isVisible) {
    startAutoCloseTimer();
  }
  return tripStore.isVisible;
});
</script>

<style scoped>
.notification-popup {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
  min-width: 320px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.notification-content {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  gap: 16px;
  background: white;
}

.notification-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.notification-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #4a5568;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  color: #a0aec0;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.close-btn:hover {
  background: #f7fafc;
  color: #4a5568;
}

/* Variantes de color */
.notification-error .notification-content {
  border-left: 4px solid #f56565;
}

.notification-error .notification-icon {
  color: #f56565;
}

.notification-success .notification-content {
  border-left: 4px solid #48bb78;
}

.notification-success .notification-icon {
  color: #48bb78;
}

.notification-info .notification-content {
  border-left: 4px solid #4299e1;
}

.notification-info .notification-icon {
  color: #4299e1;
}

/* Animaciones */
.slide-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.slide-fade-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Responsive */
@media (max-width: 640px) {
  .notification-popup {
    top: 10px;
    right: 10px;
    left: 10px;
    max-width: none;
    min-width: 0;
  }
}
</style>
