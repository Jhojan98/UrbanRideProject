<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.png" alt="Logo" class="form-logo" />
        <h2 class="form-title">Crear Cuenta</h2>
        <form @submit.prevent="onSubmit">
            <div class="form-group">
                <label for="name">Nombre</label>
                <input id="name" type="text" v-model="name" placeholder="Juan Pérez" required />
            </div>
            <div class="form-group">
                <label for="email">Correo Electrónico</label>
                <input id="email" type="email" v-model="email" placeholder="ejemplo@dominio.com" required />
            </div>
            <div class="form-group">
                <label for="password">Contraseña</label>
                <input id="password" type="password" v-model="password" placeholder="********" required />
            </div>
            <button type="submit" class="form-submit">Registrarse</button>
            <h3>{{ feedback }}</h3>
        </form>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const name:Ref<string> = ref('');
const email: Ref<string> = ref('');
const password:Ref<string> = ref('');
const feedback: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

const onSubmit = async () => {
    feedback.value = '';
    const res = await store.register(name.value, email.value, password.value);
    
    if (res) {
        feedback.value = "Registro exitoso. Te enviaremos un código de verificación.";
        // Redirigir a la página de verificación OTP
        router.push('/verify-otp');
    } else {
        feedback.value = store.message || "Error en el registro";
    }
}
</script>

<style lang="scss">
@import "@/styles/login.scss";
</style>