# Implementación de Cobro basado en Notificaciones SSE

## Visión General

El sistema de notificaciones SSE (Server-Sent Events) permite recibir eventos en tiempo real del backend sobre el ciclo de vida de los viajes:
- **START_TRAVEL**: El viaje ha iniciado (usuario desbloqueó la bicicleta)
- **END_TRAVEL**: El viaje ha finalizado (usuario regresó la bicicleta)
- **EXPIRED_TRAVEL**: El viaje expiró sin completarse

## Arquitectura Actual

### 1. Servicio SSE (`travelNotifications.ts`)
**Ubicación**: `/frontend/client/src/services/travelNotifications.ts`

**Responsabilidades**:
- Conectar a endpoint SSE: `GET /notification/sse/connect?uid={userId}`
- Recibir eventos SSE con notificaciones de viajes
- Detectar tipos de eventos: START_TRAVEL, END_TRAVEL, EXPIRED_TRAVEL
- Mostrar notificaciones visuales al usuario
- Reconectar automáticamente en caso de desconexión
- Desconectar al cerrar sesión

**Estructura de eventos recibidos**:
```json
{
  "message": "START_TRAVEL: Viaje iniciado para usuario X",
  "type": "START_TRAVEL"
}
```

### 2. Componente de Confirmación (`ConfirmationComponent.vue`)
**Ubicación**: `/frontend/client/src/components/reservation/ConfirmationComponent.vue`

**Responsabilidades**:
- Mostrar código de matrícula de la bicicleta para desbloqueo
- Botón de desbloqueo con validación
- Mostrar modal de éxito con cobro estimado después de END_TRAVEL

## Implementación del Cobro

### Paso 1: Escuchar Notificaciones SSE en ConfirmationComponent

En `ConfirmationComponent.vue`, agregar watcher para eventos SSE:

```vue
<script setup lang="ts">
import { useTripStore } from '@/stores/trip'; // Importar el store de notificaciones SSE

const tripStore = useTripStore();

// Escuchar cambios en la notificación
watch(() => tripStore.notification, async (newNotification) => {
  if (!newNotification) return;
  
  console.log('[ConfirmationComponent] Notificación SSE recibida:', newNotification);
  
  if (newNotification.type === 'START_TRAVEL') {
    handleStartTravel(newNotification);
  } else if (newNotification.type === 'END_TRAVEL') {
    handleEndTravel(newNotification);
  } else if (newNotification.type === 'EXPIRED_TRAVEL') {
    handleExpiredTravel(newNotification);
  }
  
  // Cerrar la notificación visual después de procesarla
  tripStore.closeNotification();
}, { deep: true });

// Funciones para manejar cada tipo de evento
function handleStartTravel(notification) {
  console.log('[ConfirmationComponent] Viaje iniciado:', notification.message);
  // Ya se muestra el desbloqueado en la UI
  // Esta es una confirmación del backend que el viaje comenzó
  showStartTravelNotification();
}

async function handleEndTravel(notification) {
  console.log('[ConfirmationComponent] Viaje finalizado:', notification.message);
  
  // AQUÍ ES DONDE SE REALIZA EL COBRO
  try {
    const userUid = getAuth().currentUser?.uid;
    const travelId = extractTravelIdFromMessage(notification.message);
    
    // Llamar al endpoint de cobro del backend
    const chargeResponse = await chargeTrip(userUid, travelId);
    
    console.log('[ConfirmationComponent] Cobro procesado:', chargeResponse);
    
    // Actualizar balance local
    await refreshUserBalance();
    
    // Mostrar modal de éxito con costo
    showSuccessModal(chargeResponse.amount, chargeResponse.currency);
    
  } catch (error) {
    console.error('[ConfirmationComponent] Error procesando cobro:', error);
    showErrorModal('Error al procesar el pago');
  }
}

function handleExpiredTravel(notification) {
  console.log('[ConfirmationComponent] Viaje expirado:', notification.message);
  
  // El viaje expiró sin completarse
  // Se aplica penalización automática si es necesario
  showExpiredTravelModal();
}
</script>
```

### Paso 2: Funciones Auxiliares para el Cobro

