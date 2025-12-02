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

// Estado
const loading = ref(false)
const selectedAmount = ref<number | null>(25000) // Monto por defecto seleccionado

// Datos del usuario (en producción esto vendría de tu store de auth)
const userData = ref({
  email: 'sean.bolivar.2002@gmail.com', // Reemplaza con email real
  userId: 'seanb14422' // Reemplaza con ID real
})

// Montos de recarga predefinidos (sin bonificación)
const rechargeAmounts = [
  { value: 10000, display: '10.000' },
  { value: 25000, display: '25.000' },
  { value: 50000, display: '50.000' },
  { value: 100000, display: '100.000' }
]

// Mapeo de montos a priceIds de Stripe (usa tus priceIds reales de Stripe)
const priceMap: { [key: number]: string } = {
  10000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz',
  25000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz', 
  50000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz',
  100000: 'price_1SXRt6H5d2VSDRWEyeJzMcCz'
}

// Seleccionar monto
const selectAmount = (amount: number) => {
  selectedAmount.value = amount
}

// Función principal de recarga
const handleRecharge = async () => {
  if (loading.value || !selectedAmount.value) return
  
  loading.value = true

  try {
    const priceId = priceMap[selectedAmount.value]
    
    const request = {
      priceId: priceId,
      quantity: 1,
      customerEmail: userData.value.email,
      userId: userData.value.userId
    }

    // Llamar al backend Java para crear sesión de Stripe
    const response = await fetch('http://localhost:8007/payments/checkout-session', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status}`)
    }

    const data = await response.json()
    
    // Redirigir directamente a Stripe
    window.location.href = data.url
    
  } catch (error) {
    console.error('Error al recargar:', error)
    alert('Error al iniciar el pago. Por favor intenta nuevamente.')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
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

/* Modo oscuro */
:global(html[data-theme="dark"]) {
  .header {
    h1 {
      color: var(--color-text-primary-dark);
    }
    
    p {
      color: var(--color-text-secondary-dark);
    }
  }
  
  .recharge-section {
    background: var(--color-surface-dark);
  }
  
  .recharge-option {
    background: var(--color-background-dark);
    border-color: var(--color-border-dark);
    
    .amount {
      color: var(--color-text-primary-dark);
    }
    
    &:hover {
      border-color: var(--color-primary-light);
    }
    
    &.selected {
      border-color: var(--color-primary-light);
      background: rgba(46, 125, 50, 0.15);
    }
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