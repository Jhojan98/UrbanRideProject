<!-- Src/views/LoginView.vue -->
<template>
    <div class="page-with-background">
        <div class="form-container">
            <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
            <h2 class="form-title">Iniciar Sesión</h2>
            <form @submit.prevent="logUser">
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope left"></i> Correo Electrónico</label>
                    <input id="email" type="email" v-model="email" placeholder="" required />
                </div>
                <div class="form-group">
                    <label for="password"><i class="fas fa-lock"> </i>Contraseña</label>
                    <input id="password" type="password" v-model="password" placeholder="" required />
                </div>
                <button type="submit" class="form-submit">Entrar</button>
            </form>

            <br>       
        </div>
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

    //const result = await store.login(email.value, password.value);
    router.push({ name: 'dashboard' });


}
</script>

<style lang="scss" scoped>
@import "@/styles/login.scss";
</style>