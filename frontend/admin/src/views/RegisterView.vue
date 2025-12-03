<template>
    <div class="register-admin-page">
        <div class="container">
            <h1>Registro de Administradores</h1>

            <form class="admin-form" @submit.prevent="onSubmit">
                <div class="form-row">
                    <label for="name">Nombre</label>
                    <input id="name" v-model.trim="form.name" type="text" placeholder="Nombre completo" required />
                    <p v-if="errors.name" class="error-text">{{ errors.name }}</p>
                </div>

                <div class="form-row">
                    <label for="email">Email</label>
                    <input id="email" v-model.trim="form.email" type="email" placeholder="email@dominio.com" required />
                    <p v-if="errors.email" class="error-text">{{ errors.email }}</p>
                </div>

                <div class="form-grid">
                    <div class="form-row">
                        <label for="password">Contraseña</label>
                        <input id="password" v-model="form.password" type="password" placeholder="••••••••" required />
                        <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
                    </div>

                    <div class="form-row">
                        <label for="confirmPassword">Confirmar contraseña</label>
                        <input id="confirmPassword" v-model="form.confirmPassword" type="password" placeholder="••••••••" required />
                        <p v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</p>
                    </div>
                </div>

                <div class="form-row">
                    <label for="role">Rol</label>
                    <select id="role" v-model="form.role" required>
                        <option value="ADMIN">Admin</option>
                        <option value="SUPER_ADMIN">Super Admin</option>
                    </select>
                    <p v-if="errors.role" class="error-text">{{ errors.role }}</p>
                </div>

                <div class="actions">
                    <button class="primary" type="submit" :disabled="submitting">
                        {{ submitting ? 'Registrando...' : 'Registrar administrador' }}
                    </button>
                    <button class="secondary" type="button" @click="resetForm" :disabled="submitting">Limpiar</button>
                </div>

                <p v-if="submitMessage" class="submit-message">{{ submitMessage }}</p>
            </form>
        </div>
    </div>

</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import useAuthStore from '@/stores/auth'

type Role = 'ADMIN' | 'SUPER_ADMIN'

const form = reactive({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ADMIN' as Role,
})

const errors = reactive<{ [k: string]: string | null }>({
    name: null,
    email: null,
    password: null,
    confirmPassword: null,
    role: null,
})

const submitting = ref(false)
const submitMessage = ref('')

const validate = () => {
    errors.name = !form.name ? 'El nombre es requerido' : null
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    errors.email = !form.email ? 'El email es requerido' : (!emailRegex.test(form.email) ? 'Email inválido' : null)
    errors.password = form.password.length < 6 ? 'La contraseña debe tener al menos 6 caracteres' : null
    errors.confirmPassword = form.confirmPassword !== form.password ? 'Las contraseñas no coinciden' : null
    errors.role = !form.role ? 'El rol es requerido' : null

    return !errors.name && !errors.email && !errors.password && !errors.confirmPassword && !errors.role
}

const resetForm = () => {
    form.name = ''
    form.email = ''
    form.password = ''
    form.confirmPassword = ''
    form.role = 'ADMIN'
    submitMessage.value = ''
    Object.keys(errors).forEach(k => (errors[k] = null))
}

const authStore = useAuthStore()

const onSubmit = async () => {
    submitMessage.value = ''
    if (!validate()) return
    submitting.value = true
    try {
        const created = await authStore.createAdminAccount(form.email, form.password,form.name)
        if (!created) {
            throw new Error('No se pudo registrar el administrador')
        }
        // Nota: el store guarda en Firebase y backend (uidUser, userName).
        // Si el backend soporta nombre/rol, se puede ampliar en el store.
        submitMessage.value = 'Administrador registrado correctamente'
        resetForm()
    } catch (err) {
        const message = err instanceof Error ? err.message : 'Ocurrió un error al registrar'
        submitMessage.value = message
    } finally {
        submitting.value = false
    }
}
</script>

<style scoped>
.register-admin-page {
    background: var(--color-gray-very-light);
    min-height: calc(100vh - 80px);
}

.container {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
}

h1 {
    margin: 0 0 1rem 0;
    color: var(--color-text-primary-light);
}

.admin-form {
    background: var(--color-background-light);
    border-radius: var(--border-radius);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 1.5rem;
}

.form-row {
    margin-bottom: 1rem;
}

label {
    display: block;
    margin-bottom: 0.35rem;
    font-weight: 600;
    color: var(--color-text-primary-light);
}

input, select {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 1px solid var(--color-border-light);
    border-radius: var(--border-radius);
    font-size: 1rem;
    color: var(--select-text);
    background-color: var(--select-bg);
    transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

input:focus, select:focus {
    outline: none;
    border-color: var(--color-green-main);
    box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
}

.form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.actions {
    display: flex;
    gap: 1rem;
    margin-top: 1rem;
}

.error-text {
    margin: 0.35rem 0 0 0;
    color: var(--color-red-alert);
    font-size: 0.875rem;
}

.submit-message {
    margin-top: 1rem;
    color: var(--color-text-secondary-light);
}

@media (max-width: 768px) {
    .form-grid { grid-template-columns: 1fr; }
}

html[data-theme="dark"] .admin-form {
    background: var(--color-surface-dark);
}

html[data-theme="dark"] input,
html[data-theme="dark"] select {
    background-color: var(--color-surface-dark);
    color: var(--color-text-primary-dark);
    border-color: var(--color-border-dark);
}
</style>
