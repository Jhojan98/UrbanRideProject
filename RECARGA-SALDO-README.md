# 💳 Sistema de Recarga de Saldo - UrbanRide

## 📋 Historia de Usuario

**Como** usuario del sistema de bicicletas UrbanRide  
**Quiero** asociar una tarjeta y recargar saldo  
**Para** poder usar el servicio de alquiler de bicicletas

### Criterios de Aceptación

**Given**: Estoy logueado en el sistema  
**When**: Ingreso los datos de mi tarjeta o monto de recarga  
**Then**: El sistema muestra mi nuevo saldo actualizado (mock)

---

## ✅ Funcionalidades Implementadas

### 1. **Recarga de Saldo (MOCK)** 💰
- Endpoint POST para recargar saldo
- Validación de montos mínimos y máximos
- Actualización inmediata del saldo
- Eventos en RabbitMQ para notificaciones

### 2. **Consulta de Saldo Individual** 🔍
- Endpoint GET para consultar saldo de un método de pago
- Información detallada del método

### 3. **Consulta de Saldo Total** 📊
- Endpoint GET para obtener saldo total del usuario
- Listado de todos los métodos de pago activos con sus saldos

---

## 🚀 API Endpoints

### **POST /api/metodos-pago/recarga**
Recarga de saldo MOCK para método de pago

**Request:**
```json
{
  "k_metodo_pago": 1,
  "monto": 50000,
  "descripcion": "Recarga mensual"
}
```

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
- ✅ Monto mínimo: **$1,000**
- ✅ Monto máximo: **$5,000,000**
- ✅ Método de pago debe existir y estar activo

---

### **GET /api/metodos-pago/saldo/{metodo_pago_id}**
Consulta el saldo actual de un método de pago

**Response:**
```json
{
  "k_metodo_pago": 1,
  "k_usuario_cc": "1234567890",
  "saldo_actual": 175000,
  "n_marca": "MASTERCARD",
  "n_numero_tarjeta": "**** **** **** 9903",
  "ultima_actualizacion": "2025-11-07T02:47:07.123456"
}
```

---

### **GET /api/metodos-pago/usuario/{usuario_cc}/saldo-total**
Consulta el saldo total de todos los métodos de pago del usuario

**Response:**
```json
{
  "k_usuario_cc": "1234567890",
  "saldo_total": 175000,
  "cantidad_metodos": 1,
  "metodos_pago": [
    {
      "k_metodo_pago": 4,
      "n_marca": "MASTERCARD",
      "n_numero_tarjeta": "**** **** **** 9903",
      "saldo": 175000,
      "b_principal": true
    }
  ]
}
```

---

## 🐰 Eventos RabbitMQ

### Evento: **SALDO_RECARGADO**
**Routing Key:** `metodo_pago.recarga`

**Estructura del Evento:**
```json
{
  "event_type": "SALDO_RECARGADO",
  "timestamp": "1699311234.567",
  "data": {
    "k_metodo_pago": 1,
    "k_usuario_cc": "1234567890",
    "monto_recargado": 50000,
    "saldo_anterior": 0,
    "saldo_nuevo": 50000,
    "n_marca": "VISA",
    "fecha_recarga": "2025-11-07T02:47:23.368725"
  }
}
```

### Usos del Evento
Este evento puede ser consumido por:
- 📧 **Servicio de Notificaciones** - Enviar email/SMS de confirmación
- 📊 **Servicio de Analytics** - Registrar estadísticas de recargas
- 🔍 **Servicio de Auditoría** - Log de todas las transacciones
- 💼 **Servicio de Facturación** - Generar comprobantes

---

## 🧪 Pruebas

### Ejecutar Script de Pruebas
```powershell
.\test-recarga-saldo.ps1
```

### Resultados Esperados
```
✅ Creación de método de pago con saldo inicial ($0)
✅ Consulta de saldo individual
✅ Recarga de saldo (3 recargas exitosas)
   - Primera recarga: $50,000
   - Segunda recarga: $25,000
   - Tercera recarga: $100,000
   - Saldo final: $175,000
✅ Consulta de saldo total del usuario
✅ Validaciones de monto mínimo y máximo
✅ Validación de método de pago inexistente
```

