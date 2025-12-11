export default {
    title: 'Registro de Administradores',
    fields: {
        name: 'Nombre',
        namePlaceholder: 'Nombre completo',
        email: 'Email',
        emailPlaceholder: 'email@dominio.com',
        password: 'Contraseña',
        confirmPassword: 'Confirmar contraseña',
        role: 'Rol'
    },
    roles: {
        admin: 'Admin',
        superAdmin: 'Super Admin'
    },
    buttons: {
        submit: 'Registrar administrador',
        submitting: 'Registrando...',
        clear: 'Limpiar'
    },
    errors: {
        nameRequired: 'El nombre es requerido',
        emailRequired: 'El email es requerido',
        emailInvalid: 'Email inválido',
        passwordLength: 'La contraseña debe tener al menos 6 caracteres',
        passwordMismatch: 'Las contraseñas no coinciden',
        roleRequired: 'El rol es requerido'
    }
}
