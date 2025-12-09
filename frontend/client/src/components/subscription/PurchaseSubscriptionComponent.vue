<template>
  <div class="subscription-purchase">
    <!-- Header -->
    <div class="header">
      <h1>{{ $t('subscription.purchase.title') }}</h1>
      <p>{{ $t('subscription.purchase.subtitle') }}</p>
    </div>

    <!-- Información de suscripción -->
    <div class="subscription-info">
      <div class="info-card">
        <h3>📋 Plan Mensual</h3>
        <ul class="features">
          <li> 50 viajes mensuales incluidos</li>
          <li> Sin costo por minuto excedente</li>
          <li> Prioridad en reservas</li>
          <li> Soporte 24/7</li>
        </ul>

        <div class="price-section">
          <span class="original-price">$39.00 USD</span>
          <span class="price-label">{{ $t('subscription.purchase.price') || 'Precio del plan mensual' }}</span>
        </div>
      </div>
    </div>

    <!-- Estado actual -->
    <div class="current-status">
      <div class="balance-info">
        <h4>{{ $t('subscription.purchase.currentBalance') }}</h4>
        <p class="balance-amount">{{ formattedBalance }}</p>
      </div>

      <div class="requirement-info">
        <p v-if="canSubscribe" class="success-message">
           {{ $t('subscription.purchase.eligible') }}
        </p>
        <p v-else class="warning-message">
           {{ $t('subscription.purchase.insufficientBalance') }}
        </p>
        <p class="requirement">
          {{ $t('subscription.purchase.minimumRequirement') }}
        </p>
      </div>
    </div>

    <!-- Botón de compra -->
    <button
      class="butn-primary purchase-btn"
      @click="handlePurchase"
      :disabled="loading || !canSubscribe"
    >
      <span v-if="loading">
        {{ $t('subscription.purchase.processing') }}
      </span>
      <span v-else>
        {{ $t('subscription.purchase.button') }}
      </span>
    </button>

    <!-- Mensajes de estado -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-if="purchaseSuccess" class="success-message">
      <h3>🎉 {{ $t('subscription.purchase.success.title') }}</h3>
      <p>{{ $t('subscription.purchase.success.message') }}</p>
      <div class="success-details">
        <p><strong>Plan:</strong> Mensual</p>
        <p><strong>Viajes incluidos:</strong> 50</p>
        <p><strong>Nuevo saldo:</strong> {{ formattedBalance }}</p>
      </div>
    </div>

    <!-- Información adicional -->
    <div class="additional-info">
      <p>{{ $t('subscription.purchase.note') }}</p>
      <p class="small-text">
        {{ $t('subscription.purchase.terms') }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAuth } from 'firebase/auth'
import { useRouter } from 'vue-router'
import useAuth from '@/stores/auth'
import usePaymentStore from '@/stores/payment'

const { t: $t } = useI18n()
const router = useRouter()

// Stores
const authStore = useAuth()
const paymentStore = usePaymentStore()

// Precio de la suscripción
const SUBSCRIPTION_PRICE = 39

// Estado reactivo
const loading = ref(false)
const canSubscribe = ref(false)
const purchaseSuccess = ref(false)
const error = ref<string | null>(null)
const userBalance = ref<number>(0)

// Computed
const formattedBalance = computed(() => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
  }).format(userBalance.value)
})

// Inicializar al montar
onMounted(async () => {
  await checkSubscriptionEligibility()
})

