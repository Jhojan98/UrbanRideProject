# 🚴 UrbanRide - Microservicio de Métodos de Pago

Microservicio FastAPI para gestión de métodos de pago con integración RabbitMQ y PostgreSQL.

## 📋 Características

- ✅ **FastAPI** - Framework moderno y rápido para APIs
- ✅ **PostgreSQL** - Base de datos relacional
- ✅ **RabbitMQ** - Message broker para eventos asíncronos
- ✅ **SQLAlchemy** - ORM para Python
- ✅ **Docker & Docker Compose** - Containerización completa
- ✅ **Validación Luhn** - Validación de números de tarjeta
- ✅ **Detección automática de marca** - VISA, MasterCard, AMEX, etc.
- ✅ **Enmascaramiento de tarjetas** - Seguridad de datos sensibles

## 🏗️ Arquitectura

```
┌─────────────────┐
│  FastAPI App    │
│  (Puerto 5002)  │
└────────┬────────┘
         │
         ├─────────────────┐
         │                 │
    ┌────▼─────┐    ┌─────▼────────┐
    │PostgreSQL│    │   RabbitMQ   │
    │(Puerto   │    │(Puertos 5672,│
    │ 5432)    │    │  15672)      │
    └──────────┘    └──────────────┘
```

## 🚀 Inicio Rápido

### Prerequisitos

- Docker Desktop
- PowerShell (Windows)

### Levantar servicios

```powershell
# Desde la raíz del proyecto
docker-compose up -d
```

### Verificar servicios

```powershell
docker-compose ps
```

### Ejecutar pruebas

```powershell
.\test-rabbitmq-simple.ps1
```

## 📡 API Endpoints

Base URL: `http://localhost:5002`

### Health Check
```http
GET /health
```

**Respuesta:**
```json
{
  "status": "UP",
  "service": "metodos-pago-api",
  "rabbitmq_connected": true
}
```

### Crear Método de Pago
```http
POST /api/metodos-pago/
Content-Type: application/json

{
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_nombre_titular": "Juan Perez",
  "f_fecha_expiracion": "2026-12-31",
  "n_numero_tarjeta_completo": "4532015112830366",
  "b_principal": true,
  "n_direccion_facturacion": "Calle 123 #45-67",
  "n_codigo_postal": "110111"
}
```

**Evento RabbitMQ:** `metodo_pago.created`

### Listar Métodos de Pago
```http
GET /api/metodos-pago/usuario/{usuario_cc}
```

### Obtener Método de Pago
```http
GET /api/metodos-pago/{id}/usuario/{usuario_cc}
```

### Obtener Método Principal
```http
GET /api/metodos-pago/usuario/{usuario_cc}/principal
```

### Actualizar Método de Pago
```http
PUT /api/metodos-pago/{id}/usuario/{usuario_cc}
Content-Type: application/json

{
  "n_nombre_titular": "Juan Perez Actualizado",
  "n_codigo_postal": "110222"
}
```

**Evento RabbitMQ:** `metodo_pago.updated`

### Eliminar Método de Pago
```http
DELETE /api/metodos-pago/{id}/usuario/{usuario_cc}
```

**Evento RabbitMQ:** `metodo_pago.deleted`

### Establecer como Principal
```http
PATCH /api/metodos-pago/{id}/usuario/{usuario_cc}/principal
```

**Evento RabbitMQ:** `metodo_pago.principal`

### Validar Tarjeta
```http
POST /api/metodos-pago/validar
Content-Type: application/json

{
  "numeroTarjeta": "4532015112830366"
}
```

## 🐰 RabbitMQ

### Exchange
- **Nombre:** `urbanride.payments`
- **Tipo:** Topic
- **Durable:** true

### Routing Keys
- `metodo_pago.created` - Cuando se crea un método de pago
- `metodo_pago.updated` - Cuando se actualiza un método de pago
- `metodo_pago.deleted` - Cuando se elimina un método de pago
- `metodo_pago.principal` - Cuando se establece como principal

### Estructura de Eventos

```json
{
  "event_type": "METODO_PAGO_CREATED",
  "timestamp": "1699311234.567",
  "data": {
    "k_metodo_pago": 1,
    "k_usuario_cc": "1234567890",
    "t_tipo_tarjeta": "CREDITO",
    "n_marca": "VISA",
    "b_principal": true
  }
}
```

