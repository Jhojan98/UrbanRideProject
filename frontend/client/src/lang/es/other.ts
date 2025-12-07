export default {
    destination: {
        title: 'Estación Destino',
        subtitle: 'Ubicación de la estación donde debes dejar la bicicleta',
        confirm: 'Confirmar Destino',
        address: 'Dirección:',
        availableSlots: 'Espacios disponibles:',
        distance: 'Distancia aproximada:',
        estimatedTime: 'Tiempo estimado:'
    },
    balance: {
        currencyLabel: 'Moneda',
        reload: 'Recargar saldo',
        currencies: {
            USD: 'Dólar',
            EUR: 'Euro',
            COP: 'Peso colombiano'
        }
    },
    report: {
        title: 'Reportar Problema',
        subtitle: 'Informa sobre cualquier inconveniente con tu bicicleta',
        bikeInfo: 'Información de la bicicleta',
        bikeId: 'ID de la bicicleta',
        stationWhere: 'Estación donde se encuentra',
        selectStation: 'Seleccionar estación',
        problemDetails: 'Detalles del problema',
        problemType: 'Tipo de problema',
        severity: 'Gravedad del problema',
        description: 'Descripción detallada',
        descriptionPlaceholder: 'Describe el problema con tanto detalle como sea posible...',
        allowsUse: '¿Permite el uso de la bicicleta?',
        noUse: 'No, es peligroso usarla',
        yesUse: 'Sí, pero con precaución',
        sending: 'Enviando reporte...',
        sendReport: 'Enviar Reporte',
        successTitle: 'Reporte Enviado',
        successMsg: 'Hemos recibido tu reporte y lo estamos revisando. Te contactaremos si necesitamos más información.',
        reportId: 'ID del reporte:',
        problems: {
            mechanical: 'Problema Mecánico',
            electrical: 'Problema Eléctrico',
            brakes: 'Frenos',
            tire: 'Llantas',
            chain: 'Cadena',
            other: 'Otro'
        },
        severityLevels: {
            low: { name: 'Baja', desc: 'Problema menor, no afecta uso' },
            medium: { name: 'Media', desc: 'Afecta uso pero es manejable' },
            high: { name: 'Alta', desc: 'Problema grave, no usar la bicicleta' }
        },
        cancelConfirm: '¿Estás seguro de que quieres cancelar? Se perderán los datos del formulario.',
        sendError: 'Error al enviar el reporte. Intenta nuevamente.'
    },
    footer: {
        about: 'Acerca de',
        resources: 'Recursos',
        legal: 'Legal',
        copyright: '© 2026 ECORIDE'
    }
};
