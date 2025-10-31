<template>
    <div class="form-container">
        <h2 class="form-title">Iniciar Sesión</h2>
        <form @submit.prevent="onSubmit">
            <div class="form-group">
                <label for="email">Correo Electrónico</label>
                <input id="email" type="email" v-model="email" placeholder="ejemplo@dominio.com" required />
            </div>
            <div class="form-group">
                <label for="password">Contraseña</label>
                <input id="password" type="password" v-model="password" placeholder="********" required />
            </div>
            <button type="submit" class="form-submit">Entrar</button>
        </form>
        <h3>¿No tienes cuenta con nosotros?</h3>
        <router-link :to="{name:'signup'}">Regístrate aquí</router-link>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const email = ref('');
const password = ref('');

function onSubmit() {

    console.log('Login attempt', { email: email.value, password: password.value });
}
</script>

<style lang="scss">
@import "@/styles/forms.scss";
</style>