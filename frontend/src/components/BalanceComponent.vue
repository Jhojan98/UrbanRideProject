<template>
  <div class="form-container">
    <div id="balance">
      <h3 :class="balanceClass">{{ amount }}</h3>
      <p>Moneda </p>
      <select name="currency" id="currency">
        <option v-for="c in currencies" :key="c.code" :value="c.code">
          {{ c.code }} - {{ c.name }}
        </option>
      </select>
      <input type="test" v-model="amount" />
      <button>Recargar saldo</button>
    </div>
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
 * Balance status.
 * > 20000   → positive
 * 0‑20000  → low
 * < 0      → negative
 */
const balanceClass = computed(() => {
  if (amount.value >= 20000) return 'balance-positive';
  if (amount.value >= 0) return 'balance-low';
  return 'balance-negative';
});
</script>

<style scoped lang="scss">
@import "@/styles/global.scss";

p{
  margin-left: 0;
  float: left;
}
.balance-positive {
  color: var(--color-balance-positive);
}

.balance-low {
  color: var(--color-balance-low);
}

.balance-negative {
  color: var(--color-balance-negative);
}

#balance select,
#balance select option {
  background-color: var(--select-bg);
  color: var(--select-text);
  border: 1px solid var(--select-border);
}
#balance input {
  background-color: var(--select-bg);
  color: var(--select-text);
  border: 1px solid var(--select-border);
}
</style>
