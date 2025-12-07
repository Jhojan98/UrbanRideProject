<template>
  <div class="payment-methods">
    <div class="header">
      <h1>{{ $t('payments.recharge.title') }}</h1>
      <p>{{ $t('payments.recharge.subtitle') }}</p>
    </div>

    <!-- SECCIÓN DE RECARGA SIMPLE -->
    <div class="recharge-section">
      <div class="currency-selector">
        <label for="currency-select">{{ $t('payments.recharge.currency') }}:</label>
        <select
          id="currency-select"
          v-model="selectedCurrency"
          class="currency-select"
        >
          <option v-for="curr in currencies" :key="curr" :value="curr">
            {{ curr }} - {{ getCurrencyName(curr) }}
          </option>
        </select>
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
        <p v-if="selectedCurrency !== 'USD'" class="conversion-note">
          {{ $t('payments.recharge.estimatedNote', { currency: selectedCurrency }) }}
        </p>
        <p>{{ $t('payments.recharge.security') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAuth } from 'firebase/auth'
import useAuth from '@/stores/auth'
import usePaymentStore from '@/stores/payment'
import { fetchExchangeRate } from '@/services/currencyExchange'

const { t: $t } = useI18n()

// Estado
const loading = ref(false)
const selectedAmount = ref<number | null>(null) // Se calculará automáticamente al montar

// Selector de moneda
const currencies = ['USD', 'COP', 'EUR'] as const
const selectedCurrency = ref<'USD' | 'COP' | 'EUR'>('COP')

// Tasas de conversión dinámicas
const exchangeRates = ref<{ COP: number; EUR: number }>({
  COP: 4000, // Valor por defecto USD a COP
  EUR: 0.92  // Valor por defecto USD a EUR
})

// Montos de recarga predefinidos en COP
const rechargeAmountsCOP = [
  10000,   // $10.000 COP
  25000,   // $25.000 COP
  50000,   // $50.000 COP
  100000   // $100.000 COP
]

// Montos formateados según la moneda seleccionada con conversión dinámica
const displayedAmounts = computed(() => {
  return rechargeAmountsCOP.map(amountCOP => {
    let displayValue: number
    let valueUSD: number

    if (selectedCurrency.value === 'COP') {
      // Mostrar el valor en COP directamente
      displayValue = amountCOP
      // Calcular el equivalente en centavos de USD para Stripe
      valueUSD = Math.round((amountCOP / exchangeRates.value.COP) * 100)
    } else if (selectedCurrency.value === 'EUR') {
      // Convertir de COP a EUR para mostrar
      const amountUSD = amountCOP / exchangeRates.value.COP
      displayValue = amountUSD * exchangeRates.value.EUR
      valueUSD = Math.round(amountUSD * 100)
    } else {
      // USD: Convertir de COP a USD para mostrar
      valueUSD = Math.round((amountCOP / exchangeRates.value.COP) * 100)
      displayValue = valueUSD / 100 // Convertir de centavos a dólares
    }

    const locale = selectedCurrency.value === 'COP' ? 'es-CO' :
                   selectedCurrency.value === 'EUR' ? 'es-ES' : 'en-US'
    const formatted = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: selectedCurrency.value,
      minimumFractionDigits: selectedCurrency.value === 'COP' ? 0 : 2
    }).format(displayValue)

    return {
      valueUSD: valueUSD, // Valor en centavos de USD para Stripe
      valueCOP: amountCOP, // Valor original en COP
      formatted: formatted // Texto formateado para mostrar
    }
  })
})

