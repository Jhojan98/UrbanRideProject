<template>
  <div class="reservation">
    <!-- Modal de reporte de problemas -->
    <Transition name="fade">
      <div v-if="showProblemModal" class="problem-modal-overlay" @click.self="closeProblemModal">
        <div class="problem-modal">
          <button class="close-btn" @click="closeProblemModal">✕</button>
          <h2>{{ $t('reservation.confirmation.reportTitle') }}</h2>
          <p class="modal-subtitle">{{ $t('reservation.confirmation.reportSubtitle') }}</p>

          <form @submit.prevent="submitProblemReport">
            <label class="input-label">{{ $t('reservation.confirmation.problemType') }}</label>
            <select v-model="problemForm.type" required>
              <option value="">{{ $t('reservation.confirmation.selectProblem') }}</option>
              <option value="BICYCLE">{{ $t('reservation.confirmation.problemBicycle') }}</option>
              <option value="LOCK">{{ $t('reservation.confirmation.problemLock') }}</option>
              <option value="STATION">{{ $t('reservation.confirmation.problemStation') }}</option>
              <option value="OTHER">{{ $t('reservation.confirmation.problemOther') }}</option>
            </select>

            <label class="input-label">{{ $t('reservation.confirmation.problemDescription') }}</label>
            <textarea
              v-model="problemForm.description"
              :placeholder="$t('reservation.confirmation.describeProblem')"
              rows="4"
              required
            ></textarea>

            <div class="modal-actions">
              <button type="button" class="btn-secondary" @click="closeProblemModal">{{ $t('common.cancel') }}</button>
              <button type="submit" class="btn-primary" :disabled="isSubmittingProblem">
                {{ isSubmittingProblem ? $t('reservation.confirmation.reporting') : $t('reservation.confirmation.sendReport') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>

    <!-- Modal de éxito (viaje finalizado) -->
    <Transition name="fade">
      <div v-if="showSuccessModal" class="success-modal-overlay">
        <div class="success-modal">
          <div class="success-icon">🚲</div>
          <h2>{{ $t('reservation.confirmation.tripStarted') || '¡Viaje Iniciado!' }}</h2>
          <div class="success-message">
            <p>{{ $t('reservation.confirmation.enjoyRide') || 'Disfruta tu viaje. Tu tiempo ha comenzado.' }}</p>
          </div>
          <button class="butn-primary" @click="() => { clearReservation(); router.push('/'); }">
            {{ $t('common.continue') || 'Continuar' }}
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
          {{ $t('reservation.confirmation.slotHint') }} <strong>{{ slotId }}</strong>.
        </p>
        <div class="lock-status" v-if="isUnlocked">
          <i class="fa fa-lock-open" style="color:#2E7D32;margin-right:6px"></i>
          <span>🔓 {{ $t('reservation.confirmation.bikeUnlocked') }}</span>
        </div>
        <label class="detail-label" for="bicycle-code">
          {{ $t('reservation.confirmation.enterBikeCode') }}
        </label>
        <input
          id="bicycle-code"
          v-model="bicycleCode"
          type="text"
          inputmode="numeric"
          maxlength="6"
          :placeholder="$t('reservation.confirmation.codePlaceholder')"
          class="input"
        />
        <button class="butn-primary" :disabled="isLoading || isUnlocked" @click="unlockBike">
          <i :class="isUnlocked ? 'fa fa-lock-open' : 'fa fa-lock'" style="margin-right:6px"></i>
          {{ isLoading ? $t('common.loading') : (isUnlocked ? $t('reservation.confirmation.unlocked') : $t('reservation.confirmation.unlock')) }}
        </button>

        <!-- Botón de reportar problema -->
        <button
          v-if="isUnlocked"
          class="btn-report-problem"
          @click="showProblemModal = true"
          :title="$t('reservation.confirmation.reportProblem')"
        >
          <i class="fa fa-exclamation-circle"></i>
          {{ $t('reservation.confirmation.reportProblem') }}
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
import { useSupportStore } from '@/stores/support';
import { getAuth } from 'firebase/auth';

interface TripDetails {
  type: string;
  estimatedCost: string;
  bikeType: string;
}

interface ProblemForm {
  type: string;
  description: string;
}

const router = useRouter();
const { t: $t } = useI18n();
const { hasActiveReservation, getTripType, getEstimatedCost, getBikeType, clearReservation, reservationData, getReservationStartTime } = useReservation();
const travelStore = useTravelStore();
const supportStore = useSupportStore();

const isLoading = ref(false);
const isUnlocked = ref(false);
const showSuccessModal = ref(false);
const showProblemModal = ref(false);
const isSubmittingProblem = ref(false);
const timeLeft = ref<string>('10:00');
const tripDetails = ref<TripDetails>({
  type: 'Última Milla',
  estimatedCost: '$5.00',
  bikeType: 'Mecánica'
});
const bicycleCode = ref<string>('');
const problemForm = ref<ProblemForm>({
  type: '',
  description: ''
});
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
  if (timerInterval) clearInterval(timerInterval);

  const updateTime = () => {
    const startedAt = getReservationStartTime.value ?? Date.now();
    const elapsed = Math.floor((Date.now() - startedAt) / 1000);
    const remaining = Math.max(0, startTime - elapsed);

    currentTime.value = remaining;
    timeLeft.value = formatTime(remaining);

    if (remaining <= 0) {
      if (timerInterval) clearInterval(timerInterval);
      clearReservation();
      router.push('/');
    }
  };

  updateTime(); // Initial update
  timerInterval = window.setInterval(updateTime, 1000);
};

const closeProblemModal = () => {
  showProblemModal.value = false;
  problemForm.value = { type: '', description: '' };
};

const submitProblemReport = async () => {
  if (!problemForm.value.type || !problemForm.value.description.trim()) {
    alert('Por favor, completa todos los campos');
    return;
  }

  try {
    isSubmittingProblem.value = true;

    // Obtener el ID de viaje activo
    const firebaseAuth = getAuth();
    const currentUser = firebaseAuth.currentUser;
    const userUid = currentUser?.uid;

    if (!userUid) {
      throw new Error('Usuario no autenticado');
    }

    // Enviar reporte como una queja
    await supportStore.submitComplaint({
      description: `[VIAJE] ${problemForm.value.type}: ${problemForm.value.description}`,
      type: 'BICYCLE', // Mapear según el tipo
      travelId: undefined // Se podría obtener del reservationData
    });

    alert('Problema reportado exitosamente. Gracias por tu retroalimentación.');
    closeProblemModal();
  } catch (error) {
    console.error('Error reportando problema:', error);
    alert('No se pudo registrar el reporte. Intenta nuevamente.');
  } finally {
    isSubmittingProblem.value = false;
  }
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

// Modal de reporte de problemas
.problem-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9998;
}

.problem-modal {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease;
  position: relative;

  .close-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #999;
    transition: color 0.3s;

    &:hover {
      color: #333;
    }
  }

  h2 {
    color: #004E61;
    font-size: 1.5rem;
    margin: 0 0 0.5rem 0;
    font-weight: 600;
  }

  .modal-subtitle {
    color: #666;
    margin-bottom: 1.5rem;
    font-size: 0.95rem;
  }

  form {
    display: flex;
    flex-direction: column;
    gap: 1.2rem;
  }

  .input-label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #333;
    font-size: 0.95rem;
  }

  select,
  textarea {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    font-family: inherit;
    color: #333;
    background: white;
    transition: border-color 0.3s;

    &::placeholder {
      color: #999;
    }

    &:focus {
      outline: none;
      border-color: #2E7D32;
      box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
    }

    &:hover:not(:focus) {
      border-color: #bdbdbd;
    }
  }

  .modal-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    margin-top: 1rem;
  }

  .btn-primary,
  .btn-secondary {
    padding: 0.7rem 1.5rem;
    border: none;
    border-radius: 8px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
  }

  .btn-primary {
    background: #2E7D32;
    color: white;

    &:hover:not(:disabled) {
      background: #1f5620;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
    }

    &:disabled {
      background: #bdbdbd;
      cursor: not-allowed;
    }
  }

  .btn-secondary {
    background: #f0f0f0;
    color: #333;

    &:hover {
      background: #e0e0e0;
    }
  }
}

