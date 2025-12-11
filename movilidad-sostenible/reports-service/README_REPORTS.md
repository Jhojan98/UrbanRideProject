# Reports Service

Este microservicio es responsable de generar reportes administrativos y dashboards de usuario para la plataforma UrbanRide.

## Funcionalidades

- **Reportes Administrativos**: Generación de reportes generales en formatos Excel (.xlsx) y PDF (.pdf).
- **Dashboard de Usuario**: Generación de reportes individuales por usuario en Excel y PDF.
- **Gráficos**: Inclusión de gráficos estadísticos (uso de bicicletas, demanda de estaciones, etc.) en los reportes.
- **Soporte Multilingüe**: Soporte básico para español e inglés en los reportes generados.

## Tecnologías

- **Python 3.9+**
- **FastAPI**: Framework web.
- **Pandas**: Manipulación de datos y generación de Excel.
- **ReportLab**: Generación de PDFs.
- **Matplotlib**: Generación de gráficos.
- **OpenPyXL**: Motor de escritura para Excel.

## Arquitectura: Patrón Aggregator

Este servicio implementa el **Patrón Aggregator** (Agregador) para consolidar información distribuida en múltiples microservicios.

### ¿Por qué se usa?
En una arquitectura de microservicios, los datos de usuarios, viajes, bicicletas y estaciones residen en bases de datos separadas. Para generar un reporte unificado, sería ineficiente que el cliente (frontend) hiciera docenas de peticiones a cada servicio.

### ¿Cómo funciona?
1.  **Recepción**: El `reports-service` recibe una única petición (ej. "Dame el dashboard del usuario X").
2.  **Recolección**: La clase `ReportAggregator` utiliza `ServiceClients` para realizar peticiones HTTP asíncronas a los servicios de:
    *   `usuario-service` (Datos personales, saldo)
    *   `viaje-service` (Historial de viajes)
    *   `estaciones-service` (Nombres de estaciones)
    *   `mantenimiento-service` (Registros de mantenimiento)
3.  **Procesamiento**: Los datos crudos se combinan, filtran y transforman. Por ejemplo, se cruzan los IDs de estaciones de los viajes con los nombres reales obtenidos del servicio de estaciones.
4.  **Respuesta**: Se entrega un único objeto consolidado (o archivo generado) al cliente.

## Endpoints Principales

### Admin
- `GET /api/admin/overview.xlsx`: Descargar reporte general en Excel.
- `GET /api/admin/overview.pdf`: Descargar reporte general en PDF.

### Usuario
- `GET /api/users/{user_id}/dashboard.xlsx`: Descargar dashboard de usuario en Excel.
- `GET /api/users/{user_id}/dashboard.pdf`: Descargar dashboard de usuario en PDF.

### Reportes Específicos
- `GET /api/reports/bicycle-usage.xlsx` / `.pdf`
- `GET /api/reports/station-demand.xlsx` / `.pdf`
- `GET /api/reports/service-demand.xlsx` / `.pdf`
- `GET /api/reports/bicycle-demand.xlsx` / `.pdf`
- `GET /api/reports/daily-trips.xlsx` / `.pdf`
- `GET /api/reports/maintenances.xlsx` / `.pdf`

## Nota de Desarrollo

Este servicio ha sido desarrollado con la asistencia de herramientas de Inteligencia Artificial para la optimización de código, generación de gráficos y estructuración de reportes.
