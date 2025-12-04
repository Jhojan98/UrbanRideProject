<template>
  <div class="payment-methods">
    <div class="header">
      <h1>{{ $t('payments.recharge.title') }}</h1>
      <p>{{ $t('payments.recharge.subtitle') }}</p>
    </div>

    <!-- SECCIÓN DE RECARGA SIMPLE -->
    <div class="recharge-section">
      <div class="currency-selector">
        <label>{{ $t('payments.recharge.currency') }}:</label>
        <button
          v-for="curr in currencies"
          :key="curr"
          @click="selectedCurrency = curr"
          :class="['currency-btn', { 'active': selectedCurrency === curr }]"
        >
          {{ curr }}
        </button>
      </div>
      <div class="recharge-options">
        <div
          v-for="amount in displayedAmounts"
          :key="amount.valueUSD"
          class="recharge-option"
          :class="{ 'selected': selectedAmount === amount.valueUSD }"
          @click="selectAmount(amount.valueUSD)"
        >
          <span class="amount">{{ amount.formatted }}</span>
        </div>
      </div>

      <button
        class="butn-primary recharge-btn"
        @click="handleRecharge"
        :disabled="loading || !selectedAmount"
      >
        <span v-if="loading">{{ $t('payments.recharge.processing') }}</span>
        <span v-else>{{ $t('payments.recharge.rechargeBtn') }}</span>
      </button>

      <div class="recharge-info">
        <p v-if="selectedCurrency === 'COP'" class="conversion-note">
          {{ $t('payments.recharge.estimatedNote', { currency: selectedCurrency }) }}
        </p>
        <p>{{ $t('payments.recharge.security') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAuth } from 'firebase/auth'
import useAuth from '@/stores/auth'
import usePaymentStore from '@/stores/payment'

const { t: $t } = useI18n()

// Estado
const loading = ref(false)
const selectedAmount = ref<number | null>(25000) // Monto por defecto seleccionado en USD

// Selector de moneda
const currencies = ['USD', 'COP'] as const
const selectedCurrency = ref<'USD' | 'COP'>('USD')

// Tasa de conversión estimada (1 USD = 4000 COP)
const USD_TO_COP_RATE = 4000

// Montos de recarga predefinidos en USD (valores reales para Stripe)
const rechargeAmounts = [
  { value: 10000, display: '10.000' },
  { value: 25000, display: '25.000' },
  { value: 50000, display: '50.000' },
  { value: 100000, display: '100.000' }
]

// Montos formateados según la moneda seleccionada
const displayedAmounts = computed(() => {
  return rechargeAmounts.map(amount => {
    const valueUSD = amount.value
    const displayValue = selectedCurrency.value === 'COP'
      ? valueUSD * USD_TO_COP_RATE
      : valueUSD

    const locale = selectedCurrency.value === 'COP' ? 'es-CO' : 'en-US'
    const formatted = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: selectedCurrency.value,
      minimumFractionDigits: selectedCurrency.value === 'COP' ? 0 : 2
    }).format(displayValue)

    return {
      valueUSD: valueUSD,
      formatted: formatted
    }
  })
})

// Mapeo de montos a priceIds de Stripe (usa tus priceIds reales de Stripe)
const priceMap: { [key: number]: string } = {
  10000: 'price_1SZJg7H5d2VSDRWESK3K2s4c',
  25000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz',
  50000: 'price_1SZJgKH5d2VSDRWELkeUDwRx',
  100000: 'price_1SZJgWH5d2VSDRWEdevIO0e5'
}

// Stores
const authStore = useAuth()
const paymentStore = usePaymentStore()

const selectAmount = (amount: number) => {
  selectedAmount.value = amount
}