### Management UI
- **URL:** http://localhost:15672
- **Usuario:** urbanride
- **Password:** urbanride2024

## 🗄️ Base de Datos

### Tabla: `metodo_pago`

| Columna | Tipo | Descripción |
|---------|------|-------------|
| k_metodopago | BIGSERIAL | ID único (PK) |
| k_usuario_cc | VARCHAR(50) | Cédula del usuario |
| t_tipotarjeta | VARCHAR(20) | CREDITO, DEBITO, PSE, EFECTIVO |
| n_numerotarjeta | VARCHAR(20) | Número enmascarado |
| n_numerotarjetacompleto | VARCHAR(20) | Número completo (⚠️ solo desarrollo) |
| n_nombretitular | VARCHAR(100) | Nombre del titular |
| f_fechaexpiracion | DATE | Fecha de expiración |
| n_marca | VARCHAR(20) | VISA, MASTERCARD, AMEX, etc. |
| b_principal | BOOLEAN | ¿Es método principal? |
| b_activo | BOOLEAN | ¿Está activo? |
| f_fecharegistro | TIMESTAMP | Fecha de registro |
| n_direccionfacturacion | VARCHAR(255) | Dirección de facturación |
| n_codigopostal | VARCHAR(20) | Código postal |

## 🔒 Seguridad

⚠️ **IMPORTANTE:** El almacenamiento de `n_numerotarjetacompleto` es solo para desarrollo/pruebas. 

En producción:
- ✅ Usar tokenización (Stripe, PayU, etc.)
- ✅ Encriptar datos sensibles
- ✅ Cumplir con PCI DSS
- ✅ No almacenar números de tarjeta completos

## 🧪 Validaciones

### Algoritmo de Luhn
Se valida el número de tarjeta usando el algoritmo de Luhn.

### Detección de Marca
- **VISA:** Comienza con 4
- **MasterCard:** Comienza con 51-55
- **AMEX:** Comienza con 34 o 37
- **DINERS:** Comienza con 36, 38, o 30
- **JCB:** Comienza con 35

### Enmascaramiento
- Formato: `**** **** **** 0366`
- Solo se muestran los últimos 4 dígitos

## 📦 Estructura del Proyecto

```
metodos-pago-api/
├── app/
│   ├── __init__.py
│   ├── main.py          # FastAPI app con lifespan
│   ├── database.py      # Configuración SQLAlchemy
│   ├── models.py        # Modelos ORM
│   ├── schemas.py       # Pydantic schemas
│   ├── routers.py       # Endpoints API
│   ├── utils.py         # Luhn, detectar marca, enmascarar
│   └── messaging.py     # Cliente RabbitMQ
├── Dockerfile
└── requirements.txt
```

## 🔧 Variables de Entorno

```bash
DB_HOST=postgres
DB_PORT=5432
DB_NAME=sistemabicicletas
DB_USER=urbanride
DB_PASSWORD=urbanride2024
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=urbanride
RABBITMQ_PASSWORD=urbanride2024
```

## 🐛 Troubleshooting

### Contenedor no inicia
```powershell
docker-compose logs metodos-pago-api
```

### RabbitMQ no conecta
```powershell
# Reiniciar servicio
docker-compose restart metodos-pago-api
```

### Base de datos no tiene tabla
```powershell
# Crear tabla manualmente
docker exec urbanride-postgres psql -U urbanride -d sistemabicicletas -f /docker-entrypoint-initdb.d/02-metodo-pago.sql
```

## 📊 Monitoreo

### RabbitMQ Management
- Ver exchanges: http://localhost:15672/#/exchanges
- Ver mensajes publicados
- Ver conexiones activas

### pgAdmin
- URL: http://localhost:5050
- Usuario: admin@urbanride.com
- Password: admin2024

### Logs
```powershell
# Ver logs en tiempo real
docker-compose logs -f metodos-pago-api

# Ver últimas 100 líneas
docker logs urbanride-metodos-pago-api --tail 100
```

## 🚦 Estado de Servicios

```powershell
# Verificar estado
docker-compose ps

# Reiniciar todos los servicios
docker-compose restart

# Detener todos los servicios
docker-compose down

# Rebuild completo
docker-compose up -d --build --force-recreate
```

## 📝 Licencia

UrbanRide © 2025

## 🤝 Contribuir

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

---

**Desarrollado con ❤️ usando FastAPI, RabbitMQ y PostgreSQL**