// Verificar elegibilidad
const checkSubscriptionEligibility = async () => {
  const firebaseAuth = getAuth()
  const firebaseUser = firebaseAuth.currentUser

  if (!firebaseUser) {
    console.warn('Usuario no autenticado')
    error.value = 'Usuario no autenticado'
    return
  }

  try {
    // Obtener balance del usuario
    const balance = await paymentStore.fetchBalance(firebaseUser.uid)

    if (balance !== null) {
      userBalance.value = balance
      canSubscribe.value = balance >= SUBSCRIPTION_PRICE

      console.log(`[Subscription] Balance: $${balance}, Required: $${SUBSCRIPTION_PRICE}, Can subscribe: ${canSubscribe.value}`)
    } else {
      error.value = 'No se pudo obtener el saldo'
      canSubscribe.value = false
    }
  } catch (err) {
    console.error('Error verificando elegibilidad:', err)
    error.value = 'Error al verificar saldo'
    canSubscribe.value = false
  }
}

// Manejar compra
const handlePurchase = async () => {
  if (!canSubscribe.value) {
    error.value = `Saldo insuficiente. Necesitas al menos $${SUBSCRIPTION_PRICE} USD`
    return
  }

  loading.value = true
  error.value = null

  try {
    const result = await authStore.buySubscription()

    if (result) {
      purchaseSuccess.value = true

      // Actualizar balance después de la compra
      await checkSubscriptionEligibility()

      // Redirigir al perfil después de 3 segundos
      setTimeout(() => {
        router.push({ name: 'profile' })
      }, 3000)
    } else {
      error.value = 'Error al procesar la compra. Por favor intenta nuevamente.'
    }
  } catch (err) {
    console.error('Error comprando suscripción:', err)
    error.value = 'Error al procesar la compra'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.subscription-purchase {
  max-width: 600px;
  margin: 30px auto;
  padding: 2rem;
  background: var(--color-background-light);
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  color: var(--color-text-primary-light);
  font-family: 'Inter', sans-serif;
  transition: background 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
}

[data-theme="dark"] .subscription-purchase {
  background: var(--color-surface-dark);
  color: var(--color-text-primary-dark);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.5);
}

.header {
  text-align: center;
  margin-bottom: 2rem;

  h1 {
    font-size: 1.7rem;
    font-weight: 600;
    color: inherit;
    margin-bottom: 0.5rem;
  }

  p {
    color: var(--color-text-secondary-light);
    font-size: 1rem;
  }

  [data-theme="dark"] & p {
    color: var(--color-text-secondary-dark);
  }
}

.subscription-info {
  margin-bottom: 2rem;

  .info-card {
    background: linear-gradient(135deg, var(--color-primary-light) 0%, #1b5e20 100%);
    color: white;
    padding: 1.5rem;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
    transition: transform 0.2s ease, box-shadow 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(46, 125, 50, 0.4);
    }

    h3 {
      font-size: 1.3rem;
      margin-bottom: 1rem;
      font-weight: 600;
    }

    .features {
      list-style: none;
      padding: 0;
      margin-bottom: 1.5rem;

      li {
        padding: 0.6rem 0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.15);
        font-size: 0.95rem;

        &:last-child {
          border-bottom: none;
        }
      }
    }

    .price-section {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
      text-align: center;
      padding-top: 0.5rem;

      .original-price {
        font-size: 2rem;
        font-weight: 700;
      }

      .price-label {
        font-size: 0.85rem;
        opacity: 0.9;
      }
    }
  }

  [data-theme="dark"] .info-card {
    background: linear-gradient(135deg, var(--color-primary-dark) 0%, #1b5e20 100%);
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.25);

    &:hover {
      box-shadow: 0 6px 16px rgba(76, 175, 80, 0.35);
    }
  }
}

