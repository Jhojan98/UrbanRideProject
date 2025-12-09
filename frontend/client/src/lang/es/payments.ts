export default {
    title: 'Métodos de Pago',
    subtitle: 'Gestiona tus formas de pago para realizar reservas',
    primary: 'Principal',
    makePrimary: 'Hacer Principal',
    delete: 'Eliminar',
    addNewTitle: 'Agregar nuevo método de pago',
    cardType: 'Tipo de tarjeta',
    selectType: 'Seleccionar tipo',
    cardNumber: 'Número de tarjeta',
    expiry: 'Fecha de expiración',
    cvv: 'CVV',
    setAsPrimary: 'Establecer como método de pago principal',
    adding: 'Agregando...',
    addCard: 'Agregar Tarjeta',
    confirmDelete: '¿Estás seguro de que quieres eliminar este método de pago?',
    visa: 'Visa',
    mastercard: 'Mastercard',
    amex: 'American Express',
    recharge: {
        title: 'Recargar Saldo',
        subtitle: 'Elige el monto que deseas recargar en tu cuenta',
        selectAmount: 'Por favor selecciona un monto',
        processing: 'Procesando...',
        rechargeBtn: 'Recargar Saldo',
        security: '🔒 Serás redirigido a Stripe para completar el pago de forma segura',
        notAuthenticated: 'No se encontró usuario autenticado. Vuelve a iniciar sesión.',
        priceNotFound: 'PriceId no configurado para el monto seleccionado',
        error: 'Error al iniciar el pago',
        tryAgain: 'Por favor intenta nuevamente.',
        currency: 'Moneda',
        estimatedNote: 'Nota: Los montos se muestran en {currency} para tu referencia. El pago se procesa en USD.'
    },
    success: {
        title: 'Pago realizado ✅',
        subtitle: '¡Gracias por tu compra! Tu saldo ha sido recargado exitosamente.',
        sessionId: 'Session ID:',
        user: 'Usuario:',
        updatedBalance: 'Saldo actualizado:',
        viewBalance: 'Ver mi saldo actualizado',
        backHome: 'Volver al inicio'
    },
    cancel: {
        title: 'Pago cancelado ❌',
        subtitle: 'El pago fue cancelado o no se completó. Si fue un error intenta nuevamente.',
        backHome: 'Volver al inicio'
    }
};
