<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal">
      <h2>Ingresar</h2>
      <p class="muted">Introduce tus credenciales. Usa <strong>admin</strong> como usuario para acceder al panel administrativo.</p>

      <form @submit.prevent="submit">
        <label>Usuario o email
          <input v-model="username" placeholder="ej. juan@ejemplo.com o admin" />
        </label>

        <label>Contraseña
          <input v-model="password" type="password" placeholder="*******" />
        </label>

        <div class="row">
          <label class="remember"><input type="checkbox" v-model="remember" /> Recordarme</label>
          <button class="btn" type="submit">Entrar</button>
        </div>

        <div v-if="error" class="error">{{ error }}</div>
      </form>

      <div class="example">
        <strong>Ejemplo:</strong> admin / cualquier contraseña -> redirige a ADMIN. Cualquier otro usuario -> usuario normal.
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      remember: true,
      error: ''
    }
  },
  methods: {
    submit() {
      this.error = ''
      if (!this.username || !this.password) {
        this.error = 'Por favor completa usuario y contraseña.'
        return
      }

      const userLower = this.username.trim().toLowerCase()
      let role = 'user'
      if (userLower === 'admin' || userLower === 'admin@empresa.com') role = 'admin'
      const name = this.username.includes('@') ? this.username.split('@')[0] : this.username
      this.$emit('login', { name, role })
    }
  }
}
</script>

<style scoped>
.modal-backdrop {
  position:fixed; inset:0; display:flex; align-items:center; justify-content:center;
  background: linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.25));
  padding:20px;
}
.modal {
  width:100%; max-width:520px; background:white; border-radius:12px; padding:20px; box-shadow:0 12px 30px rgba(2,22,16,0.12);
}
.modal h2 { margin:0 0 8px 0; }
.muted { color:#666; margin-top:0; margin-bottom:12px; font-size:13px; }

form label { display:block; margin-bottom:10px; font-size:14px; color:#333; }
form input { width:100%; padding:10px 12px; border-radius:8px; border:1px solid #e6e6e6; margin-top:6px; }

.row { display:flex; justify-content:space-between; align-items:center; margin-top:12px; gap:12px; }
.remember { font-size:13px; color:#666; display:flex; align-items:center; gap:8px; }

.error { margin-top:10px; color:#b00020; font-weight:600; }

.example { margin-top:14px; font-size:13px; color:#444; background:#f7f7f7; padding:8px; border-radius:8px; }
</style>