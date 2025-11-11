<!-- Src/views/LoginView.vue -->
<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.jpg" alt="Logo" class="form-logo" />
        <h2 class="form-title">Iniciar Sesión</h2>
        <form @submit.prevent="logUser">
            <div class="form-group">
                <label for="email"><i class="fas fa-envelope left"></i> Correo Electrónico</label>
                <input id="email" type="email" v-model="email" placeholder="ejemplo@dominio.com" required />
            </div>
            <div class="form-group">
                <label for="password"><i class="fas fa-lock"> </i>Contraseña</label>
                <input id="password" type="password" v-model="password" placeholder="********" required />
            </div>
                <button type="submit" class="form-submit">Entrar</button>
        </form>
        
        <br>
        <h4>¿No tienes cuenta?
            <router-link class="link-inline" :to="{ name: 'signup' }">
                Regístrate aquí
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
    const password: Ref<string> = ref('');
    const feedback: Ref<string> = ref('');

    const store = userAuth();
    const router = useRouter();

    const logUser = async () => {
        feedback.value = '';
    
    const result = await store.login(email.value, password.value);
    
    if (result && 'needsVerification' in result && result.needsVerification) {
        //  verificación OTP, redirigir
        feedback.value = 'Verificación requerida. Redirigiendo...';
        setTimeout(() => {
            router.push('/verify-otp');
        }, 1500);
    } else if (result && 'success' in result && result.success) {
        // Login exitoso
        feedback.value = 'Login exitoso. Redirigiendo...';
        setTimeout(() => {
            router.push('/reservation');
        }, 1500);
    } else {
        // Error en login
        feedback.value = store.message || 'Error en el login';
    }
    }
</script>

<style lang="scss">
@import "@/styles/login.scss";
</style>