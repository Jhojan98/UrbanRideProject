<template>
  <div class="payment-methods">
    <div class="header">
      <h1>Recargar Saldo</h1>
      <p>Elige el monto que deseas recargar en tu cuenta</p>
    </div>

    <!-- SECCIÓN DE RECARGA SIMPLE -->
    <div class="recharge-section">
      <div class="recharge-options">
        <div
          v-for="amount in rechargeAmounts"
          :key="amount.value"
          class="recharge-option"
          :class="{ 'selected': selectedAmount === amount.value }"
          @click="selectAmount(amount.value)"
        >
          <span class="amount">${{ amount.display }}</span>
        </div>
      </div>

      <button
        class="butn-primary recharge-btn"
        @click="handleRecharge"
        :disabled="loading || !selectedAmount"
      >
        <span v-if="loading">Procesando...</span>
        <span v-else>Recargar Saldo</span>
      </button>

      <div class="recharge-info">
        <p>🔒 Serás redirigido a Stripe para completar el pago de forma segura</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getAuth } from 'firebase/auth'
import useAuth from '@/stores/auth'
import usePaymentStore from '@/stores/payment'

// Estado
const loading = ref(false)
const selectedAmount = ref<number | null>(25000) // Monto por defecto seleccionado

// Montos de recarga predefinidos (sin bonificación)
const rechargeAmounts = [
  { value: 10000, display: '10.000' },
  { value: 25000, display: '25.000' },
  { value: 50000, display: '50.000' },
  { value: 100000, display: '100.000' }
]

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
    alert('No se encontró usuario autenticado. Vuelve a iniciar sesión.')
    return
  }

  loading.value = true

  try {
    const priceId = priceMap[selectedAmount.value]
    if (!priceId) throw new Error('PriceId no configurado para el monto seleccionado')

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
      const errorMsg = paymentStore.error || 'Error al crear la sesión de pago'
      alert(`Error: ${errorMsg}`)
      return
    }

    // Marcar pago como iniciado
    paymentStore.markPaymentComplete()

    // Redirigir a Stripe
    window.location.href = session.url

  } catch (error: unknown) {
    console.error('Error al recargar:', error)
    const errorMsg = error instanceof Error ? error.message : 'Por favor intenta nuevamente.'
    alert(`Error al iniciar el pago: ${errorMsg}`)
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
