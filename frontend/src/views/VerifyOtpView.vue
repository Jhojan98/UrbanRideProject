<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.jpg" alt="Logo" class="form-logo" />
        <h2 class="form-title">Verificación OTP</h2>
        <form @submit.prevent="verifyOtp">
            <div class="form-group">
                <label for="email">Correo Electrónico</label>
                <input 
                    id="email" 
                    type="email" 
                    v-model="email" 
                    placeholder="ejemplo@dominio.com" 
                    required 
                />
            </div>
            <div class="form-group">
                <label for="otp">Código OTP</label>
                <input 
                    id="otp" 
                    type="text" 
                    v-model="otp" 
                    placeholder="Ingresa el código de 6 dígitos" 
                    required 
                    maxlength="6"
                />
            </div>
            <button type="submit" class="form-submit">Verificar Código</button>
        </form>
        
        <div class="otp-actions">
            <button @click="resendOtp" class="btn-resend">Reenviar OTP</button>
        </div>
        
        <br>
        <h4>
            ¿Volver al 
            <router-link class="link-inline" :to="{ name: 'login' }">
                Inicio de Sesión
            </router-link>
            <p>{{ feedback }}</p>
        </h4>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const email: Ref<string> = ref('');
const otp: Ref<string> = ref('');
const feedback: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

const verifyOtp = async () => {
    const otpNumber = parseInt(otp.value, 10);
    
    if (isNaN(otpNumber)) {
        feedback.value = 'El OTP debe ser un número válido';
        return;
    }

    // Usar el email almacenado en el store si está disponible
    const emailToVerify = email.value || store.tempEmail;
    
    if (!emailToVerify) {
        feedback.value = 'Por favor, ingresa tu correo electrónico';
        return;
    }

    const res = await store.verifyOtp(emailToVerify, otpNumber);
    
    if (res) {
        feedback.value = 'Verificación exitosa';
        // Redirigir al usuario a la página de reservas
        setTimeout(() => {
            router.push('/reservation');
        }, 1500);
    } else {
        feedback.value = store.message || 'Error al verificar el OTP';
    }
};

const resendOtp = async () => {
    const res = await store.generateOtp(email.value);
    
    if (res) {
        feedback.value = 'Nuevo código OTP enviado a tu correo';
    } else {
        feedback.value = store.message || 'Error al reenviar el OTP';
    }
};
</script>

<style lang="scss">
@import "@/styles/forms.scss";

.otp-actions {
    margin-top: 1rem;
    text-align: center;
}

.btn-resend {
    background: transparent;
    color: var(--color-primary-light);
    border: 1px solid var(--color-primary-light);
    padding: 0.5rem 1rem;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.3s ease;
}

.btn-resend:hover {
    background: var(--color-primary-light);
    color: var(--color-button-text-light);
}

[data-theme="dark"] .btn-resend {
    color: var(--color-primary-dark);
    border-color: var(--color-primary-dark);
}

[data-theme="dark"] .btn-resend:hover {
    background: var(--color-primary-dark);
    color: var(--color-button-text-dark);
}
</style>