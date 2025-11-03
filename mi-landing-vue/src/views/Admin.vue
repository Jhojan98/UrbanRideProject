<template>
  <div>
    <h2>Panel Administrativo</h2>
    <p>Bienvenido, <strong>{{ userName }}</strong> — esta vista simula el panel de administración.</p>

    <div class="grid">
      <div class="tile">Estaciones: <strong>24</strong></div>
      <div class="tile">Bicis disponibles: <strong>312</strong></div>
      <div class="tile">Tickets abiertos: <strong>3</strong></div>
    </div>

    <div class="box">
      <h3>Acciones rápidas</h3>
      <button class="btn" @click="simulateAlert">Simular alerta de telemetría</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      auth: JSON.parse(localStorage.getItem('auth')) || {}
    }
  },
  computed: {
    userName() { return this.auth?.name || 'Admin' }
  },
  methods: {
    simulateAlert() {
      alert('Alerta simulada: lockStatus=ERROR en bici #123')
    }
  }
}
</script>

<style scoped>
.grid { display:flex; gap:12px; margin-top:14px; }
.tile { flex:1; padding:14px; background:#fff; border-radius:8px; box-shadow:0 8px 18px rgba(1,40,30,0.04) }
.box { margin-top:18px; padding:14px; background:#fdfdfd; border-radius:8px; }
</style>