<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.jpg" alt="Logo" class="form-logo" />
        <h2 class="form-title">Iniciar Sesión</h2>
        <form @submit.prevent="logUser">
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
import { RouterLink } from 'vue-router';

const email: Ref<string> = ref('');
const password: Ref<string> = ref('');
const feedback: Ref<string> = ref('');
const store = userAuth();

const logUser = async () => {
    const res = await store.login(email.value, password.value)
}


</script>

<style lang="scss">
@import "@/styles/login.scss";
</style>