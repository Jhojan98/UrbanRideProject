<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.png" alt="Logo" class="form-logo" />
        <h2 class="form-title">Iniciar Sesión</h2>
        <form @submit.prevent="logUser">
            <div class="form-group icon-input">
                <label for="email"><i class="fas fa-envelope left"></i> Correo Electrónico</label>
                <div class="input-wrapper">
                    <input id="email" type="email" v-model="email" placeholder="Correo Electrónico" required />
                </div>
            </div>

            <div class="form-group icon-input">
                <label for="password"> <i class="fas fa-lock"></i> Contraseña</label>
                <div class="input-wrapper">
                    <input id="password" type="password" v-model="password" placeholder="Contraseña" required />
                </div>
            </div>

            <button type="submit" class="form-submit">Entrar</button>
        </form>
        <br>
        <h4>¿No tienes cuenta?
            <router-link class="link-inline" :to="{ name: 'signup' }">
                Regístrate aquí
            </router-link>
            <p>{{feedback}}</p>
        </h4>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
import userAuth from '@/stores/auth';
import { RouterLink, useRouter } from 'vue-router';

const email: Ref<string> = ref('');
const password: Ref<string> = ref('');
const feedback: Ref<string> = ref('');
const store = userAuth();
const router = useRouter();

const logUser = async () => {
    feedback.value = '';
    console.log(email.value, password.value);
    const res = await store.login(email.value, password.value);
    
    if (res.needsVerification) {
        feedback.value = "Por favor verifica tu cuenta primero";
        router.push('/verify-otp');
        return;
    }
    
    if (res.success) {
        feedback.value = "Inicio de sesión exitoso";
        router.push('/reservation');
    } else {
        feedback.value = store.message || "Error en el inicio de sesión";
    }
}


</script>

<style lang="scss" scoped>
@import "@/styles/login.scss";
</style>