.current-status {
  background: var(--color-gray-very-light, #f8f9fa);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1.5px solid var(--color-border-light);
  transition: background 0.3s ease, border 0.3s ease;

  .balance-info {
    text-align: center;
    margin-bottom: 1rem;

    h4 {
      font-size: 0.9rem;
      color: var(--color-text-secondary-light);
      margin-bottom: 0.5rem;
      font-weight: 600;
    }

    .balance-amount {
      font-size: 2rem;
      font-weight: 700;
      color: var(--color-primary-light);
    }

    [data-theme="dark"] & h4 {
      color: var(--color-text-secondary-dark);
    }

    [data-theme="dark"] & .balance-amount {
      color: var(--color-green-light);
    }
  }

  .requirement-info {
    text-align: center;

    .success-message {
      color: var(--color-primary-light);
      font-weight: 600;
      margin-bottom: 0.5rem;
      font-size: 0.95rem;
    }

    .warning-message {
      color: #f57c00;
      font-weight: 600;
      margin-bottom: 0.5rem;
      font-size: 0.95rem;
    }

    .requirement {
      font-size: 0.85rem;
      color: var(--color-text-secondary-light);
    }

    [data-theme="dark"] & .success-message {
      color: var(--color-green-light);
    }

    [data-theme="dark"] & .requirement {
      color: var(--color-text-secondary-dark);
    }
  }
}

[data-theme="dark"] .current-status {
  background: var(--color-gray-light);
  border-color: var(--color-border-dark);
}

.purchase-btn {
  width: 100%;
  padding: 1rem;
  margin-top: 0.6rem;
  margin-bottom: 1.5rem;
  background: var(--color-primary-light);
  color: var(--color-button-text-light);
  font-weight: 600;
  font-size: 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.25s ease, transform 0.15s ease, box-shadow 0.25s ease;

  &:hover:not(:disabled) {
    background: var(--color-green-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: var(--color-gray-light);
    color: var(--color-gray-medium);
  }
}

[data-theme="dark"] .purchase-btn {
  background: var(--color-primary-dark);
  color: var(--color-button-text-dark);

  &:hover:not(:disabled) {
    background: var(--color-green-light);
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.95rem;
  font-weight: 500;
  border: 1px solid #ef5350;
}

[data-theme="dark"] .error-message {
  background: rgba(211, 47, 47, 0.15);
  color: #ef5350;
  border-color: #c62828;
}

.success-message {
  background: #e8f5e9;
  color: var(--color-primary-light);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  text-align: center;
  border: 1px solid var(--color-primary-light);

  h3 {
    margin-top: 0;
    margin-bottom: 0.8rem;
    font-weight: 600;
    font-size: 1.3rem;
  }

  .success-details {
    background: var(--color-background-light);
    padding: 1rem;
    border-radius: 8px;
    margin-top: 1rem;
    text-align: left;
    border: 1px solid var(--color-border-light);

    p {
      margin: 0.5rem 0;
      font-size: 0.95rem;
    }
  }
}

[data-theme="dark"] .success-message {
  background: rgba(76, 175, 80, 0.15);
  color: var(--color-green-light);
  border-color: var(--color-green-light);

  .success-details {
    background: var(--color-surface-dark);
    border-color: var(--color-border-dark);
  }
}

.additional-info {
  text-align: center;
  color: var(--color-text-secondary-light);
  font-size: 0.9rem;
  line-height: 1.6;

  p {
    margin: 0.5rem 0;
  }

  .small-text {
    font-size: 0.8rem;
    opacity: 0.8;
    margin-top: 0.8rem;
    font-style: italic;
  }
}

[data-theme="dark"] .additional-info {
  color: var(--color-text-secondary-dark);
}

/* Responsive */
@media (max-width: 768px) {
  .subscription-purchase {
    padding: 1.5rem;
    border-radius: 12px;
    margin: 20px auto;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .header p {
    font-size: 0.9rem;
  }

  .info-card {
    padding: 1.2rem !important;

    h3 {
      font-size: 1.1rem !important;
    }

    .features li {
      font-size: 0.85rem !important;
    }

    .original-price {
      font-size: 1.6rem !important;
    }

    .price-label {
      font-size: 0.8rem !important;
    }
  }

  .balance-amount {
    font-size: 1.6rem !important;
  }

  .current-status {
    padding: 1.2rem;
  }

  .purchase-btn {
    font-size: 0.95rem;
    padding: 0.9rem;
  }
}
</style>
