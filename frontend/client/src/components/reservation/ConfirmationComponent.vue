<template>
  <div class="reservation">
    <!-- Modal de éxito (viaje finalizado) -->
    <Transition name="fade">
      <div v-if="showSuccessModal" class="success-modal-overlay">
        <div class="success-modal">
          <div class="success-icon">✓</div>
          <h2>¡Viaje Finalizado!</h2>
          <div class="success-message">
            <p>Tu viaje ha sido registrado exitosamente.</p>
            <p class="cost-info">
              💳 <strong>Cobro: {{ tripDetails.estimatedCost }}</strong>
            </p>
            <p class="email-info">
              📧 Recibirás la factura por correo electrónico en los próximos minutos.
            </p>
          </div>
          <button class="butn-primary" @click="() => { clearReservation(); router.push('/'); }">
            Volver al inicio
          </button>
        </div>
      </div>
    </Transition>

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

      <div class="unlock-section">
        <p class="slot-hint" v-if="slotId">
          Tu slot asignado es <strong>{{ slotId }}</strong>.
        </p>
        <div class="lock-status" v-if="isUnlocked">
          <i class="fa fa-lock-open" style="color:#2E7D32;margin-right:6px"></i>
          <span>🔓 Bicicleta desbloqueada — viaje iniciado</span>
        </div>
        <label class="detail-label" for="bicycle-code">
          {{ $t('reservation.confirmation.enterBikeCode') || 'Digita el número de matrícula que se encuentra en la bicicleta' }}
        </label>
        <input
          id="bicycle-code"
          v-model="bicycleCode"
          type="text"
          inputmode="numeric"
          maxlength="6"
          placeholder="Código de 6 dígitos"
          class="input"
        />
        <button class="butn-primary" :disabled="isLoading || isUnlocked" @click="unlockBike">
          <i :class="isUnlocked ? 'fa fa-lock-open' : 'fa fa-lock'" style="margin-right:6px"></i>
          {{ isLoading ? $t('common.loading') : (isUnlocked ? 'Desbloqueado' : ($t('reservation.confirmation.unlock') || 'Desbloquear')) }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useReservation } from '@/composables/useReservation';
import { useTravelStore } from '@/stores/travel';
import { getAuth } from 'firebase/auth';

interface TripDetails {
  type: string;
  estimatedCost: string;
  bikeType: string;
}

const router = useRouter();
const { t: $t } = useI18n();
const { hasActiveReservation, getTripType, getEstimatedCost, getBikeType, clearReservation, reservationData } = useReservation();
const travelStore = useTravelStore();

const isLoading = ref(false);
const isUnlocked = ref(false);
const showSuccessModal = ref(false);
const timeLeft = ref<string>('10:00');
const tripDetails = ref<TripDetails>({
  type: 'Última Milla',
  estimatedCost: '$5.00',
  bikeType: 'Mecánica'
});
const bicycleCode = ref<string>('');
const slotId = computed<string | null>(() => {
  const data: any = reservationData.value;
  return data?.startResponse?.slotId ?? data?.slotId ?? null;
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

// Desbloqueo/verificación de bicicleta: si es correcto, damos por iniciado y cerramos
const unlockBike = async () => {
  if (isLoading.value) return;
  const code = (bicycleCode.value || '').trim();
  if (!code || code.length !== 6) {
    window.alert('Por favor, digita el código de 6 dígitos de la bicicleta');
    return;
  }

  if (!reservationData.value) {
    window.alert('No hay datos de reserva disponibles');
    return;
  }

  try {
    isLoading.value = true;
    const firebaseAuth = getAuth();
    const currentUser = firebaseAuth.currentUser;
    const userUid = currentUser?.uid;
    if (!userUid) throw new Error('Usuario no autenticado');

    // Verificar bicicleta (backend persiste el viaje si es correcto)
    const resp = await travelStore.verifyBicycle(userUid, code);
    console.log('[ConfirmationComponent] verify-bicycle OK:', resp);

    // Mostrar modal de éxito
    isUnlocked.value = true;
    if (timerInterval) clearInterval(timerInterval);
    
    // Mostrar el modal de éxito por 3 segundos
    showSuccessModal.value = true;
    setTimeout(() => {
      clearReservation();
      router.push('/');
    }, 3000);
  } catch (err: any) {
    console.error('[ConfirmationComponent] Error verificando bicicleta:', err);
    window.alert('No se pudo verificar/desbloquear la bicicleta: ' + (err?.message ?? String(err)));
  } finally {
    isLoading.value = false;
  }
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

// Modal de éxito - viaje finalizado
.success-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.success-modal {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  max-width: 450px;
  width: 90%;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.4s ease;

  .success-icon {
    font-size: 4rem;
    color: #2E7D32;
    margin-bottom: 1rem;
    animation: popIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }

  h2 {
    color: #004E61;
    font-size: 1.8rem;
    margin: 0 0 1.5rem 0;
    font-weight: 600;
  }

  .success-message {
    margin-bottom: 2rem;
    
    p {
      margin: 0.75rem 0;
      color: #666;
      font-size: 1rem;
      line-height: 1.5;

      &.cost-info {
        background: #f0f7ff;
        padding: 1rem;
        border-radius: 8px;
        border-left: 4px solid #2E7D32;
        font-weight: 500;
        color: #004E61;
        margin: 1rem 0;
      }

      &.email-info {
        background: #fff3cd;
        padding: 1rem;
        border-radius: 8px;
        border-left: 4px solid #ff9800;
        font-size: 0.95rem;
      }
    }
  }

  .butn-primary {
    width: 100%;
    padding: 0.8rem 1.5rem;
    font-size: 1rem;
    margin-top: 0.5rem;
  }
}

/* Dark mode styles */
[data-theme="dark"] .success-modal {
  background: var(--color-surface-dark);

  .success-icon {
    color: var(--color-primary-light);
  }

  h2 {
    color: var(--color-primary-light);
  }

  .success-message {
    p {
      color: var(--color-text-secondary-dark);

      &.cost-info {
        background: rgba(46, 125, 50, 0.15);
        color: var(--color-primary-light);
        border-left-color: var(--color-primary-light);
      }

      &.email-info {
        background: rgba(255, 152, 0, 0.15);
        color: var(--color-text-primary-dark);
        border-left-color: #ff9800;
      }
    }
  }
}

// Transiciones
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes popIn {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

</style>
