<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">Iniciar Sesión</h1>
      <!-- Formulario de login -->
      <form class="login-form">
        <div class="form-group">
          <label for="email">Correo electrónico</label>
          <input type="email" id="email" class="form-input">
        </div>
        <div class="form-group">
          <label for="password">Contraseña</label>
          <input type="password" id="password" class="form-input">
        </div>
        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox"> Recordar usuario
          </label>
          <a href="#" class="forgot-password">Recuperar Contraseña</a>
        </div>
        <button type="submit" class="btn-login">Iniciar Sesión</button>
      </form>
      <p class="register-link">
        ¿No tienes cuenta? <router-link to="/login">Registrate aquí</router-link>
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login'
}
</script>

<style scoped>
.login-container {
  min-height: calc(100vh - 140px); /* Altura mínima menos header y footer */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background-color: #f8f9fa; /* Fondo gris claro */
}

.login-card {
  background: #FFFFFF;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); /* Sombra más pronunciada */
  width: 100%;
  max-width: 400px;
}

.login-title {
  color: #004E61;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem; /* Espacio entre elementos del formulario */
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  color: #004E61;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

/* Efecto cuando el input está enfocado */
.form-input:focus {
  outline: none;
  border-color: #2E7D32; /* Borde verde al enfocar */
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap; /* En móviles se apila */
  gap: 1rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #004E61;
}

.forgot-password {
  color: #2E7D32;
  text-decoration: none;
  font-size: 0.9rem;
}

.forgot-password:hover {
  text-decoration: underline;
}

.btn-login {
  background-color: #2E7D32;
  color: #FFFFFF;
  border: none;
  padding: 12px;
  border-radius: 6px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-login:hover {
  background-color: #266629; /* Verde más oscuro al hover */
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #004E61;
}

.register-link a {
  color: #2E7D32;
  text-decoration: none;
  font-weight: bold;
}

.register-link a:hover {
  text-decoration: underline;
}

/* Responsive para móviles */
@media (max-width: 480px) {
  .login-card {
    padding: 2rem; /* Menos padding en móviles */
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>