// Actualizar las tasas de cambio cuando se cambia de moneda
const updateExchangeRate = async () => {
  try {
    if (selectedCurrency.value === 'COP') {
      // Obtener la tasa de cambio real: 1 USD a COP
      const rateCOP = await fetchExchangeRate('USD', 'COP', 1)
      exchangeRates.value.COP = rateCOP
      console.log(`Tasa de cambio actualizada: 1 USD = ${rateCOP} COP`)
    } else if (selectedCurrency.value === 'EUR') {
      // Obtener la tasa de cambio real: 1 USD a EUR
      const rateEUR = await fetchExchangeRate('USD', 'EUR', 1)
      exchangeRates.value.EUR = rateEUR
      console.log(`Tasa de cambio actualizada: 1 USD = ${rateEUR} EUR`)
    }
  } catch (error) {
    console.error('Error obteniendo tasa de cambio, usando valor por defecto:', error)
    // Mantener valores por defecto en caso de error
  }
}

// Observar cambios en la moneda seleccionada
watch(selectedCurrency, () => {
  updateExchangeRate()
})

// Función helper para obtener el nombre de la moneda
const getCurrencyName = (currency: string): string => {
  const names: { [key: string]: string } = {
    USD: 'Dólar estadounidense',
    COP: 'Peso colombiano',
    EUR: 'Euro'
  }
  return names[currency] || currency
}

// Cargar tasas iniciales al montar el componente
onMounted(async () => {
  // Cargar ambas tasas al inicio
  try {
    const [rateCOP, rateEUR] = await Promise.all([
      fetchExchangeRate('USD', 'COP', 1).catch(() => 4000),
      fetchExchangeRate('USD', 'EUR', 1).catch(() => 0.92)
    ])
    exchangeRates.value.COP = rateCOP
    exchangeRates.value.EUR = rateEUR
    console.log('Tasas de cambio cargadas:', exchangeRates.value)
  } catch (error) {
    console.error('Error cargando tasas iniciales:', error)
  }

  // Seleccionar el segundo monto por defecto (25.000 COP)
  if (displayedAmounts.value.length > 1) {
    selectedAmount.value = displayedAmounts.value[1].valueUSD
  }
})

// Mapeo de montos COP a priceIds de Stripe
// Estos priceIds deben corresponder a los valores aproximados en USD
const priceMapCOP: { [key: number]: string } = {
  10000: 'price_1SZJg7H5d2VSDRWESK3K2s4c',   // ~$2.50 USD
  25000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz',   // ~$6.25 USD
  50000: 'price_1SZJgKH5d2VSDRWELkeUDwRx',   // ~$12.50 USD
  100000: 'price_1SZJgWH5d2VSDRWEdevIO0e5'   // ~$25 USD
}

// Función helper para obtener el priceId basado en el monto en COP
const getPriceId = (amountCOP: number): string | undefined => {
  return priceMapCOP[amountCOP]
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
    // Obtener el objeto completo del monto seleccionado
    const selectedAmountObj = displayedAmounts.value.find(a => a.valueUSD === selectedAmount.value)
    if (!selectedAmountObj) throw new Error($t('payments.recharge.priceNotFound'))

    const priceId = getPriceId(selectedAmountObj.valueCOP)
    if (!priceId) throw new Error($t('payments.recharge.priceNotFound'))

    console.log(`Recargando: ${selectedAmountObj.formatted} (${selectedAmountObj.valueUSD} centavos USD, ${selectedAmountObj.valueCOP} COP)`)

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
  gap: 0.75rem;

  label {
    font-weight: 500;
    color: #333;
    font-size: 14px;
  }

  .currency-select {
    padding: 0.6rem 1rem;
    border: 2px solid #e0e0e0;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    color: #333;
    transition: all 0.3s;
    min-width: 200px;

    &:hover {
      border-color: var(--color-primary-light);
    }

    &:focus {
      outline: none;
      border-color: var(--color-primary-light);
      box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
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
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem !important;
    margin-bottom: 15px;

    label {
      font-size: 12px;
    }

    .currency-select {
      width: 100%;
      min-width: auto;
      padding: 0.5rem 0.8rem !important;
      font-size: 13px !important;
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
    gap: 0.4rem !important;

    label {
      font-size: 11px;
    }

    .currency-select {
      padding: 0.4rem 0.6rem !important;
      font-size: 12px !important;
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
