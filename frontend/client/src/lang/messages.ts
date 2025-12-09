export const messages = {
    es: {
        common: {
            brand: 'ECORIDE',
            minutes: 'minutos',
            accept: 'Aceptar',
            cancel: 'Cancelar',
            loading: 'Cargando...',
            menu: 'Abrir menú',
            close: 'Cerrar'
        },
        nav: {
            home: 'Inicio',
            maps: 'Mapa',
            profile: 'Perfil',
            login: 'Iniciar Sesión',
            signup: 'Registrarse',
            logout: 'Cerrar Sesión'
        },
        home: {
            hero: {
                title: 'Rodar tu vida, respirar tu ciudad.',
                subtitle: 'Tu ciudad a tu ritmo, de forma sostenible.',
                buttons: {
                    stations: 'Ver Mapa de Estaciones',
                    learnMore: 'Aprende Más'
                }
            },
            discover: 'Descubre ecoBici',
            cards: {
                health: {
                    title: 'Salud y Bienestar',
                    text: 'Mejora tu condición física y reduce el estrés pedaleando por la ciudad.'
                },
                environment: {
                    title: 'Impacto Ambiental',
                    text: 'Contribuye a un aire más limpio y a una ciudad más verde eligiendo la bicicleta.'
                },
                efficiency: {
                    title: 'Movilidad Eficiente',
                    text: 'Evita el tráfico y llega a tu destino de forma rápida y flexible.'
                }
            },
            stations: {
                title: 'Nuestra Red de Estaciones',
                button: 'Ver Mapa Completo'
            }
        },
        auth: {
            login: {
                title: 'Iniciar Sesión',
                email: 'Correo Electrónico',
                password: 'Contraseña',
                submit: 'Entrar',
                google: 'Ingresar con Google',
                noAccount: '¿No tienes cuenta?',
                registerHere: 'Regístrate aquí',
                verifyRequired: 'Verificación requerida. Redirigiendo...',
                success: 'Login exitoso. Redirigiendo...',
                error: 'Error en el login'
            },
            signup: {
                title: 'Crear Cuenta',
                id: 'Identificación',
                idPlaceholder: 'Número de identificación',
                username: 'Nombre de Usuario',
                usernamePlaceholder: 'Nombre de usuario',
                firstName: 'Primer Nombre',
                firstNamePlaceholder: 'Primer nombre',
                secondName: 'Segundo Nombre',
                secondNamePlaceholder: 'Segundo nombre (opcional)',
                firstLastName: 'Primer Apellido',
                firstLastNamePlaceholder: 'Primer apellido',
                secondLastName: 'Segundo Apellido',
                secondLastNamePlaceholder: 'Segundo apellido (opcional)',
                birthDate: 'Fecha de Nacimiento',
                email: 'Correo Electrónico',
                emailPlaceholder: { name: 'juanperez68', domain: 'correo.com' },
                password: 'Contraseña',
                submit: 'Registrarse',
                google: 'Registrarse con Google',
                idRequired: 'Por favor ingresa tu identificación.',
                success: 'Registro exitoso. Te enviaremos un código de verificación.',
                error: 'Error en el registro'
            },
            otp: {
                title: 'Verificación de Email',
                codeSentTo: 'Correo enviado a:',
                codeLabel: 'Código OTP',
                codePlaceholder: 'Ingresa el código de 6 dígitos',
                verify: 'Verificar Email',
                resend: 'Reenviar Email',
                backToLogin: 'Volver al Inicio de Sesión',
                noEmailFound: 'No se encontró email para verificar. Redirigiendo al login...',
                noEmail: 'No hay email disponible para verificar',
                invalidOtp: 'El OTP debe ser un número válido de 6 dígitos',
                success: '¡Verificación exitosa! Redirigiendo...',
                verifyError: 'Error al verificar el email',
                noEmailResend: 'No hay email disponible para reenviar',
                resendSuccess: 'Email de verificación reenviado',
                resendError: 'Error al reenviar el email',
                emailSent: 'Hemos enviado un correo de verificación a tu email.',
                checkInbox: 'Por favor revisa tu bandeja de entrada y haz clic en el enlace de verificación.',
                afterVerify: 'Una vez verificado, haz clic en el botón de abajo para continuar.'
            }
        },
        profile: {
            loading: 'Cargando...',
            header: {
                welcome_user: 'Bienvenido de nuevo, {name}'
            },
            tabs: {
                overview: 'Resumen',
                trips: 'Viajes',
                fines: 'Multas',
                complaints: 'Quejas / Mantenimiento',
                reports: 'Reportes'
            },
            complaints: {
                title: 'Quejas y mantenimiento',
                subtitle: 'Reporta incidencias o levanta solicitudes de mantenimiento.',
                formTitle: 'Reportar queja',
                formSubtitle: 'Ingresa una queja para soporte. El ID de viaje es opcional.',
                descriptionLabel: 'Descripción',
                descriptionPlaceholder: 'Describe el inconveniente',
                typeLabel: 'Tipo',
                typeOptions: {
                    bicycle: 'Bicicleta',
                    slot: 'Candado / Slot',
                    station: 'Estación'
                },
                travelIdLabel: 'ID de viaje (opcional)',
                travelIdPlaceholder: 'Ej: 102',
                submit: 'Enviar queja',
                submitting: 'Enviando...',
                success: 'Queja registrada (#{id})',
                errorRequired: 'La descripción es obligatoria',
                errorNumeric: 'El ID de viaje debe ser numérico',
                submitError: 'No se pudo enviar la queja',
                maintenanceTitle: 'Solicitar mantenimiento',
                maintenanceSubtitle: 'Redirige al servicio de mantenimiento para crear un ticket.',
                openMaintenance: 'Abrir mantenimiento'
            },
            reportsSection: {
                title: 'Reportes',
                subtitle: 'Descarga reportes operativos en PDF o Excel.',
                cards: {
                    bicycleUsage: 'Uso de bicicletas',
                    stationDemand: 'Demanda de estaciones',
                    bicycleDemand: 'Demanda de bicicletas',
                    dailyTrips: 'Viajes diarios',
                    maintenances: 'Mantenimientos'
                },
                downloadExcel: 'Excel',
                downloadPdf: 'PDF',
                downloadError: 'No se pudo descargar el reporte. Verifica que reports-service (5004) esté arriba.'
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
                noFines: 'No hay multas pendientes'
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
                currency: 'Moneda'
            }
        },
        reservation: {
            form: {
                stationDetails: 'Detalles de la Estación',
                defaultStation: 'Selecciona una estación',
                selectStation: 'Selecciona una estación',
                bikesAvailable: 'Bicicletas Disponibles',
                totalSlots: 'Total de puestos',
                bikeType: 'Tipo de Bicicleta:',
                mechanical: 'Mecánica',
                electric: 'Eléctrica',
                rideType: 'Tipo de Viaje:',
                lastMile: 'Última Milla',
                lastMileMax: 'Máx 45 min',
                longTrip: 'Recorrido Largo',
                longTripMax: 'Máx 75 min',
                balance: 'Saldo:',
                recharge: 'Recargar',
                warning: 'La bicicleta se reservará por',
                warningMinutes: '10 minutos',
                reserveBike: 'Reservar Bicicleta',
                selectionAlert: 'Por favor selecciona el tipo de bicicleta y el tipo de viaje.'
            },
            confirmation: {
                remainingTime: 'Tiempo restante de reserva',
                tripDetails: 'Detalles del viaje',
                bikeType: 'Tipo de bicicleta:',
                tripType: 'Tipo de viaje:',
                estimatedCost: 'Costo estimado:',
                confirm: 'Confirmar Reserva',
                enterBikeCode: 'Ingresa el código de la bicicleta',
                unlock: 'Desbloquear'
            },
            map: {
                authRequired: 'Acceso restringido',
                authMessage: 'Para acceder a nuestros servicios y ver el mapa, debes iniciar sesión primero.',
                accept: 'Aceptar',
                originStation: 'Estación Origen',
                destinationStation: 'Estación Destino',
                availableBikes: 'bicicletas',
                availableSlots: 'puestos libres',
                markerOrigin: 'Origen',
                markerDestination: 'Destino',
                markerCoords: 'Lat: {lat}, Lng: {lng}',
                popup: {
                    availableTypes: 'Tipos disponibles',
                    mechanical: 'Mecánicas',
                    electric: 'Eléctricas',
                    available: 'Disponibles',
                    slots: 'Slots',
                    noSlots: 'Sin slots',
                    slotFree: 'Slot {num}: Libre',
                    slotOccupied: 'Slot {num}: Ocupado',
                    slotMaintenance: 'Slot {num}: Mantto.',
                    slotOutOfService: 'Slot {num}: Fuera',
                    slotUnknown: 'Desconocido'
                },
                bicycle: {
                    title: 'Bicicleta {id}',
                    battery: 'Batería',
                    location: 'Ubicación',
                    lastUpdate: 'Última actualización'
                }
            },
            notifications: {
                expiredTravel: 'Viaje Cancelado',
                expiredTravelMsg: 'El tiempo de espera ha expirado y tu viaje ha sido cancelado automáticamente.',
                startTravel: 'Viaje Iniciado',
                startTravelMsg: '¡Tu reserva ha sido confirmada! El viaje ha iniciado exitosamente.',
                endTravel: 'Viaje Finalizado',
                endTravelMsg: 'Tu viaje ha finalizado. Los detalles y el monto total serán enviados a tu correo electrónico.',
                defaultNotification: 'Notificación',
                closeLabel: 'Cerrar notificación'
            }
        },
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
        payments: {
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
    },
    en: {
        common: {
            brand: 'ECORIDE',
            minutes: 'minutes',
            accept: 'Accept',
            cancel: 'Cancel',
            loading: 'Loading',
            menu: 'Open menu',
            close: 'Close'
        },
        nav: {
            home: 'Home',
            maps: 'Map',
            profile: 'Profile',
            login: 'Login',
            signup: 'Sign Up',
            logout: 'Log Out'
        },
        home: {
            hero: {
                title: 'Pedal, breathe, live your city.',
                subtitle: 'Your city at your pace, sustainably.',
                buttons: {
                    stations: 'View Stations Map',
                    learnMore: 'Learn More'
                }
            },
            discover: 'Discover ecoBici',
            cards: {
                health: {
                    title: 'Health & Wellness',
                    text: 'Improve fitness and reduce stress by cycling through the city.'
                },
                environment: {
                    title: 'Environmental Impact',
                    text: 'Contribute to cleaner air and a greener city by choosing a bike.'
                },
                efficiency: {
                    title: 'Efficient Mobility',
                    text: 'Avoid traffic and reach your destination quickly and flexibly.'
                }
            },
            stations: {
                title: 'Our Station Network',
                button: 'View Full Map'
            }
        },
        auth: {
            login: {
                title: 'Login',
                email: 'Email',
                password: 'Password',
                submit: 'Enter',
                google: 'Sign in with Google',
                noAccount: "Don't have an account?",
                registerHere: 'Register here',
                verifyRequired: 'Verification required. Redirecting...',
                success: 'Login successful. Redirecting',
                error: 'Login error'
            },
            signup: {
                title: 'Create Account',
                id: 'Identification',
                idPlaceholder: 'Identification number',
                username: 'Username',
                usernamePlaceholder: 'Username',
                firstName: 'First Name',
                firstNamePlaceholder: 'First name',
                secondName: 'Middle Name',
                secondNamePlaceholder: 'Middle name (optional)',
                firstLastName: 'Last Name',
                firstLastNamePlaceholder: 'Last name',
                secondLastName: 'Second Last Name',
                secondLastNamePlaceholder: 'Second last name (optional)',
                birthDate: 'Birth Date',
                email: 'Email',
                emailPlaceholder: { name: 'johndoe', domain: 'email.com' },
                password: 'Password',
                submit: 'Register',
                google: 'Sign up with Google',
                idRequired: 'Please enter your identification',
                success: 'Registration successful. We will send a verification code',
                error: 'Registration error'
            },
            otp: {
                title: 'Email Verification',
                codeSentTo: 'Email sent to:',
                codeLabel: 'OTP Code',
                codePlaceholder: 'Enter the 6-digit code',
                verify: 'Verify Email',
                resend: 'Resend Email',
                backToLogin: 'Back to Login',
                noEmailFound: 'No email found to verify. Redirecting to login',
                noEmail: 'No email available to verify',
                invalidOtp: 'OTP must be a valid 6-digit number',
                success: 'Verification successful! Redirecting',
                verifyError: 'Email verification error',
                noEmailResend: 'No email available to resend',
                resendSuccess: 'Verification email resent',
                resendError: 'Error resending email',
                emailSent: 'We have sent a verification email to your address',
                checkInbox: 'Please check your inbox and click on the verification link',
                afterVerify: 'Once verified, click the button below to continue'
            }
        },
        profile: {
            loading: 'Loading...',
            header: { welcome_user: 'Welcome back, {name}' },
            tabs: {
                overview: 'Overview',
                trips: 'Trips',
                fines: 'Fines',
                complaints: 'Complaints / Maintenance',
                reports: 'Reports'
            },
            complaints: {
                title: 'Complaints and maintenance',
                subtitle: 'Report incidents or raise maintenance requests.',
                formTitle: 'Submit complaint',
                formSubtitle: 'Enter a complaint for support. Travel ID is optional.',
                descriptionLabel: 'Description',
                descriptionPlaceholder: 'Describe the issue',
                typeLabel: 'Type',
                typeOptions: {
                    bicycle: 'Bicycle',
                    slot: 'Lock / Slot',
                    station: 'Station'
                },
                travelIdLabel: 'Travel ID (optional)',
                travelIdPlaceholder: 'e.g. 102',
                submit: 'Send complaint',
                submitting: 'Sending...',
                success: 'Complaint recorded (#{id})',
                errorRequired: 'Description is required',
                errorNumeric: 'Travel ID must be numeric',
                submitError: 'Could not submit the complaint',
                maintenanceTitle: 'Request maintenance',
                maintenanceSubtitle: 'Redirects to the maintenance service to create a ticket.',
                openMaintenance: 'Open maintenance'
            },
            reportsSection: {
                title: 'Reports',
                subtitle: 'Download operational reports in PDF or Excel.',
                cards: {
                    bicycleUsage: 'Bicycle usage',
                    stationDemand: 'Station demand',
                    bicycleDemand: 'Bicycle demand',
                    dailyTrips: 'Daily trips',
                    maintenances: 'Maintenances'
                },
                downloadExcel: 'Excel',
                downloadPdf: 'PDF',
                downloadError: 'Could not download the report. Check that reports-service (5004) is up.'
            },
            trips: {
                title: 'Trip History',
                startStation: 'Start Station',
                endStation: 'End Station',
                route: 'Route',
                date: 'Date',
                duration: 'Duration',
                cost: 'Cost',
                status: 'Status',
                viewAll: 'View all trips',
                noTrips: 'No trips recorded'
            },
            fines: {
                title: 'My Fines',
                fineId: 'Fine ID',
                reason: 'Reason',
                amount: 'Amount',
                date: 'Date',
                description: 'Description',
                status: 'Status',
                paid: 'Paid',
                pending: 'Pending',
                payNow: 'Pay Now',
                noFines: 'No pending fines'
            },
            loyalty: {
                title: 'Loyalty Points',
                pointsLabel: 'Current points',
                missingPoints: '{points} points left for next level.',
                rewardsTitle: 'Your rewards',
                reward1: '1 hour free ride',
                reward2: '10% subscription discount',
                reward3: 'Access to premium bikes',
                redeem: 'Redeem points'
            },
            balance: {
                title: 'Balance / Registered Card',
                currentBalance: 'Current balance',
                registeredCard: 'Registered Card',
                expires: 'Expires:',
                managePayments: 'Manage payment methods',
                addBalance: 'Add balance',
                currency: 'Currency'
            }
        },
        reservation: {
            form: {
                stationDetails: 'Station Details',
                defaultStation: 'Select a station',
                selectStation: 'Select a station',
                bikesAvailable: 'Available Bikes',
                totalSlots: 'Total slots',
                bikeType: 'Bike Type:',
                mechanical: 'Mechanical',
                electric: 'Electric',
                rideType: 'Trip Type:',
                lastMile: 'Last Mile',
                lastMileMax: 'Max 45 min',
                longTrip: 'Long Trip',
                longTripMax: 'Max 75 min',
                balance: 'Balance:',
                recharge: 'Recharge',
                warning: 'The bike will be reserved for',
                warningMinutes: '10 minutes',
                reserveBike: 'Reserve Bike',
                selectionAlert: 'Please select bike type and trip type.'
            },
            confirmation: {
                remainingTime: 'Reservation remaining time',
                tripDetails: 'Trip details',
                bikeType: 'Bike type:',
                tripType: 'Trip type:',
                estimatedCost: 'Estimated cost:',
                confirm: 'Confirm Reservation',
                enterBikeCode: 'Enter the bike code',
                unlock: 'Unlock'
            },
            map: {
                authRequired: 'Restricted access',
                authMessage: 'To access our services and view the map, you must login first',
                accept: 'Accept',
                originStation: 'Origin Station',
                destinationStation: 'Destination Station',
                availableBikes: 'bikes',
                availableSlots: 'available slots',
                markerOrigin: 'Origin',
                markerDestination: 'Destination',
                markerCoords: 'Lat: {lat}, Lng: {lng}',
                popup: {
                    availableTypes: 'Available types',
                    mechanical: 'Mechanical',
                    electric: 'Electric',
                    available: 'Available',
                    slots: 'Slots',
                    noSlots: 'No slots',
                    slotFree: 'Slot {num}: Free',
                    slotOccupied: 'Slot {num}: Occupied',
                    slotMaintenance: 'Slot {num}: Maint.',
                    slotOutOfService: 'Slot {num}: Out',
                    slotUnknown: 'Unknown'
                },
                bicycle: {
                    title: 'Bicycle {id}',
                    battery: 'Battery',
                    location: 'Location',
                    lastUpdate: 'Last update'
                }
            },
            notifications: {
                expiredTravel: 'Travel Cancelled',
                expiredTravelMsg: 'The wait time has expired and your trip has been automatically cancelled',
                startTravel: 'Travel Started',
                startTravelMsg: 'Your reservation has been confirmed! The trip has started successfully',
                endTravel: 'Travel Completed',
                endTravelMsg: 'Your trip has ended. Details and total amount will be sent to your email',
                defaultNotification: 'Notification',
                closeLabel: 'Close notification'
            }
        },
        destination: {
            title: 'Destination Station',
            subtitle: 'Location of the station where you must leave the bike',
            confirm: 'Confirm Destination',
            address: 'Address:',
            availableSlots: 'Available slots:',
            distance: 'Approximate distance:',
            estimatedTime: 'Estimated time:'
        },
        balance: {
            currencyLabel: 'Currency',
            reload: 'Reload balance',
            currencies: {
                USD: 'Dollar',
                EUR: 'Euro',
                COP: 'Colombian Peso'
            }
        },
        payments: {
            title: 'Payment Methods',
            subtitle: 'Manage your payment methods for reservations',
            primary: 'Primary',
            makePrimary: 'Make Primary',
            delete: 'Delete',
            addNewTitle: 'Add new payment method',
            cardType: 'Card type',
            selectType: 'Select type',
            cardNumber: 'Card number',
            expiry: 'Expiration date',
            cvv: 'CVV',
            setAsPrimary: 'Set as primary payment method',
            adding: 'Adding',
            addCard: 'Add Card',
            confirmDelete: 'Are you sure you want to delete this payment method?',
            visa: 'Visa',
            mastercard: 'Mastercard',
            amex: 'American Express',
            recharge: {
                title: 'Reload Balance',
                subtitle: 'Choose the amount you want to reload to your account',
                selectAmount: 'Please select an amount',
                processing: 'Processing',
                rechargeBtn: 'Reload Balance',
                security: '🔒 You will be redirected to Stripe to complete the payment securely',
                notAuthenticated: 'No authenticated user found. Please login again',
                priceNotFound: 'PriceId not configured for the selected amount',
                error: 'Error starting payment',
                tryAgain: 'Please try again',
                currency: 'Currency',
                estimatedNote: 'Note: Amounts are shown in {currency} for your reference. Payment is processed in USD'
            },
            success: {
                title: 'Payment completed ✅',
                subtitle: 'Thank you for your purchase! Your balance has been reloaded successfully',
                sessionId: 'Session ID:',
                user: 'User:',
                updatedBalance: 'Updated balance:',
                viewBalance: 'View my updated balance',
                backHome: 'Back to home'
            },
            cancel: {
                title: 'Payment cancelled ❌',
                subtitle: 'The payment was cancelled or not completed. If it was a mistake, try again',
                backHome: 'Back to home'
            }
        },
        report: {
            title: 'Report Problem',
            subtitle: 'Report any issue with your bike',
            bikeInfo: 'Bike information',
            bikeId: 'Bike ID',
            stationWhere: 'Station where it is located',
            selectStation: 'Select station',
            problemDetails: 'Problem details',
            problemType: 'Problem type',
            severity: 'Problem severity',
            description: 'Detailed description',
            descriptionPlaceholder: 'Describe the problem with as much detail as possible',
            allowsUse: 'Does it allow bike usage?',
            noUse: 'No, unsafe to use',
            yesUse: 'Yes, but with caution',
            sending: 'Sending report',
            sendReport: 'Send Report',
            successTitle: 'Report Sent',
            successMsg: 'We received your report and are reviewing it. We will contact you if more info is needed',
            reportId: 'Report ID:',
            problems: {
                mechanical: 'Mechanical Problem',
                electrical: 'Electrical Problem',
                brakes: 'Brakes',
                tire: 'Tires',
                chain: 'Chain',
                other: 'Other'
            },
            severityLevels: {
                low: { name: 'Low', desc: 'Minor issue, does not affect use' },
                medium: { name: 'Medium', desc: 'Affects use but manageable' },
                high: { name: 'High', desc: 'Serious issue, do not use bike' }
            },
            cancelConfirm: 'Are you sure you want to cancel? Form data will be lost',
            sendError: 'Error sending report. Please try again'
        },
        footer: {
            about: 'About',
            resources: 'Resources',
            legal: 'Legal',
            copyright: '© 2026 ECORIDE'
        }
    }
};
