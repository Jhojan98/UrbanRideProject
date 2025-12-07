export const messages = {
    es: {
        login: {
            title: 'Iniciar sesión',
            email: 'Correo electrónico',
            password: 'Contraseña',
            button: 'Entrar'
        },
        dashboard: {
            stations: {
                title: 'Panel de estaciones',
                nameC: 'Nombre',
                locationC: 'Ubicación',
                CCTVC: 'Estado CCTV',
                iluminationC: 'Estado Iluminación',
                parkingC: 'Parqueadero',
                panicC: 'Botón de pánico',
                statusActive: 'Activo | Inactivo',
                statusActiveF: 'Activa | Inactiva',
                panicStatus: 'ALARMA ACTIVADA | OK',
                slotStatus: 'Ocupado | Libre',
                aviables: 'Cupos disponibles: {count}',
                cathegory:'Metro | Centro financiero | Residencial'
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
    },
    management: {
      title: 'Gestión Administrativa',
      tabs: {
        cities: 'Ciudades',
        stations: 'Estaciones',
        bicycles: 'Bicicletas',
        slots: 'Slots'
      },
      cities: {
        title: 'Gestión de Ciudades',
        create: 'Crear Ciudad',
        newCity: 'Nueva Ciudad',
        idCity: 'ID Ciudad',
        cityName: 'Nombre de Ciudad',
        empty: 'No hay ciudades registradas',
        confirmDelete: '¿Está seguro de eliminar esta ciudad?',
        createSuccess: 'Ciudad creada exitosamente. Los cambios pueden tardar unos segundos.',
        createError: 'Error al crear la ciudad',
        deleteSuccess: 'Ciudad eliminada exitosamente',
        deleteError: 'Error al eliminar la ciudad'
      },
      stations: {
        title: 'Gestión de Estaciones',
        create: 'Crear Estación',
        newStation: 'Nueva Estación',
        idStation: 'ID Estación',
        stationName: 'Nombre de Estación',
        latitude: 'Latitud',
        longitude: 'Longitud',
        city: 'Ciudad',
        selectCity: 'Seleccione una ciudad',
        type: 'Tipo',
        location: 'Ubicación',
        cctvStatus: 'CCTV Activo',
        empty: 'No hay estaciones registradas',
        confirmDelete: '¿Está seguro de eliminar esta estación? Esto también eliminará sus slots.',
        createSuccess: 'Estación creada exitosamente con 15 slots generados automáticamente',
        createError: 'Error al crear la estación',
        deleteSuccess: 'Estación eliminada exitosamente',
        deleteError: 'Error al eliminar la estación'
      },
      bicycles: {
        title: 'Gestión de Bicicletas',
        create: 'Crear Bicicleta',
        newBicycle: 'Nueva Bicicleta',
        type: 'Tipo de Bicicleta',
        electric: 'Eléctrica',
        mechanic: 'Mecánica',
        series: 'Serie',
        model: 'Modelo',
        padlockStatus: 'Estado del Candado',
        battery: 'Batería (%)',
        empty: 'No hay bicicletas registradas',
        confirmDelete: '¿Está seguro de eliminar esta bicicleta?',
        createSuccess: 'Bicicleta creada exitosamente',
        createError: 'Error al crear la bicicleta',
        deleteSuccess: 'Bicicleta eliminada exitosamente',
        deleteError: 'Error al eliminar la bicicleta'
      },
      slots: {
        title: 'Gestión de Slots',
        filterByStation: 'Filtrar por Estación:',
        allStations: 'Todas las estaciones',
        station: 'Estación',
        bicycle: 'Bicicleta',
        noBicycle: 'Sin bicicleta',
        assignBicycle: 'Asignar Bicicleta',
        assignBicycleTitle: 'Asignar Bicicleta a Slot',
        assigningToSlot: 'Asignando a slot',
        selectBicycle: 'Seleccionar Bicicleta',
        chooseBicycle: 'Elija una bicicleta disponible',
        empty: 'No hay slots disponibles',
        assignSuccess: 'Bicicleta asignada exitosamente al slot',
        assignError: 'Error al asignar la bicicleta'
      }
    },
    common: {
      save: 'Guardar',
      saving: 'Guardando...',
      cancel: 'Cancelar',
      actions: 'Acciones',
      loading: 'Cargando...',
      assign: 'Asignar',
      assigning: 'Asignando...'
    },
  
    en: {
        login: {
            title: 'Sign In',
            email: 'Email',
            password: 'Password',
            button: 'Login'
        },
        dashboard: {
            stations: {
                title: 'Stations Panel',
                nameC: 'Name',
                locationC: 'Location',
                CCTVC: 'CCTV Status',
                iluminationC: 'Lighting Status',
                parkingC: 'Parking',
                panicC: 'Panic Button',
                statusActive: 'Active | Inactive',
                statusActiveF: 'Active | Inactive',
                panicStatus: 'ALARM TRIGGERED | OK',
                slotStatus: 'Occupied | Free',
                aviables: 'Available slots: {count}',
                cathegory:'Metro | Financial Center | Residential'
            },
            bikes: {
                title: 'Bikes at: {stationName}',
                idC: 'Id',
                conditionC: 'Condition',
                modelC: 'Model',
                typeC: 'Type',
                batteryC: 'Battery',
                bikeType: 'Electric | Mechanical',
                bikeCondition: 'Optimal | Needs maintenance'
            },
            isEmpty: 'No bikes at this station',
            logout: 'Log out'
        }
    }
}