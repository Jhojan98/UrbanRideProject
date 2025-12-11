export default {
    loading: 'Cargando...',
    header: {
        welcome_user: 'Bienvenido de nuevo, {name}'
    },
    tabs: {
        overview: 'Saldo y Planes',
        trips: 'Viajes',
        fines: 'Multas',
        complaints: 'Quejas',
        reports: 'Reportes'
    },
    trips: {
        title: 'Historial de Viajes',
        startStation: 'Estación de Inicio',
        endStation: 'Estación de Destino',
        route: 'Ruta',
        date: 'Fecha',
        duration: 'Duración',
        cost: 'Costo',
        status: 'Estado',
        viewAll: 'Ver todos los viajes',
        noTrips: 'No hay viajes registrados'
    },
    fines: {
        title: 'Mis Multas',
        fineId: 'Multa ID',
        reason: 'Razón',
        amount: 'Monto',
        date: 'Fecha',
        description: 'Descripción',
        status: 'Estado',
        paid: 'Pagado',
        pending: 'Pendiente',
        payNow: 'Pagar Ahora',
        noFines: 'No hay multas pendientes',
        noAmount: 'No se puede procesar el pago sin cantidad',
        noAmountError: 'Error: No se puede procesar el pago sin cantidad',
        notIdentified: 'Error: Usuario no identificado',
        insufficientBalance: 'Saldo insuficiente. Tu saldo actual es {balance} y la multa es de {fine}',
        confirmPayment: '¿Deseas pagar la multa #{id} de {amount}?\n\nSaldo actual: {balance}\nSaldo después del pago: {remaining}',
        paymentSuccess: '¡Multa pagada exitosamente! Tu saldo ha sido actualizado.',
        paymentError: 'Error procesando pago de multa'
    },
    loyalty: {
        title: 'Puntos de Fidelización',
        pointsLabel: 'Puntos actuales',
        missingPoints: 'Faltan {points} puntos para el siguiente nivel.',
        rewardsTitle: 'Tus recompensas',
        reward1: '1 hora de viaje gratis',
        reward2: '10% de descuento en suscripción',
        reward3: 'Acceso a bicicletas premium',
        redeem: 'Canjear puntos'
    },
    balance: {
        title: 'Saldo / Tarjeta Registrada',
        currentBalance: 'Saldo actual',
        registeredCard: 'Tarjeta Registrada',
        expires: 'Expira:',
        managePayments: 'Gestionar métodos de pago',
        addBalance: 'Añadir saldo',
        buySubscription: 'Comprar Suscripción',
        currency: 'Moneda'
    },
    complaints: {
        title: 'Reportar Problema',
        subtitle: 'Cuéntanos sobre cualquier inconveniente que hayas tenido',
        formTitle: 'Reportar un Problema',
        formSubtitle: 'Completa el formulario para reportar un inconveniente',
        descriptionLabel: 'Descripción del problema',
        descriptionPlaceholder: 'Describe el problema con tanto detalle como sea posible...',
        typeLabel: 'Tipo de problema',
        typeOptions: {
            bicycle: 'Bicicleta',
            slot: 'Puesto de estacionamiento',
            station: 'Estación'
        },
        travelIdLabel: 'ID del viaje (opcional)',
        travelIdPlaceholder: 'Ej: 123456',
        submit: 'Enviar Reporte',
        submitting: 'Enviando...',
        success: 'Reporte enviado exitosamente. ID: {id}',
        errorRequired: 'La descripción es requerida',
        errorNumeric: 'El ID del viaje debe ser un número válido',
        submitError: 'Error al enviar el reporte. Intenta nuevamente'
    },
    reportsSection: {
        title: 'Reportes',
        subtitle: 'Descarga tus reportes en los formatos disponibles',
        downloadExcel: 'Descargar Excel',
        downloadPdf: 'Descargar PDF',
        downloadError: 'Error al descargar el reporte. Intenta nuevamente',
        cards: {
            bicycleUsage: 'Uso de Bicicletas',
            stationDemand: 'Demanda de Estaciones',
            bicycleDemand: 'Demanda de Bicicletas',
            dailyTrips: 'Viajes Diarios'
        }
    }
};
