<template>
  <div>
    <header class="header">
      <div class="brand" @click="goHome">
        <div class="logo">🚲</div>
        <div>
          <h1>EcoBici IoT</h1>
          <p class="tag">Movilidad sostenible con bicis inteligentes</p>
        </div>
      </div>

      <div class="actions">
        <div v-if="auth && auth.logged" class="user-info">
          <span class="welcome">Hola, <strong>{{ auth.name }}</strong></span>
          <button class="btn small" @click="logout">Cerrar sesión</button>
        </div>
        <button v-else class="btn" @click="openLogin">Ingresar</button>
      </div>
    </header>

    <main>
      <router-view />
    </main>

    <LoginModal v-if="loginOpen" @close="loginOpen = false" @login="onLogin" />

    <footer class="footer">
      <small>© {{ new Date().getFullYear() }} EcoBici IoT — Proyecto académico</small>
    </footer>
  </div>
</template>

<script>
import LoginModal from './components/LoginModal.vue'

export default {
  components: { LoginModal },
  data() {
    return {
      loginOpen: false,
      auth: JSON.parse(localStorage.getItem('auth')) || null,
    }
  },
  methods: {
    openLogin() { this.loginOpen = true },
    onLogin(payload) {
      this.auth = { logged: true, name: payload.name, role: payload.role }
      localStorage.setItem('auth', JSON.stringify(this.auth))
      this.loginOpen = false
      if (payload.role === 'admin') this.$router.push({ name: 'Admin' })
      else this.$router.push({ name: 'User' })
    },
    logout() {
      localStorage.removeItem('auth')
      this.auth = null
      this.$router.push({ name: 'Home' })
    },
    goHome() { this.$router.push({ name: 'Home' }) }
  }
}
</script>

<style>
:root {
  --primary: #1f8a70;
  --dark: #0b3b2e;
  --muted: #666;
  --accent: #f3a712;
  --glass: rgba(255,255,255,0.95);
}

* { box-sizing: border-box; }
body { margin: 0; font-family: Inter, system-ui, Arial, sans-serif; color: #222; background: linear-gradient(180deg,#f7fbfb,#e9f6f4); min-height: 100vh; }

.header {
  display:flex; justify-content:space-between; align-items:center;
  padding:16px 28px; background: var(--glass); backdrop-filter: blur(6px);
  border-bottom: 1px solid rgba(0,0,0,0.04);
}
.brand { display:flex; align-items:center; gap:12px; cursor:pointer; }
.logo { width:56px; height:56px; display:flex; align-items:center; justify-content:center; font-size:28px; border-radius:8px; background:linear-gradient(180deg,#eafff4,#d8fff0) }
.tag { margin:0; font-size:13px; color:var(--muted); }
h1 { margin:0; font-size:20px; color:var(--dark); }

.actions { display:flex; align-items:center; gap:12px; }

.btn {
  background: var(--primary); color:white; border:none; padding:10px 14px; border-radius:8px; cursor:pointer;
  box-shadow: 0 4px 10px rgba(31,138,112,0.12);
}
.btn:hover { transform: translateY(-1px); }
.btn.small { padding:6px 10px; background:#efefef; color:#333; border:1px solid #ddd; }

main { padding:28px; max-width:1100px; margin:20px auto; }

.footer { text-align:center; padding:18px 8px; color:var(--muted); margin-top:40px; }

@media (max-width:720px){
  .header { padding:12px;}
  main { padding:16px; }
  h1 { font-size:18px;}
}
</style>