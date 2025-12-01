<template>
  <div class="payment-result container">
    <div class="card">
      <h1>Pago cancelado ❌</h1>
      <p>El pago fue cancelado o no se completó. Si fue un error intenta nuevamente.</p>
      <router-link to="/" class="btn">Volver al inicio</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "PaymentCancelComponent",
};
</script>

<style scoped>
/* Reusar estilos del success */
.payment-result { padding: 2rem; display:flex; justify-content:center; }
.card { max-width:600px; width:100%; text-align:center; padding:2rem; border-radius:.5rem; box-shadow: 0 6px 18px rgba(0,0,0,0.06); background: #fff; }
.btn { display:inline-block; margin-top:1rem; padding:.6rem 1.2rem; border-radius:.4rem; text-decoration:none; background:#e74c3c; color:white; }
</style>
