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
import useAuth from '@/stores/auth' // Ajusta la ruta si tu store está en otra carpeta

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

// store de auth (Pinia)
const authStore = useAuth()

const selectAmount = (amount: number) => {
  selectedAmount.value = amount
}

// Función principal de recarga
const handleRecharge = async () => {
  if (loading.value || !selectedAmount.value) return

  // obtener usuario firebase actual
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

    const request = {
      priceId: priceId,
      quantity: 1,
      customerEmail: firebaseUser.email ?? authStore.tempEmail ?? undefined,
      userId: firebaseUser.uid
    }

    // Construir headers y añadir Authorization si hay token
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }

    // URL al servicio de pagos (mantuve localhost:8007 como en tu componente original).
    // Si usas gateway o variable de entorno, cámbialo aquí.
    const response = await fetch('http://localhost:8007/payments/checkout-session', {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
    })

    if (!response.ok) {
      // intentar leer body JSON con detalle del error
      let errBody: any = null
      try { errBody = await response.json() } catch(e) { /* no JSON */ }
      console.error('Error HTTP del backend:', response.status, response.statusText, errBody)
      const msg = errBody?.error || `Error del servidor: ${response.status}`
      throw new Error(msg)
    }
    
    const data = await response.json()
    if (!data || !data.url) {
      console.error('Respuesta inesperada del backend:', data)
      throw new Error('Respuesta inválida del servidor al crear la sesión de pago')
    }

    // Redirigir directamente a Stripe
    window.location.href = data.url

  } catch (error: any) {
    console.error('Error al recargar:', error)
    // Mostrar mensaje más informativo si existe
    alert(`Error al iniciar el pago: ${error?.message || 'Por favor intenta nuevamente.'}`)
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