[data-theme="dark"] .problem-modal {
  background: var(--color-surface-dark);

  .close-btn {
    color: var(--color-text-secondary-dark);

    &:hover {
      color: var(--color-text-primary-dark);
    }
  }

  h2 {
    color: var(--color-primary-light);
  }

  .modal-subtitle {
    color: var(--color-text-secondary-dark);
  }

  .input-label {
    color: var(--color-text-secondary-dark);
  }

  select,
  textarea {
    background: var(--color-background-dark);
    color: var(--color-text-primary-dark);
    border-color: var(--color-border-dark);

    &::placeholder {
      color: var(--color-text-secondary-dark);
    }

    &:focus {
      border-color: var(--color-primary-light);
      box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
    }

    &:hover:not(:focus) {
      border-color: rgba(255, 255, 255, 0.2);
    }
  }

  .btn-secondary {
    background: rgba(255, 255, 255, 0.1);
    color: var(--color-text-primary-dark);

    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }
}

// Botón de reportar problema
.btn-report-problem {
  width: 100%;
  padding: 0.7rem 1.2rem;
  margin-top: 1rem;
  background: #ff9800;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;

  i {
    font-size: 1.1rem;
  }

  &:hover {
    background: #f57c00;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

[data-theme="dark"] .btn-report-problem {
  background: #ff9800;

  &:hover {
    background: #ffb74d;
  }
}

</style>
