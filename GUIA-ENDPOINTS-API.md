# 📚 Guía Completa de Endpoints - API Métodos de Pago

## 🌐 Base URL
```
http://localhost:5002
```

## 📖 Swagger UI
```
http://localhost:5002/docs
```
- Documentación interactiva
- Probar endpoints directamente desde el navegador
- Ver schemas y ejemplos

---

## 📋 Índice de Endpoints

1. [Health Check](#1-health-check)
2. [Crear Método de Pago](#2-crear-método-de-pago)
3. [Listar Métodos de Pago](#3-listar-métodos-de-pago)
4. [Obtener Método Específico](#4-obtener-método-específico)
5. [Obtener Método Principal](#5-obtener-método-principal)
6. [Actualizar Método de Pago](#6-actualizar-método-de-pago)
7. [Eliminar Método de Pago](#7-eliminar-método-de-pago)
8. [Establecer como Principal](#8-establecer-como-principal)
9. [Validar Número de Tarjeta](#9-validar-número-de-tarjeta)
10. [Recargar Saldo](#10-recargar-saldo-mock)
11. [Consultar Saldo Individual](#11-consultar-saldo-individual)
12. [Consultar Saldo Total](#12-consultar-saldo-total)

---

## 1. Health Check

### `GET /health`
Verifica que la API esté funcionando correctamente.

**Request:**
```bash
curl http://localhost:5002/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "metodos-pago-api",
  "rabbitmq_connected": true
}
```

**Códigos de Estado:**
- `200 OK` - Servicio funcionando

**Uso:**
- Monitorear disponibilidad del servicio
- Health checks en Kubernetes/Docker
- Verificar conexión RabbitMQ

---

## 2. Crear Método de Pago

### `POST /api/metodos-pago/`
Crea un nuevo método de pago asociado a un usuario.

**Request Body:**
```json
{
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_nombre_titular": "Juan Pérez",
  "f_fecha_expiracion": "2026-12-31",
  "n_numero_tarjeta_completo": "4532015112830366",
  "b_principal": true,
  "n_direccion_facturacion": "Calle 123 #45-67",
  "n_codigo_postal": "110111"
}
```

**Campos Obligatorios:**
- `k_usuario_cc` - Cédula del usuario
- `t_tipo_tarjeta` - CREDITO, DEBITO, PSE, EFECTIVO
- `n_nombre_titular` - Nombre del titular
- `f_fecha_expiracion` - Fecha en formato YYYY-MM-DD
- `n_numero_tarjeta_completo` - Número completo de tarjeta

**Campos Opcionales:**
- `b_principal` - Si es el método principal (default: false)
- `n_direccion_facturacion` - Dirección de facturación
- `n_codigo_postal` - Código postal

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_numero_tarjeta": "**** **** **** 0366",
  "n_nombre_titular": "Juan Pérez",
  "f_fecha_expiracion": "2026-12-31",
  "n_marca": "VISA",
  "b_principal": true,
  "b_activo": true,
  "f_fecha_registro": "2025-11-07T02:00:00",
  "n_direccion_facturacion": "Calle 123 #45-67",
  "n_codigo_postal": "110111",
  "v_saldo": 0
}
```

**Validaciones Automáticas:**
- ✅ Algoritmo de Luhn para validar número
- ✅ Detección automática de marca
- ✅ Enmascaramiento del número
- ✅ Si es el primer método, se marca como principal
- ✅ Si se marca como principal, desmarca los demás

**Evento RabbitMQ:**
```json
{
  "event_type": "METODO_PAGO_CREATED",
  "routing_key": "metodo_pago.created"
}
```

**Errores Comunes:**
```json
// 400 Bad Request - Tarjeta inválida
{
  "detail": "Número de tarjeta inválido"
}

// 400 Bad Request - Usuario tiene 5 métodos
{
  "detail": "No se pueden registrar más de 5 métodos de pago por usuario"
}
```

**Ejemplo con curl:**
```bash
curl -X POST "http://localhost:5002/api/metodos-pago/" \
  -H "Content-Type: application/json" \
  -d '{
    "k_usuario_cc": "1234567890",
    "t_tipo_tarjeta": "CREDITO",
    "n_nombre_titular": "Juan Pérez",
    "f_fecha_expiracion": "2026-12-31",
    "n_numero_tarjeta_completo": "4532015112830366",
    "b_principal": true,
    "n_direccion_facturacion": "Calle 123 #45-67",
    "n_codigo_postal": "110111"
  }'
```

**Ejemplo con PowerShell:**
```powershell
$body = @{
    k_usuario_cc = "1234567890"
    t_tipo_tarjeta = "CREDITO"
    n_nombre_titular = "Juan Pérez"
    f_fecha_expiracion = "2026-12-31"
    n_numero_tarjeta_completo = "4532015112830366"
    b_principal = $true
    n_direccion_facturacion = "Calle 123 #45-67"
    n_codigo_postal = "110111"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5002/api/metodos-pago/" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

---

## 3. Listar Métodos de Pago

### `GET /api/metodos-pago/usuario/{usuario_cc}`
Lista todos los métodos de pago activos de un usuario.

**Parámetros URL:**
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl http://localhost:5002/api/metodos-pago/usuario/1234567890
```

**Response:**
```json
[
  {
    "k_metodo_pago": 1,
    "k_usuario_cc": "1234567890",
    "t_tipo_tarjeta": "CREDITO",
    "n_numero_tarjeta": "**** **** **** 0366",
    "n_nombre_titular": "Juan Pérez",
    "f_fecha_expiracion": "2026-12-31",
    "n_marca": "VISA",
    "b_principal": true,
    "b_activo": true,
    "f_fecha_registro": "2025-11-07T02:00:00",
    "n_direccion_facturacion": "Calle 123 #45-67",
    "n_codigo_postal": "110111",
    "v_saldo": 50000
  },
  {
    "k_metodo_pago": 2,
    "k_usuario_cc": "1234567890",
    "t_tipo_tarjeta": "DEBITO",
    "n_numero_tarjeta": "**** **** **** 1234",
    "n_nombre_titular": "Juan Pérez",
    "f_fecha_expiracion": "2025-08-31",
    "n_marca": "MASTERCARD",
    "b_principal": false,
    "b_activo": true,
    "f_fecha_registro": "2025-11-07T03:00:00",
    "n_direccion_facturacion": "Carrera 7 #10-20",
    "n_codigo_postal": "110111",
    "v_saldo": 25000
  }
]
```

**Características:**
- Solo muestra métodos activos (`b_activo = true`)
- Ordenados por fecha de registro
- Incluye saldo actual de cada método

**Si no hay métodos:**
```json
[]
```

---

## 4. Obtener Método Específico

### `GET /api/metodos-pago/{id}/usuario/{usuario_cc}`
Obtiene los detalles de un método de pago específico.

**Parámetros URL:**
- `id` - ID del método de pago (integer)
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl http://localhost:5002/api/metodos-pago/1/usuario/1234567890
```

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_numero_tarjeta": "**** **** **** 0366",
  "n_nombre_titular": "Juan Pérez",
  "f_fecha_expiracion": "2026-12-31",
  "n_marca": "VISA",
  "b_principal": true,
  "b_activo": true,
  "f_fecha_registro": "2025-11-07T02:00:00",
  "n_direccion_facturacion": "Calle 123 #45-67",
  "n_codigo_postal": "110111",
  "v_saldo": 50000
}
```

**Errores:**
```json
// 404 Not Found
{
  "detail": "Método de pago no encontrado"
}
```

**Seguridad:**
- Valida que el método pertenezca al usuario
- No muestra métodos de otros usuarios

---

## 5. Obtener Método Principal

### `GET /api/metodos-pago/usuario/{usuario_cc}/principal`
Obtiene el método de pago marcado como principal del usuario.

**Parámetros URL:**
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl http://localhost:5002/api/metodos-pago/usuario/1234567890/principal
```

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_numero_tarjeta": "**** **** **** 0366",
  "n_nombre_titular": "Juan Pérez",
  "f_fecha_expiracion": "2026-12-31",
  "n_marca": "VISA",
  "b_principal": true,
  "b_activo": true,
  "f_fecha_registro": "2025-11-07T02:00:00",
  "n_direccion_facturacion": "Calle 123 #45-67",
  "n_codigo_postal": "110111",
  "v_saldo": 50000
}
```

**Casos Especiales:**
```json
// 404 Not Found - Usuario sin métodos de pago
{
  "detail": "El usuario no tiene un método de pago principal"
}
```

**Uso Típico:**
- Seleccionar método predeterminado para pagos
- Mostrar tarjeta preferida en UI
- Procesamiento automático de pagos

---

## 6. Actualizar Método de Pago

### `PUT /api/metodos-pago/{id}/usuario/{usuario_cc}`
Actualiza información de un método de pago existente.

**Parámetros URL:**
- `id` - ID del método de pago (integer)
- `usuario_cc` - Cédula del usuario (string)

**Request Body:**
```json
{
  "n_nombre_titular": "Juan Pérez Actualizado",
  "n_direccion_facturacion": "Nueva Dirección #123",
  "n_codigo_postal": "110222"
}
```

**Campos Actualizables:**
- `n_nombre_titular` - Nombre del titular
- `n_direccion_facturacion` - Dirección
- `n_codigo_postal` - Código postal

**Campos NO Actualizables:**
- ❌ Número de tarjeta (seguridad)
- ❌ Tipo de tarjeta
- ❌ Fecha de expiración
- ❌ Marca
- ❌ Usuario CC

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "CREDITO",
  "n_numero_tarjeta": "**** **** **** 0366",
  "n_nombre_titular": "Juan Pérez Actualizado",
  "f_fecha_expiracion": "2026-12-31",
  "n_marca": "VISA",
  "b_principal": true,
  "b_activo": true,
  "f_fecha_registro": "2025-11-07T02:00:00",
  "n_direccion_facturacion": "Nueva Dirección #123",
  "n_codigo_postal": "110222",
  "v_saldo": 50000
}
```

**Evento RabbitMQ:**
```json
{
  "event_type": "METODO_PAGO_UPDATED",
  "routing_key": "metodo_pago.updated"
}
```

**Ejemplo con curl:**
```bash
curl -X PUT "http://localhost:5002/api/metodos-pago/1/usuario/1234567890" \
  -H "Content-Type: application/json" \
  -d '{
    "n_nombre_titular": "Juan Pérez Actualizado",
    "n_codigo_postal": "110222"
  }'
```

---

## 7. Eliminar Método de Pago

### `DELETE /api/metodos-pago/{id}/usuario/{usuario_cc}`
Elimina (desactiva) un método de pago.

**Parámetros URL:**
- `id` - ID del método de pago (integer)
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl -X DELETE http://localhost:5002/api/metodos-pago/1/usuario/1234567890
```

**Response:**
```json
{
  "mensaje": "Método de pago eliminado correctamente"
}
```

**Comportamiento:**
- ✅ Soft delete (`b_activo = false`)
- ✅ No elimina físicamente el registro
- ✅ Si era principal, asigna otro como principal
- ✅ Mantiene historial

**Evento RabbitMQ:**
```json
{
  "event_type": "METODO_PAGO_DELETED",
  "routing_key": "metodo_pago.deleted"
}
```

**Errores:**
```json
// 404 Not Found
{
  "detail": "Método de pago no encontrado"
}

// 400 Bad Request - Es el único método activo
{
  "detail": "No se puede eliminar el único método de pago activo"
}
```

---

## 8. Establecer como Principal

### `PATCH /api/metodos-pago/{id}/usuario/{usuario_cc}/principal`
Marca un método de pago como principal.

**Parámetros URL:**
- `id` - ID del método de pago (integer)
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl -X PATCH http://localhost:5002/api/metodos-pago/2/usuario/1234567890/principal
```

**Response:**
```json
{
  "k_metodo_pago": 2,
  "k_usuario_cc": "1234567890",
  "t_tipo_tarjeta": "DEBITO",
  "n_numero_tarjeta": "**** **** **** 1234",
  "n_nombre_titular": "Juan Pérez",
  "f_fecha_expiracion": "2025-08-31",
  "n_marca": "MASTERCARD",
  "b_principal": true,
  "b_activo": true,
  "f_fecha_registro": "2025-11-07T03:00:00",
  "n_direccion_facturacion": "Carrera 7 #10-20",
  "n_codigo_postal": "110111",
  "v_saldo": 25000
}
```

**Comportamiento:**
- ✅ Desmarca el método principal anterior
- ✅ Marca este como principal
- ✅ Solo un método puede ser principal

**Evento RabbitMQ:**
```json
{
  "event_type": "METODO_PAGO_SET_PRINCIPAL",
  "routing_key": "metodo_pago.principal"
}
```

---

## 9. Validar Número de Tarjeta

### `POST /api/metodos-pago/validar`
Valida un número de tarjeta usando el algoritmo de Luhn.

**Request Body:**
```json
{
  "numeroTarjeta": "4532015112830366"
}
```

**Response - Tarjeta Válida:**
```json
{
  "valido": true
}
```

**Response - Tarjeta Inválida:**
```json
{
  "valido": false
}
```

**Validaciones:**
- ✅ Algoritmo de Luhn
- ✅ Longitud (13-19 dígitos)
- ✅ Solo dígitos numéricos

**Números de Prueba Válidos:**
```
VISA:        4532015112830366
MasterCard:  5425233430109903
AMEX:        374245455400126
Diners:      36111111111111
```

**Ejemplo con curl:**
```bash
curl -X POST "http://localhost:5002/api/metodos-pago/validar" \
  -H "Content-Type: application/json" \
  -d '{"numeroTarjeta": "4532015112830366"}'
```

**Uso:**
- Validar tarjetas antes de crear
- Formularios en tiempo real
- Pre-validación frontend

---

## 10. Recargar Saldo (MOCK)

### `POST /api/metodos-pago/recarga`
Recarga saldo en un método de pago (simulación).

**Request Body:**
```json
{
  "k_metodo_pago": 1,
  "monto": 50000,
  "descripcion": "Recarga mensual"
}
```

**Campos:**
- `k_metodo_pago` - ID del método (requerido)
- `monto` - Monto a recargar (requerido, min: 1000, max: 5000000)
- `descripcion` - Descripción opcional

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "monto_recargado": 50000,
  "saldo_anterior": 0,
  "saldo_nuevo": 50000,
  "n_marca": "VISA",
  "n_numero_tarjeta": "**** **** **** 0366",
  "fecha_recarga": "2025-11-07T02:47:23.368725",
  "descripcion": "Recarga mensual",
  "mensaje": "Recarga exitosa de $50,000. Nuevo saldo: $50,000"
}
```

**Validaciones:**
- ✅ Monto mínimo: $1,000
- ✅ Monto máximo: $5,000,000
- ✅ Método debe existir y estar activo
- ✅ Actualización inmediata

**Evento RabbitMQ:**
```json
{
  "event_type": "SALDO_RECARGADO",
  "routing_key": "metodo_pago.recarga"
}
```

**Errores:**
```json
// 400 Bad Request - Monto bajo
{
  "detail": [
    {
      "type": "value_error",
      "msg": "Value error, El monto mínimo de recarga es $1,000"
    }
  ]
}

// 400 Bad Request - Monto alto
{
  "detail": [
    {
      "type": "value_error",
      "msg": "Value error, El monto máximo de recarga es $5,000,000"
    }
  ]
}

// 404 Not Found
{
  "detail": "Método de pago no encontrado"
}

// 400 Bad Request
{
  "detail": "El método de pago está inactivo"
}
```

**Ejemplo con PowerShell:**
```powershell
$recarga = @{
    k_metodo_pago = 1
    monto = 50000
    descripcion = "Recarga inicial"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:5002/api/metodos-pago/recarga" `
  -Method Post `
  -ContentType "application/json" `
  -Body $recarga
```

---

## 11. Consultar Saldo Individual

### `GET /api/metodos-pago/saldo/{metodo_pago_id}`
Consulta el saldo de un método de pago específico.

**Parámetros URL:**
- `metodo_pago_id` - ID del método de pago (integer)

**Request:**
```bash
curl http://localhost:5002/api/metodos-pago/saldo/1
```

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "saldo_actual": 175000,
  "n_marca": "VISA",
  "n_numero_tarjeta": "**** **** **** 0366",
  "ultima_actualizacion": "2025-11-07T02:47:07.123456"
}
```

**Uso:**
- Verificar saldo antes de transacción
- Mostrar saldo en UI
- Validar saldo suficiente

---

## 12. Consultar Saldo Total

### `GET /api/metodos-pago/usuario/{usuario_cc}/saldo-total`
Consulta el saldo total de todos los métodos activos del usuario.

**Parámetros URL:**
- `usuario_cc` - Cédula del usuario (string)

**Request:**
```bash
curl http://localhost:5002/api/metodos-pago/usuario/1234567890/saldo-total
```

**Response:**
```json
{
  "k_usuario_cc": "1234567890",
  "saldo_total": 200000,
  "cantidad_metodos": 2,
  "metodos_pago": [
    {
      "k_metodo_pago": 1,
      "n_marca": "VISA",
      "n_numero_tarjeta": "**** **** **** 0366",
      "saldo": 175000,
      "b_principal": true
    },
    {
      "k_metodo_pago": 2,
      "n_marca": "MASTERCARD",
      "n_numero_tarjeta": "**** **** **** 9903",
      "saldo": 25000,
      "b_principal": false
    }
  ]
}
```

**Uso:**
- Dashboard del usuario
- Verificar poder adquisitivo total
- Resumen financiero

---

## 🔒 Seguridad

### **Datos Sensibles**
```
⚠️  IMPORTANTE: Este es un sistema MOCK para desarrollo

En PRODUCCIÓN se debe:
✅ NO almacenar números de tarjeta completos
✅ Usar tokenización (Stripe, PayU)
✅ Implementar JWT/OAuth
✅ Usar HTTPS
✅ Cumplir PCI DSS
✅ Encriptar datos en BD
```

### **Rate Limiting**
Considera implementar:
- Límite de recargas por hora
- Límite de creación de métodos
- Throttling por IP

---

## 🎯 Flujos Comunes

### **Flujo 1: Nuevo Usuario**
```
1. POST /api/metodos-pago/           → Crear primer método
2. GET /api/metodos-pago/usuario/{cc} → Listar (verá 1)
3. POST /api/metodos-pago/recarga    → Recargar saldo
```

### **Flujo 2: Agregar Método Adicional**
```
1. GET /api/metodos-pago/usuario/{cc}       → Ver métodos actuales
2. POST /api/metodos-pago/validar           → Validar nueva tarjeta
3. POST /api/metodos-pago/                  → Crear método
4. PATCH /api/metodos-pago/{id}/principal   → Establecer como principal (opcional)
```

### **Flujo 3: Antes de Hacer un Pago**
```
1. GET /api/metodos-pago/usuario/{cc}/principal → Obtener método principal
2. GET /api/metodos-pago/saldo/{id}             → Verificar saldo
3. (Si insuficiente) POST /api/metodos-pago/recarga → Recargar
```

### **Flujo 4: Gestión de Métodos**
```
1. GET /api/metodos-pago/usuario/{cc}       → Listar todos
2. PUT /api/metodos-pago/{id}/usuario/{cc}  → Actualizar datos
3. DELETE /api/metodos-pago/{id}/usuario/{cc} → Eliminar (si es necesario)
```

---

## 📊 Códigos de Estado HTTP

| Código | Significado | Cuándo |
|--------|-------------|--------|
| **200** | OK | Operación exitosa (GET, PUT, PATCH) |
| **201** | Created | Método creado exitosamente (POST) |
| **400** | Bad Request | Datos inválidos, validación fallida |
| **404** | Not Found | Método/Usuario no encontrado |
| **500** | Internal Error | Error del servidor |

---

## 🐰 Eventos RabbitMQ

Todos los endpoints que modifican datos publican eventos:

| Endpoint | Evento | Routing Key |
|----------|--------|-------------|
| POST /api/metodos-pago/ | METODO_PAGO_CREATED | metodo_pago.created |
| PUT /api/metodos-pago/{id} | METODO_PAGO_UPDATED | metodo_pago.updated |
| DELETE /api/metodos-pago/{id} | METODO_PAGO_DELETED | metodo_pago.deleted |
| PATCH /api/metodos-pago/{id}/principal | METODO_PAGO_SET_PRINCIPAL | metodo_pago.principal |
| POST /api/metodos-pago/recarga | SALDO_RECARGADO | metodo_pago.recarga |

**Ver eventos en:**
- http://localhost:15672 (urbanride/urbanride2024)
- Exchange: `urbanride.payments`

---

## 🧪 Testing con Swagger UI

### **Cómo Usar:**

1. **Abrir Swagger UI:**
   ```
   http://localhost:5002/docs
   ```

2. **Seleccionar un endpoint:**
   - Click en el endpoint que quieres probar

3. **Click en "Try it out":**
   - Habilita la edición de parámetros

4. **Llenar parámetros:**
   - Request body (si aplica)
   - Path parameters
   - Query parameters

5. **Click en "Execute":**
   - Envía la petición

6. **Ver respuesta:**
   - Response body
   - Response headers
   - HTTP status code

---

## 💡 Tips y Mejores Prácticas

### **1. Números de Tarjeta de Prueba**
```
VISA:        4532015112830366
MasterCard:  5425233430109903
AMEX:        374245455400126
```

### **2. Manejo de Errores**
Siempre verifica el código de estado:
```javascript
if (response.status === 404) {
  console.log("Método no encontrado");
} else if (response.status === 400) {
  console.log("Datos inválidos:", response.data.detail);
}
```

### **3. Formato de Fechas**
Siempre usar formato ISO: `YYYY-MM-DD`
```json
"f_fecha_expiracion": "2026-12-31"
```

### **4. Montos**
Usar enteros (centavos):
```json
{
  "monto": 50000  // $50,000 pesos
}
```

---

## 📚 Recursos Adicionales

- **Swagger UI:** http://localhost:5002/docs
- **ReDoc:** http://localhost:5002/redoc
- **OpenAPI JSON:** http://localhost:5002/openapi.json

---

**¡Ahora tienes una guía completa de todos los endpoints de la API!** 🎉
