<template>
    <div class="page-with-background">
        <div class="form-container">
            <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
            <h2 class="form-title">{{ $t('auth.signup.title') }}</h2>
            <form @submit.prevent="onSubmit" class="form-grid">
                <div class="form-group">
                    <label for="id"><i class="fas fa-id-card"></i> {{ $t('auth.signup.id') }}</label>
                    <input id="id" type="text" inputmode="numeric" pattern="[0-9]*" v-model.number="id" :placeholder="$t('auth.signup.idPlaceholder')" required />
                </div>
                <div class="form-group">
                    <label for="username"><i class="fas fa-user"></i> {{ $t('auth.signup.username') }}</label>
                    <input id="username" type="text" v-model="username" :placeholder="$t('auth.signup.usernamePlaceholder')" required />
                </div>
                <div class="form-group">
                    <label for="fName"><i class="fas fa-user"></i> {{ $t('auth.signup.firstName') }}</label>
                    <input id="fName" type="text" v-model="fName" :placeholder="$t('auth.signup.firstNamePlaceholder')" required />
                </div>
                <div class="form-group">
                    <label for="sName"><i class="fas fa-user"></i> {{ $t('auth.signup.secondName') }}</label>
                    <input id="sName" type="text" v-model="sName" :placeholder="$t('auth.signup.secondNamePlaceholder')" />
                </div>
                <div class="form-group">
                    <label for="fLastName"><i class="fas fa-user"></i> {{ $t('auth.signup.firstLastName') }}</label>
                    <input id="fLastName" type="text" v-model="fLastName" :placeholder="$t('auth.signup.firstLastNamePlaceholder')" required />
                </div>
                <div class="form-group">
                    <label for="sLastName"><i class="fas fa-user"></i> {{ $t('auth.signup.secondLastName') }}</label>
                    <input id="sLastName" type="text" v-model="sLastName" :placeholder="$t('auth.signup.secondLastNamePlaceholder')" />
                </div>
                <div class="form-group">
                    <label for="birthDate"><i class="fas fa-calendar"></i> {{ $t('auth.signup.birthDate') }}</label>
                    <input id="birthDate" type="date" v-model="birthDate" required />
                </div>
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope"></i> {{ $t('auth.signup.email') }}</label>
                    <input id="email" type="email" v-model="email" :placeholder="$t('auth.signup.emailPlaceholder.name') + '@' + $t('auth.signup.emailPlaceholder.domain')" required />
                </div>
                <div class="form-group">
                    <label for="password"><i class="fas fa-lock"></i> {{ $t('auth.signup.password') }}</label>
                    <input id="password" type="password" v-model="password" :placeholder="$t('auth.signup.password')" required />
                </div>
                <button type="submit" class="form-submit">{{ $t('auth.signup.submit') }}</button>
                <button type="button" class="form-submit google-btn" @click="googleSignup">
                    <i class="fab fa-google"></i> {{ $t('auth.signup.google') }}
                </button>
                <h3>{{ feedback }}</h3>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const id: Ref<number | null> = ref(null);
const username: Ref<string> = ref('');
const password: Ref<string> = ref('');
const fName: Ref<string> = ref('');
const sName: Ref<string> = ref('');
const fLastName: Ref<string> = ref('');
const sLastName: Ref<string> = ref('');
const birthDate: Ref<string> = ref('');
const email: Ref<string> = ref('');
const feedback: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

const { t: $t } = useI18n();
const onSubmit = async () => {
    feedback.value = '';
    
    // Convertir la fecha string a objeto Date
    // El input type="date" devuelve formato yyyy-mm-dd, creamos un Date que se serializará en JSON
    const birthDateObj = new Date(birthDate.value + 'T00:00:00');
    
    if (id.value === null) {
        feedback.value = $t('auth.signup.idRequired');
        return;
    }

    // Log de depuración para verificar valores antes de enviar
    console.log('=== DATOS DEL FORMULARIO DE REGISTRO ===');
    console.log('ID:', id.value);
    console.log('Username:', username.value);
    console.log('Password:', password.value ? '****** (length: ' + password.value.length + ')' : 'vacío');
    console.log('First Name:', fName.value);
    console.log('Second Name:', sName.value);
    console.log('First Last Name:', fLastName.value);
    console.log('Second Last Name:', sLastName.value);
    console.log('Birth Date (string):', birthDate.value);
    console.log('Birth Date (Date obj):', birthDateObj);
    console.log('Email:', email.value);
    console.log('=========================================');

    const res = await store.register(
        id.value,
        username.value,
        password.value,
        fName.value,
        sName.value || "",
        fLastName.value,
        sLastName.value || "",
        birthDateObj,
        email.value
    );

    if (res) {
        feedback.value = store.message || $t('auth.signup.success');
        // Redirigir a la página de verificación OTP
        router.push({ name: 'verify-email' });
    } else {
        feedback.value = store.message || $t('auth.signup.error');
    }
}

const googleSignup = async () => {
    feedback.value = '';
    let result;
    try {
        result = await store.socialLoginWithGoogle();
    } catch (e) {
        console.error('Excepción inesperada en socialLoginWithGoogle (signup):', e);
        result = { success: false };
    }
    if (result && result.success) {
        feedback.value = store.message || $t('auth.signup.success');
        setTimeout(() => {
            router.push({ name: 'reservation' });
        }, 1200);
    } else {
        feedback.value = store.message || $t('auth.signup.error');
    }
}
</script>

<style lang="scss" scoped>
@import "@/styles/login.scss";

/* Ocultar botones de aumentar/disminuir en inputs numéricos */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
  appearance: textfield;
}
</style>