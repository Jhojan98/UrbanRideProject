<template>
  <div>
    <button @click="startCheckout" :disabled="loading">
      {{ loading ? 'Redirigiendo...' : 'Pagar ahora' }}
    </button>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "PaymentCheckoutComponent",
  props: {
    priceId: { type: String, required: true },
    quantity: { type: Number, default: 1 },
    customerEmail: { type: String, default: null },
    userId: { type: String, required: true }
  },
  data() { return { loading: false }; },
  methods: {
    async startCheckout() {
      try {
        this.loading = true;
        // BASE URL configurable vía variable de entorno (ver más abajo)
        const BASE = process.env.VUE_APP_API_URL || "http://localhost:5006";
        const body = {
          priceId: this.priceId,
          quantity: this.quantity,
          customerEmail: this.customerEmail,
          userId: this.userId
        };
        const res = await axios.post(`${BASE}/payments/checkout-session`, body);
        // backend devuelve { sessionId, url }
        if (res.data && res.data.url) {
          window.location.href = res.data.url; // redirige a Stripe Checkout
        } else {
          console.error("Respuesta inesperada del servidor", res.data);
          alert("No se pudo iniciar el checkout. Revisa la consola.");
        }
      } catch (err) {
        console.error("Error creando sesión", err.response?.data || err.message);
        alert("Error creando sesión de pago. Revisa la consola.");
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
