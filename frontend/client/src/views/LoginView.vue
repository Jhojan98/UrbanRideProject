<!-- Src/views/LoginView.vue -->
<template>
    <div class="page-with-background">
        <div class="form-container">
            <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
            <h2 class="form-title">{{ $t('auth.login.title') }}</h2>
            <form @submit.prevent="logUser">
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope left"></i> {{ $t('auth.login.email') }}</label>
                    <input id="email" type="email" v-model="email" placeholder="" required />
                </div>
                <div class="form-group">
                    <label for="password"><i class="fas fa-lock"> </i>{{ $t('auth.login.password') }}</label>
                    <input id="password" type="password" v-model="password" placeholder="" required />
                </div>
                            <button type="submit" class="form-submit">{{ $t('auth.login.submit') }}</button>
                            <button type="button" class="form-submit google-btn" @click="googleLogin">
                                <i class="fab fa-google"></i> {{ $t('auth.login.google') }}
                            </button>
            </form>

            <br>
            <h4>{{ $t('auth.login.noAccount') }}
                <router-link class="link-inline" :to="{ name: 'signup' }">
                    {{ $t('auth.login.registerHere') }}
                </router-link>
                <p>{{ feedback }}</p>
            </h4>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const email: Ref<string> = ref('');
const password: Ref<string> = ref('');
const feedback: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

const { t: $t } = useI18n();
const logUser = async () => {
    feedback.value = '';

    // Log de depuración para verificar valores antes de enviar
    console.log('=== DATOS DEL FORMULARIO DE LOGIN ===');
    console.log('Email:', email.value);
    console.log('Password:', password.value ? '****** (length: ' + password.value.length + ')' : 'vacío');
    console.log('======================================');

    const result = await store.login(email.value, password.value);

    console.log('Resultado del login:', result);

    if (result && 'needsVerification' in result && result.needsVerification) {
        //  verificación OTP, redirigir
        feedback.value = $t('auth.login.verifyRequired');
        setTimeout(() => {
            router.push('/verify-email');
        }, 1500);
    } else if (result && 'success' in result && result.success) {
        // Login exitoso
        feedback.value = $t('auth.login.success');
        setTimeout(() => {
            router.push({name: "reservation"});
        }, 1500);
    } else {
        // Error en login
        feedback.value = store.message || $t('auth.login.error');
    }
}

const googleLogin = async () => {
    feedback.value = '';
    let result;
    try {
        result = await store.socialLoginWithGoogle();
    } catch (e) {
        console.error('Excepción inesperada en socialLoginWithGoogle:', e);
        result = { success: false };
    }
    if (result && result.success) {
        feedback.value = store.message || $t('auth.login.success');
        setTimeout(() => {
            router.push({ name: 'reservation' });
        }, 1200);
    } else {
        feedback.value = store.message || $t('auth.login.error');
    }
}
</script>

<style lang="scss" scoped>
@import "@/styles/login.scss";
</style>