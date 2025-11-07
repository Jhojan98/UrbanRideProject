<!-- <template>
  <div class="form-container">
    <img src="@/assets/ECORIDE.jpg" alt="Logo" class="form-logo" />
    <h2 class="form-title">Verificación de Código</h2>
    
    <div class="verification-message">
      <p>Hemos enviado un código de verificación a tu correo electrónico.</p>
      <p class="email-highlight">{{ email }}</p>
      <p>Por favor, ingresa el código de 6 dígitos para continuar.</p>
    </div>

    <form @submit.prevent="verifyOtp">
      <div class="form-group">
        <label for="otp">Código de Verificación</label>
        <input 
          id="otp" 
          type="text" 
          v-model="otp" 
          placeholder="123456" 
          required 
          maxlength="6"
          pattern="[0-9]*"
          inputmode="numeric"
          :class="{ 'error': errorMessage }"
        />
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </div>

      <button 
        type="submit" 
        class="form-submit"
        :disabled="isLoading"
      >
        <span v-if="!isLoading">Verificar Código</span>
        <span v-else>Verificando...</span>
      </button>
    </form>

    <div class="resend-section">
      <p>¿No recibiste el código?</p>
      <button 
        @click="resendOtp" 
        class="resend-button"
        :disabled="isResending"
      >
        <span v-if="!isResending">Reenviar código</span>
        <span v-else>Reenviando...</span>
      </button>
      <p class="countdown" v-if="countdown > 0">
        Puedes reenviar en {{ countdown }} segundos
      </p>
    </div>

    <br>
    <h4>
      ¿Volver al inicio?
      <router-link class="link-inline" :to="{ name: 'home' }">
        Ir al inicio
      </router-link>
    </h4>
  </div>
</template -->
