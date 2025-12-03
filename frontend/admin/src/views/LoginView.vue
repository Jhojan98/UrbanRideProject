<!-- Src/views/LoginView.vue -->
<template>
    <div class="page-with-background">
        <div class="form-container">
            <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
            <h2 class="form-title">{{ t('login.title') }}</h2>
            <form @submit.prevent="logUser">
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope left"></i> {{ t('login.email') }}</label>
                    <input id="email" type="email" v-model="email" placeholder="" required />
                </div>
                <div class="form-group">
                    <label for="password"><i class="fas fa-lock"> </i>{{ t('login.password') }}</label>
                    <input id="password" type="password" v-model="password" placeholder="" required />
                </div>
                <button type="submit" class="form-submit">{{ t('login.button') }}</button>
            </form>

            <br>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

const email: Ref<string> = ref('');
const password: Ref<string> = ref('');
const feedback: Ref<string> = ref('');

const router = useRouter();

// i18n composable
const { t } = useI18n();

const logUser = async () => {
    feedback.value = ''

    try {

        router.push({ name: 'stationsDashboard' })
    } catch (error) {
        feedback.value = 'Error al iniciar sesión. Verifica tus credenciales.'
        console.error('Login error:', error)
    }
}
</script>

<style lang="scss" scoped>
@import "@/styles/login.scss";
</style>