---

## 📊 Flujo de Recarga

```
┌──────────────┐
│   Usuario    │
│  (Logueado)  │
└──────┬───────┘
       │
       │ 1. POST /recarga
       │    {monto: 50000}
       ▼
┌──────────────────┐
│  FastAPI Server  │
│                  │
│  ✓ Validar monto │
│  ✓ Validar método│
│  ✓ Actualizar DB │
└────────┬─────────┘
         │
         ├─────────────────┐
         │                 │
         ▼                 ▼
   ┌──────────┐    ┌──────────────┐
   │PostgreSQL│    │   RabbitMQ   │
   │ Guardar  │    │  Publicar    │
   │ Saldo    │    │  Evento      │
   └──────────┘    └──────┬───────┘
                          │
                          ▼
                  ┌───────────────────┐
                  │    Consumers      │
                  │                   │
                  │ • Notificaciones  │
                  │ • Analytics       │
                  │ • Auditoría       │
                  └───────────────────┘
```

---

## 💡 Ejemplos de Uso

### Ejemplo 1: Recarga Simple
```bash
curl -X POST "http://localhost:5002/api/metodos-pago/recarga" \
  -H "Content-Type: application/json" \
  -d '{
    "k_metodo_pago": 1,
    "monto": 50000,
    "descripcion": "Recarga inicial"
  }'
```

### Ejemplo 2: Consultar Saldo
```bash
curl -X GET "http://localhost:5002/api/metodos-pago/saldo/1"
```

### Ejemplo 3: Saldo Total del Usuario
```bash
curl -X GET "http://localhost:5002/api/metodos-pago/usuario/1234567890/saldo-total"
```

---

## 🔒 Validaciones y Seguridad

### Validaciones Implementadas
| Validación | Regla | Mensaje de Error |
|------------|-------|------------------|
| Monto mínimo | >= $1,000 | "El monto mínimo de recarga es $1,000" |
| Monto máximo | <= $5,000,000 | "El monto máximo de recarga es $5,000,000" |
| Método existente | Debe existir en BD | "Método de pago no encontrado" |
| Método activo | b_activo = true | "El método de pago está inactivo" |
| Monto positivo | > 0 | "El monto debe ser mayor a 0" |

### Consideraciones de Seguridad

⚠️ **IMPORTANTE - SISTEMA MOCK:**
Este es un sistema de recarga **SIMULADA (MOCK)** para desarrollo y pruebas.

**En producción se debe:**
- ✅ Integrar con pasarela de pago real (Stripe, PayU, MercadoPago, etc.)
- ✅ Implementar autenticación JWT
- ✅ Validar identidad del usuario
- ✅ Usar HTTPS
- ✅ Implementar rate limiting
- ✅ Agregar logs de auditoría
- ✅ Encriptar datos sensibles
- ✅ Cumplir con PCI DSS

---

## 🗄️ Base de Datos

### Campo Agregado a la Tabla `metodo_pago`

```sql
ALTER TABLE metodo_pago 
ADD COLUMN v_saldo BIGINT DEFAULT 0;
```

| Campo | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| v_saldo | BIGINT | 0 | Saldo disponible en el método de pago |

### Consulta de Ejemplo
```sql
-- Ver saldo de todos los métodos de un usuario
SELECT 
    k_metodopago,
    n_marca,
    n_numerotarjeta,
    v_saldo,
    b_principal
FROM metodo_pago 
WHERE k_usuario_cc = '1234567890' 
  AND b_activo = true;
```

---

## 📝 Schemas Pydantic

### RecargaSaldoRequest
```python
class RecargaSaldoRequest(BaseModel):
    k_metodo_pago: int
    monto: int  # Min: 1000, Max: 5000000
    descripcion: Optional[str] = None
```

