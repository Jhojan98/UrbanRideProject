<template>
  <div>
    <h2>Área de Usuario</h2>
    <p>Bienvenido, <strong>{{ userName }}</strong></p>

    <div class="box">
      <h3>Datos de sesión</h3>
      <p><strong>Usuario:</strong> {{ userName }}</p>
      <p><strong>Rol:</strong> {{ role }}</p>
    </div>

    <div class="box" style="margin-top:12px;">
      <h3>Acciones</h3>
      <button class="btn" @click="mockReserve">Reservar una bici (demo)</button>
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
    userName() { return this.auth?.name || 'Usuario' },
    role() { return this.auth?.role || 'user' }
  },
  methods: {
    mockReserve() {
      alert('Reserva simulada: bici #45 reservada por 10 minutos.')
    }
  }
}
</script>

<style scoped>
.box { padding:12px; background:#fff; border-radius:8px; box-shadow:0 6px 14px rgba(0,0,0,0.04) }
</style>