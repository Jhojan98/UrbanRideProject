<template>
    <div class="page-with-background">
        <div class="form-container">
            <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
            <h2 class="form-title">Crear Cuenta</h2>
            <form @submit.prevent="onSubmit">
                <div class="form-group">
                    <label for="id"><i class="fas fa-id-card"></i> Identificación</label>
                    <input id="id" type="text" inputmode="numeric" pattern="[0-9]*" v-model.number="id" placeholder="Número de identificación" required />
                </div>
                <div class="form-group">
                    <label for="username"><i class="fas fa-user"></i> Nombre de Usuario</label>
                    <input id="username" type="text" v-model="username" placeholder="Nombre de usuario" required />
                </div>
                <div class="form-group">
                    <label for="fName"><i class="fas fa-user"></i> Primer Nombre</label>
                    <input id="fName" type="text" v-model="fName" placeholder="Primer nombre" required />
                </div>
                <div class="form-group">
                    <label for="sName"><i class="fas fa-user"></i> Segundo Nombre</label>
                    <input id="sName" type="text" v-model="sName" placeholder="Segundo nombre (opcional)" />
                </div>
                <div class="form-group">
                    <label for="fLastName"><i class="fas fa-user"></i> Primer Apellido</label>
                    <input id="fLastName" type="text" v-model="fLastName" placeholder="Primer apellido" required />
                </div>
                <div class="form-group">
                    <label for="sLastName"><i class="fas fa-user"></i> Segundo Apellido</label>
                    <input id="sLastName" type="text" v-model="sLastName" placeholder="Segundo apellido (opcional)" />
                </div>
                <div class="form-group">
                    <label for="birthDate"><i class="fas fa-calendar"></i> Fecha de Nacimiento</label>
                    <input id="birthDate" type="date" v-model="birthDate" required />
                </div>
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope"></i> Correo Electrónico</label>
                    <input id="email" type="email" v-model="email" placeholder="correo@ejemplo.com" required />
                </div>
                <div class="form-group">
                    <label for="password"><i class="fas fa-lock"></i> Contraseña</label>
                    <input id="password" type="password" v-model="password" placeholder="Contraseña" required />
                </div>
                <button type="submit" class="form-submit">Registrarse</button>
                <h3>{{ feedback }}</h3>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref } from 'vue';
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

const onSubmit = async () => {
    feedback.value = '';
    
    // Convertir la fecha string a objeto Date
    // El input type="date" devuelve formato yyyy-mm-dd, creamos un Date que se serializará en JSON
    const birthDateObj = new Date(birthDate.value + 'T00:00:00');
    
    if (id.value === null) {
        feedback.value = "Por favor ingresa tu identificación.";
        return;
    }

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
        feedback.value = "Registro exitoso. Te enviaremos un código de verificación.";
        // Redirigir a la página de verificación OTP
        router.push('/verify-otp');
    } else {
        feedback.value = "Error en el registro";
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