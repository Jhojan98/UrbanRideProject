<template>
  <div class="payment-result container">
    <div class="card">
      <h1>Pago realizado ✅</h1>
      <p>Gracias por tu compra. Tu pago fue procesado correctamente.</p>

      <div class="details" v-if="sessionId || uid">
        <p><strong>Session ID:</strong> {{ sessionId }}</p>
        <p v-if="uid"><strong>Usuario:</strong> {{ uid }}</p>
      </div>

      <router-link to="/" class="btn">Volver al inicio</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "PaymentSuccesComponent",
  data() {
    return {
      sessionId: null,
      uid: null,
    };
  },
  created() {
    // Leer query params que Stripe agrega: ?session_id=...&uid=...
    this.sessionId = this.$route.query.session_id || null;
    this.uid = this.$route.query.uid || null;
    // Opcional: podrías llamar a tu backend para verificar la sesión (si implementas un endpoint)
  },
};
</script>

<style scoped>
.payment-result { padding: 2rem; display:flex; justify-content:center; }
.card { max-width:600px; width:100%; text-align:center; padding:2rem; border-radius:.5rem; box-shadow: 0 6px 18px rgba(0,0,0,0.06); background: #fff; }
.btn { display:inline-block; margin-top:1rem; padding:.6rem 1.2rem; border-radius:.4rem; text-decoration:none; background:#0074d4; color:white; }
.details { text-align:left; margin-top:1rem; background:#f7f7f7; padding:0.8rem; border-radius:0.4rem;}
</style>