### RecargaSaldoResponse
```python
class RecargaSaldoResponse(BaseModel):
    k_metodo_pago: int
    k_usuario_cc: str
    monto_recargado: int
    saldo_anterior: int
    saldo_nuevo: int
    n_marca: Optional[str]
    n_numero_tarjeta: str
    fecha_recarga: datetime
    descripcion: Optional[str]
    mensaje: str
```

### ConsultaSaldoResponse
```python
class ConsultaSaldoResponse(BaseModel):
    k_metodo_pago: int
    k_usuario_cc: str
    saldo_actual: int
    n_marca: Optional[str]
    n_numero_tarjeta: str
    ultima_actualizacion: Optional[datetime]
```

---

## 🔧 Archivos Modificados/Creados

```
✅ app/models.py              - Campo v_saldo agregado
✅ app/schemas.py             - Schemas de recarga creados
✅ app/routers.py             - 3 endpoints nuevos
✅ app/messaging.py           - Evento SALDO_RECARGADO
✅ test-recarga-saldo.ps1     - Script de pruebas
✅ RECARGA-SALDO-README.md    - Esta documentación
```

---

## 📊 Swagger UI

Documentación interactiva disponible en:
**http://localhost:5002/docs**

Aquí puedes:
- ✅ Probar todos los endpoints
- ✅ Ver schemas completos
- ✅ Ejecutar requests directamente
- ✅ Ver respuestas de ejemplo

---

## 🎯 Casos de Uso

### Caso 1: Usuario Nuevo
1. Usuario crea método de pago (saldo inicial: $0)
2. Usuario recarga $50,000
3. Sistema actualiza saldo a $50,000
4. Usuario puede usar el servicio

### Caso 2: Usuario Recurrente
1. Usuario tiene saldo de $25,000
2. Usuario recarga $100,000
3. Sistema actualiza saldo a $125,000
4. Usuario continúa usando el servicio

### Caso 3: Usuario con Múltiples Métodos
1. Usuario tiene 2 tarjetas:
   - VISA: $50,000
   - MasterCard: $30,000
2. Saldo total: $80,000
3. Usuario puede usar cualquiera

---

## 🚦 Estados del Sistema

### Estado Inicial
```json
{
  "v_saldo": 0,
  "mensaje": "Sin saldo disponible"
}
```

### Después de Recarga
```json
{
  "v_saldo": 50000,
  "mensaje": "Recarga exitosa de $50,000. Nuevo saldo: $50,000"
}
```

### Después de Uso (futuro)
```json
{
  "v_saldo": 42000,
  "mensaje": "Saldo descontado: $8,000. Saldo restante: $42,000"
}
```

---

## ⚡ Integración Futura

### Con Sistema de Viajes
```python
# Cuando el usuario inicia un viaje
@router.post("/viajes/iniciar")
async def iniciar_viaje(viaje_data):
    # Verificar saldo suficiente
    saldo = consultar_saldo(metodo_pago_id)
    if saldo.saldo_actual < tarifa_minima:
        raise HTTPException(400, "Saldo insuficiente")
    
    # Iniciar viaje
    # Descontar saldo al finalizar
```

### Con Sistema de Notificaciones
```python
# Consumer que escucha eventos de recarga
@consumer.on_message("metodo_pago.recarga")
async def on_saldo_recargado(event):
    # Enviar email de confirmación
    await send_email(
        to=event.data.usuario_email,
        subject="Recarga exitosa",
        body=f"Tu saldo de ${event.data.monto_recargado} fue agregado"
    )
```

---

## 🎓 Conclusión

✅ **Sistema de recarga de saldo completamente funcional**

**Cumple con:**
- ✅ Historia de usuario
- ✅ Criterios de aceptación
- ✅ Validaciones necesarias
- ✅ Eventos RabbitMQ
- ✅ Pruebas exitosas
- ✅ Documentación completa

**Próximos pasos sugeridos:**
1. Integrar con pasarela de pago real
2. Implementar consumo de saldo en viajes
3. Agregar historial de recargas
4. Implementar sistema de puntos/descuentos
5. Agregar notificaciones push

---

**Desarrollado para UrbanRide** 🚴  
**Fecha:** 2025-11-07  
**Versión:** 1.0.0
