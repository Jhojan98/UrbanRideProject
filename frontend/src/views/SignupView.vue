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
        </form>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const name = ref('');
const email = ref('');
const password = ref('');

function onSubmit() {
    // Lógica de registro, por ejemplo enviar datos a la API.
    console.log('Signup attempt', { name: name.value, email: email.value, password: password.value });
}
</script>

<style lang="scss">
@import "@/styles/login.scss";
</style>