Agregar en `ConfirmationComponent.vue`:

```typescript
/**
 * Procesa el cobro del viaje
 * Endpoint sugerido: POST /viaje-service/api/viajes/{travelId}/charge
 */
async function chargeTrip(userUid: string, travelId: string) {
  const auth = getAuth();
  const token = await auth.currentUser?.getIdToken();
  
  const response = await fetch(
    `${import.meta.env.VITE_API_URL || 'http://localhost:8090'}/viaje-service/api/viajes/${travelId}/charge`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify({
        userUid,
        travelId,
        timestamp: Date.now(),
      }),
    }
  );
  
  if (!response.ok) {
    throw new Error(`Error en cobro: ${response.statusText}`);
  }
  
  return await response.json();
}

/**
 * Extrae el ID del viaje del mensaje SSE
 * Formato esperado: "END_TRAVEL: Travel ID: {id}, User: {uid}"
 */
function extractTravelIdFromMessage(message: string): string {
  const match = message.match(/Travel ID:\s*(\w+)/i);
  return match ? match[1] : '';
}

/**
 * Actualiza el balance del usuario después del cobro
 */
async function refreshUserBalance() {
  const paymentStore = usePaymentStore();
  const userUid = getAuth().currentUser?.uid;
  
  if (userUid) {
    await paymentStore.fetchBalance(userUid);
    console.log('[ConfirmationComponent] Balance actualizado después del cobro');
  }
}

/**
 * Muestra modal de éxito con detalles del cobro
 */
function showSuccessModal(amount: number, currency: string) {
  showSuccessModal.value = true;
  tripDetails.value = {
    ...tripDetails.value,
    estimatedCost: `$${amount.toFixed(2)} ${currency}`,
  };
}

/**
 * Muestra modal de error
 */
function showErrorModal(message: string) {
  alert(message); // Reemplazar con componente de notificación elegante
}

/**
 * Muestra notificación de viaje expirado
 */
function showExpiredTravelModal() {
  alert('Tu viaje expiró sin completarse. Se aplicó una penalización a tu cuenta.');
}

/**
 * Muestra notificación de inicio de viaje
 */
function showStartTravelNotification() {
  console.log('[ConfirmationComponent] Viaje confirmado como iniciado en backend');
  // Opcional: Mostrar toast de confirmación
}
```

### Paso 3: Integración en el Backend

**Endpoint sugerido para el cobro** (viaje-service):

```java
@PostMapping("/api/viajes/{id}/charge")
public ResponseEntity<ChargeResponse> chargeTrip(
  @PathVariable String id,
  @RequestBody ChargeRequest request,
  @RequestHeader("Authorization") String token
) {
  // 1. Validar autenticación
  String userUid = extractUidFromToken(token);
  
  // 2. Obtener datos del viaje
  Travel travel = travelRepository.findById(id).orElseThrow();
  
  // 3. Calcular costo final
  long durationMinutes = calculateDuration(travel.getStartTime(), travel.getEndTime());
  double baseCost = travel.getRideType().equals("LONG_TRIP") ? 25000 : 17500;
  double costPerMinute = travel.getRideType().equals("LONG_TRIP") ? 1000 : 250;
  double totalCost = baseCost + (durationMinutes * costPerMinute);
  
  // 4. Procesar cobro en payment-service
  PaymentResponse paymentResp = paymentService.charge(userUid, totalCost, "COP");
  
  if (!paymentResp.isSuccess()) {
    return ResponseEntity.status(402).body(new ChargeResponse(false, "Fondos insuficientes"));
  }
  
  // 5. Actualizar estado del viaje
  travel.setStatus("CHARGED");
  travel.setChargeCost(totalCost);
  travel.setChargeTime(new Date());
  travelRepository.save(travel);
  
  // 6. Registrar en auditoría
  auditLog.log(userUid, "TRIP_CHARGED", travel.getId(), totalCost);
  
  // 7. Enviar email de factura
  notificationService.sendTripReceipt(travel);
  
  return ResponseEntity.ok(new ChargeResponse(true, totalCost, "COP"));
}
```

### Paso 4: Mejoras Opcionales

#### 4.1 Retry automático para pagos fallidos

