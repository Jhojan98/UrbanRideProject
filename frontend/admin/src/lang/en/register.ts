export default {
    title: 'Administrator Registration',
    fields: {
        name: 'Name',
        namePlaceholder: 'Full name',
        email: 'Email',
        emailPlaceholder: 'email@domain.com',
        password: 'Password',
        confirmPassword: 'Confirm password',
        role: 'Role'
    },
    roles: {
        admin: 'Admin',
        superAdmin: 'Super Admin'
    },
    buttons: {
        submit: 'Register administrator',
        submitting: 'Registering...',
        clear: 'Clear'
    },
    errors: {
        nameRequired: 'Name is required',
        emailRequired: 'Email is required',
        emailInvalid: 'Invalid email',
        passwordLength: 'Password must be at least 6 characters',
        passwordMismatch: 'Passwords do not match',
        roleRequired: 'Role is required'
    }
}