// Función principal de recarga
const handleRecharge = async () => {
  if (loading.value || !selectedAmount.value) return

  // Obtener usuario firebase actual
  const firebaseAuth = getAuth()
  const firebaseUser = firebaseAuth.currentUser

  if (!firebaseUser) {
    alert($t('payments.recharge.notAuthenticated'))
    return
  }

  loading.value = true

  try {
    const priceId = priceMap[selectedAmount.value]
    if (!priceId) throw new Error($t('payments.recharge.priceNotFound'))

    // Usar el store para crear la sesión
    const session = await paymentStore.createCheckoutSession(
      {
        priceId: priceId,
        quantity: 1,
        customerEmail: firebaseUser.email ?? authStore.tempEmail ?? undefined,
        userId: firebaseUser.uid
      },
      authStore.token ?? undefined
    )

    if (!session || !session.url) {
      const errorMsg = paymentStore.error || $t('payments.recharge.error')
      alert(`Error: ${errorMsg}`)
      return
    }

    // Marcar pago como iniciado
    paymentStore.markPaymentComplete()

    // Redirigir a Stripe
    window.location.href = session.url

  } catch (error: unknown) {
    console.error('Error al recargar:', error)
    const errorMsg = error instanceof Error ? error.message : $t('payments.recharge.tryAgain')
    alert(`${$t('payments.recharge.error')}: ${errorMsg}`)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
/* tu CSS existente (lo puedes mantener igual) */
.payment-methods {
  padding: 20px;
  max-width: 500px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;
  text-align: center;

  h1 {
    margin: 0 0 8px 0;
    color: #333;
    font-size: 28px;
  }

  p {
    margin: 0;
    color: #666;
    font-size: 16px;
  }
}

/* Sección de Recarga */
.recharge-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.currency-selector {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;

  label {
    font-weight: 500;
    color: #333;
    font-size: 14px;
  }

  .currency-btn {
    padding: 0.5rem 1.2rem;
    border: 2px solid #e0e0e0;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s;

    &:hover {
      border-color: var(--color-primary-light);
    }

    &.active {
      background: var(--color-primary-light);
      color: white;
      border-color: var(--color-primary-light);
    }
  }
}

.recharge-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.recharge-option {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;

  &:hover {
    border-color: var(--color-primary-light);
    transform: translateY(-2px);
  }

  &.selected {
    border-color: var(--color-primary-light);
    background: rgba(46, 125, 50, 0.08);
    box-shadow: 0 4px 12px rgba(46, 125, 50, 0.15);
  }
}

.amount {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.recharge-btn {
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;

  &:disabled {
    background: #6c757d;
    cursor: not-allowed;

    &:hover {
      background: #6c757d;
    }
  }
}

.recharge-info {
  text-align: center;

  p {
    color: #666;
    font-size: 14px;
    margin: 0;
  }

  .conversion-note {
    color: #ff9800;
    font-weight: 500;
    margin-bottom: 10px;
    padding: 10px;
    background: #fff3e0;
    border-radius: 8px;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .payment-methods {
    padding: 12px;
  }

  .header {
    margin-bottom: 20px;

    h1 {
      font-size: 22px;
      margin-bottom: 6px;
    }

    p {
      font-size: 14px;
    }
  }

  .recharge-section {
    padding: 20px;
    border-radius: 12px;
  }

  .currency-selector {
    flex-wrap: wrap;
    gap: 0.25rem !important;
    margin-bottom: 15px;

    label {
      width: 100%;
      margin-bottom: 0.25rem;
      font-size: 12px;
    }

    .currency-btn {
      flex: 1;
      min-width: 45px;
      padding: 0.4rem 0.6rem !important;
      font-size: 12px !important;
      border-radius: 6px;
    }
  }

  .recharge-options {
    grid-template-columns: 1fr;
    gap: 10px;
    margin-bottom: 20px;
  }

  .recharge-option {
    padding: 16px;
    border-radius: 10px;

    .amount {
      font-size: 16px;
    }
  }

  .recharge-btn {
    width: 100%;
    padding: 14px;
    font-size: 15px;
    margin-bottom: 15px;
  }

  .recharge-info {
    p {
      font-size: 12px;
      line-height: 1.4;
    }

    .conversion-note {
      margin-bottom: 8px;
      padding: 8px;
      font-size: 12px;
    }
  }
}

@media (max-width: 480px) {
  .payment-methods {
    padding: 8px;
  }

  .header {
    margin-bottom: 16px;

    h1 {
      font-size: 18px;
    }

    p {
      font-size: 12px;
    }
  }

  .recharge-section {
    padding: 16px;
    border-radius: 8px;
  }

  .currency-selector {
    gap: 0 !important;

    label {
      font-size: 11px;
      margin-bottom: 4px;
    }

    .currency-btn {
      min-width: 40px;
      padding: 0.3rem 0.4rem !important;
      font-size: 10px !important;
      flex: 1 1 auto;
    }
  }

  .recharge-options {
    gap: 8px;
  }

  .recharge-option {
    padding: 12px 8px;
    min-height: 70px;
    display: flex;
    align-items: center;
    justify-content: center;

    .amount {
      font-size: 14px;
    }
  }

  .recharge-btn {
    padding: 12px;
    font-size: 13px;
  }

  .recharge-info p {
    font-size: 11px;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .payment-methods {
    padding: 16px;
  }

  .recharge-section {
    padding: 20px;
  }

  .recharge-options {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header h1 {
    font-size: 24px;
  }
}
</style>