```typescript
/**
 * Reintentar cobro en caso de fallo
 */
async function chargeWithRetry(userUid: string, travelId: string, maxAttempts: number = 3) {
  let lastError;
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      console.log(`[ConfirmationComponent] Intento de cobro ${attempt}/${maxAttempts}`);
      return await chargeTrip(userUid, travelId);
    } catch (error) {
      lastError = error;
      console.error(`[ConfirmationComponent] Intento ${attempt} fallido:`, error);
      
      // Esperar antes de reintentar (backoff exponencial)
      const delayMs = Math.pow(2, attempt - 1) * 1000; // 1s, 2s, 4s
      await new Promise(resolve => setTimeout(resolve, delayMs));
    }
  }
  
  throw lastError;
}
```

#### 4.2 Notificación de cobro pendiente

```typescript
/**
 * Si el cobro falla, registrar como pendiente para reintentar
 */
async function handleFailedCharge(userUid: string, travelId: string, error: Error) {
  try {
    // Guardar en base de datos como "PENDING_CHARGE"
    await paymentStore.addPendingCharge({
      userUid,
      travelId,
      timestamp: Date.now(),
      reason: error.message,
    });
    
    console.log('[ConfirmationComponent] Cobro registrado como pendiente');
    
    // Mostrar modal informando al usuario
    showPendingChargeModal();
    
  } catch (err) {
    console.error('[ConfirmationComponent] Error registrando cobro pendiente:', err);
  }
}
```

## Flujo Completo de Cobro

```
1. Usuario inicia viaje
   ↓
2. Backend envía SSE START_TRAVEL
   ↓
3. ConfirmationComponent muestra confirmación
   ↓
4. Usuario devuelve la bicicleta
   ↓
5. Backend envía SSE END_TRAVEL
   ↓
6. ConfirmationComponent detecta END_TRAVEL
   ↓
7. Llamar a /viajes/{id}/charge
   ↓
8. Backend calcula costo final
   ↓
9. Backend procesa pago en payment-service
   ↓
10. Backend actualiza estado de viaje a CHARGED
   ↓
11. Respuesta al frontend con costo final
   ↓
12. ConfirmationComponent muestra modal de éxito
   ↓
13. Email de factura enviado al usuario
```

## Variables de Entorno Requeridas

En `.env` del frontend:
```env
VITE_API_URL=http://localhost:8090
```

## Testing

### Test Local sin Backend Real

Para testing sin un backend real, modificar `handleEndTravel`:

```typescript
async function handleEndTravel(notification) {
  if (process.env.NODE_ENV === 'development') {
    // Modo desarrollo: simular cobro
    const mockAmount = 85000; // $85k COP
    showSuccessModal(mockAmount, 'COP');
    return;
  }
  
  // Modo producción: cobrar de verdad
  try {
    const chargeResponse = await chargeTrip(userUid, travelId);
    showSuccessModal(chargeResponse.amount, chargeResponse.currency);
  } catch (error) {
    console.error('Error en cobro:', error);
  }
}
```

## Resumen de Cambios Necesarios

### Frontend
- [ ] Agregar watchers en `ConfirmationComponent.vue` para eventos SSE
- [ ] Implementar funciones de cobro `chargeTrip()`, `chargeWithRetry()`
- [ ] Actualizar modales de éxito/error
- [ ] Refrescar balance después de cobro exitoso

### Backend (viaje-service)
- [ ] Crear endpoint `POST /api/viajes/{id}/charge`
- [ ] Calcular costo final basado en duración
- [ ] Integrar con payment-service para procesar pago
- [ ] Actualizar estado del viaje a CHARGED
- [ ] Disparar evento de email de factura

### Backend (notification-service)
- [ ] Enviar SSE START_TRAVEL cuando usuario desbloquee
- [ ] Enviar SSE END_TRAVEL cuando usuario devuelva bicicleta
- [ ] Enviar SSE EXPIRED_TRAVEL en caso de expiración

### Backend (payment-service)
- [ ] Endpoint para procesar cobro: `POST /api/charges`
- [ ] Validar fondos suficientes
- [ ] Registrar transacción en BD
