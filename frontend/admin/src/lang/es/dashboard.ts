export default {
    stations: {
        title: 'Panel de estaciones',
        nameC: 'Nombre',
        locationC: 'Ubicación',
        CCTVC: 'Estado CCTV',
        iluminationC: 'Estado Iluminación',
        parkingC: 'Parqueadero',
        panicC: 'Botón de pánico',
        loadingMap: 'Cargando mapa...',
        statusActive: 'Activo | Inactivo',
        statusActiveF: 'Activa | Inactiva',
        panicStatus: 'ALARMA ACTIVADA | OK',
        slotStatus: 'Ocupado | Libre',
        aviables: 'Cupos disponibles: {count}',
        cathegory: 'Metro | Centro financiero | Residencial',

        filter: {
            label: 'Filtrar por Ciudad:',
            all: 'Todas las ciudades',
            showing: 'Mostrando {filtered} de {total} estaciones',
            showingAll: 'Mostrando todas las estaciones ({total})',
            noResults: 'No hay estaciones en esta ciudad'
        },

        tooltip: {
            title: 'Estación: {name} - Capacidad',
            totalSlots: 'Total de slots:',
            availableSlots: 'Slots disponibles:',
            electric: 'Bicicletas eléctricas:',
            mechanic: 'Bicicletas mecánicas:'
        },
        map: {
            slots: 'Slots:',
            electric: 'Bicis eléctricas:',
            mechanic: 'Bicis mecánicas:',
            cctv: 'CCTV:',
            lighting: 'Iluminación:',
            panic: 'Botón de pánico:'
        },
        summary: {
            slots: 'Slots',
            electric: 'Bicis eléctricas',
            mechanic: 'Bicis mecánicas'
        },
        statusLabel: {
            cctv: 'CCTV',
            lighting: 'Iluminación',
            panic: 'Botón de pánico',
            active: 'Activo',
            inactive: 'Inactivo',
            activeF: 'Activa',
            inactiveF: 'Inactiva'
        }
    },
    bikes: {
        title: 'Bicicletas en: {stationName}',
        idC: 'Id',
        conditionC: 'Condición',
        modelC: 'Modelo',
        typeC: 'Tipo',
        batteryC: 'Batería',
        bikeType: 'Eléctrica | Mecánica',
        bikeCondition: 'Óptima | Requiere mantenimiento'
    },
    isEmpty: 'No hay bicicletas en esta estación',
    logout: 'Cerrar sesión'
}
