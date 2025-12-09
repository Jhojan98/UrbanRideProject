export default {
    title: 'Dashboard de Usuarios',
    nav: {
        users: 'Usuarios',
        fines: 'Multas',
        cac: 'Quejas y Comentarios',
        travel: 'Viajes'
    },
    list: {
        title: 'Lista de Usuarios',
        searchPlaceholder: 'Buscar usuario por nombre o email...',
        tableHeaders: {
            user: 'Usuario',
            email: 'Email',
            subscription: 'Tipo de Suscripción',
            balance: 'Balance',
            registrationDate: 'Fecha de Registro',
            actions: 'Acciones'
        },
        noSubscription: 'Sin suscripción',
        btnView: 'Ver información',
        noData: 'No se encontraron usuarios'
    },
    fines: {
        title: 'Gestión de Multas',
        filterAll: 'Todos los estados',
        filterPending: 'Pendiente',
        filterPaid: 'Pagada',
        filterCancelled: 'Cancelada',
        stats: {
            total: 'Total Multas',
            pending: 'Pendientes',
            paid: 'Pagadas',
            totalAmount: 'Monto Total'
        },
        tableHeaders: {
            id: 'ID',
            reason: 'Razón',
            description: 'Descripción',
            status: 'Estado',
            amount: 'Monto',
            date: 'Fecha'
        },
        noData: 'No se encontraron multas'
    },
    cac: {
        title: 'Quejas y Comentarios',
        filterAll: 'Todos los estados',
        filterPending: 'Pendiente',
        filterInProcess: 'En Proceso',
        filterResolved: 'Resuelto',
        filterClosed: 'Cerrado',
        stats: {
            total: 'Total',
            pending: 'Pendientes',
            inProcess: 'En Proceso',
            resolved: 'Resueltos'
        },
        ticketId: 'Ticket',
        travelId: 'Viaje',
        noData: 'No se encontraron quejas o comentarios',
        modalTitle: 'Detalle del Ticket',
        modalSections: {
            status: 'Estado',
            description: 'Descripción',
            travelInfo: 'Información del Viaje',
            travelIdLabel: 'ID del Viaje',
            date: 'Fecha'
        }
    },
    travel: {
        title: 'Viajes',
        btnRefresh: 'Actualizar',
        loading: 'Cargando...',
        tableHeaders: {
            id: 'ID',
            startStation: 'Estación Inicio',
            endStation: 'Estación Fin',
            start: 'Inicio',
            end: 'Fin',
            status: 'Estado',
            type: 'Tipo'
        },
        noData: 'No hay viajes registrados.'
    },
    modal: {
        title: 'Información del Usuario',
        tabs: {
            travels: 'Viajes',
            fines: 'Multas',
            cacs: 'Quejas/Comentarios'
        },
        loading: 'Cargando información...',
        noTravels: 'Este usuario no tiene viajes registrados',
        noFines: 'Este usuario no registra multas',
        noCacs: 'Este usuario no registra quejas o comentarios',
        travelCard: {
            id: 'Viaje',
            type: 'Tipo',
            bicycle: 'Bicicleta',
            startStation: 'Estación de Inicio',
            endStation: 'Estación de Fin',
            start: 'Inicio',
            end: 'Fin'
        },
        fineCard: {
            id: 'Multa',
            reason: 'Razón',
            amount: 'Monto',
            date: 'Fecha'
        },
        cacCard: {
            id: 'Ticket',
            description: 'Descripción',
            travel: 'Viaje',
            date: 'Fecha'
        }
    }
}
