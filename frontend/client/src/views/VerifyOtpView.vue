<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.jpg" alt="Logo" class="form-logo" />
        <h2 class="form-title">Verificación OTP</h2>
        
        <!-- Mostramos el email que viene del store -->
        <div class="email-info">
            <p>Código enviado a: <strong>{{ email }}</strong></p>
        </div>
        
        <form @submit.prevent="verifyOtp">
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
            <router-link class="link-inline" :to="{ name: 'login' }" @click="clearEmail">
                Inicio de Sesión
            </router-link>
            <p>{{ feedback }}</p>
        </h4>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const otp: Ref<string> = ref('');
const feedback: Ref<string> = ref('');
const email: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

onMounted(() => {
    // Obtenemos el email del store al cargar el componente
    if (store.tempEmail) {
        email.value = store.tempEmail;
    } else {
        feedback.value = 'No se encontró email para verificar. Redirigiendo al login...';
        setTimeout(() => {
            router.push('/login');
        }, 2000);
    }
});

const verifyOtp = async () => {
    if (!email.value) {
        feedback.value = 'No hay email disponible para verificar';
        return;
    }

    const otpNumber = parseInt(otp.value, 10);
    
    if (isNaN(otpNumber)) {
        feedback.value = 'El OTP debe ser un número válido de 6 dígitos';
        return;
    }

    if (otp.value.length !== 6) {
        feedback.value = 'El OTP debe tener exactamente 6 dígitos';
        return;
    }

    const res = await store.verifyOtp(email.value, otpNumber);
    
    if (res) {
        feedback.value = '¡Verificación exitosa! Redirigiendo...';
        // Redirigir al usuario después de verificación exitosa
        setTimeout(() => {
            router.push('/reservation');
        }, 1500);
    } else {
        feedback.value = store.message || 'Error al verificar el OTP';
    }
};

const resendOtp = async () => {
    if (!email.value) {
        feedback.value = 'No hay email disponible para reenviar OTP';
        return;
    }

    const res = await store.generateOtp(email.value);
    
    if (res) {
        feedback.value = 'Nuevo código OTP enviado a tu correo';
    } else {
        feedback.value = store.message || 'Error al reenviar el OTP';
    }
};

const clearEmail = () => {
    store.tempEmail = null;
    store.pendingVerification = false;
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

.email-info {
    text-align: center;
    margin-bottom: 1rem;
    padding: 0.5rem;
    background: rgba(46, 125, 50, 0.1);
    border-radius: 6px;
}

.email-info p {
    margin: 0;
    color: var(--color-text-secondary-light);
}

[data-theme="dark"] .email-info {
    background: rgba(46, 125, 50, 0.2);
}

[data-theme="dark"] .email-info p {
    color: var(--color-text-secondary-dark);
}
</style>