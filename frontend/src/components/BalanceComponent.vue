<template>
  <div id="balance">
    <!-- El color del monto cambia según su valor -->
    <h3 :class="balanceClass">{{ amount }}</h3>
    <select name="currency" id="currency">
      <option v-for="c in currencies" :key="c.code" :value="c.code">
        {{ c.code }} - {{ c.name }}
      </option>
    </select>
    <input type="test" v-model="amount" />
  </div>
</template>

<script lang="ts" setup>
import { ref, Ref, computed } from 'vue'

const amount: Ref<number> = ref(5000);

const currencies = [
  { code: 'USD', name: 'Dólar' },
  { code: 'EUR', name: 'Euro' },
  { code: 'COP', name: 'Peso colombiano' },
]

/**
 * Clase CSS que indica el estado del saldo.
 * > 20000   → positivo
 * 0‑20000  → bajo
 * < 0      → negativo
 */
const balanceClass = computed(() => {
  if (amount.value >= 20000) return 'balance-positive';
  if (amount.value >= 0) return 'balance-low';
  return 'balance-negative';
});
</script>

<style scoped>
/* Utiliza las variables de color definidas en global.scss */
.balance-positive { color: var(--color-balance-positive); }
.balance-low { color: var(--color-balance-low); }
.balance-negative { color: var(--color-balance-negative); }
</style>
