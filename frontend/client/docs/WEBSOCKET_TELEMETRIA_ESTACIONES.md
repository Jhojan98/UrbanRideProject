# WebSocket - Telemetría de Estaciones en Tiempo Real

## Implementación Completada

Se ha ajustado `StationWebSocketService` para manejar el nuevo tópico de telemetría en tiempo real que proporciona la cantidad de bicicletas eléctricas y mecánicas disponibles.

---

## Cambios Realizados

### 1. **Documentación del Servicio** (Líneas 9-12)
Actualizado el comentario JSDoc para incluir el nuevo tópico:

```typescript
* 3. Subscriptions:
*    - /topic/stations.bulk                           (complete array of stations with slots)
*    - /topic/station.update/user                     (real-time telemetry: availableElectricBikes, availableMechanicBikes)
```

### 2. **Suscripción al Tópico** (Línea 74-75)
Agregada suscripción al nuevo tópico en el método `subscribe()`:

```typescript
// Real-time telemetry: updates and bike availability
this.client.subscribe('/topic/station.update/user', msg => this.handleTelemetry(msg));
```

**Nota:** El tópico unificado `/topic/station.update/user` recibe telemetría en tiempo real de TODAS las estaciones.

### 3. **Método `handleTelemetry()` - Actualizado** (Líneas 78-143)

```typescript
private handleTelemetry(message: IMessage){
  try {
    // Parse telemetry data from /topic/station.update/user
    const telemetry = JSON.parse(message.body) as {
      idStation?: number;
      timestamp?: number;
      availableElectricBikes?: number;
      availableMechanicBikes?: number;
    };

    const idStation = telemetry.idStation;
    if (!idStation) {
      console.warn('[Stations WS] Telemetry sin idStation válido, se ignora. Payload:', telemetry);
      return;
    }

    // Get existing station or skip if not yet loaded
    const cached = this.stationsCache.get(idStation);
    if (!cached) {
      console.warn(`[Stations WS] Telemetry recibido para estación no cacheada (${idStation}), se ignora`);
      return;
    }

    // Update bike counts in cached station
    cached.electric = telemetry.availableElectricBikes ?? cached.electric;
    cached.mechanical = telemetry.availableMechanicBikes ?? cached.mechanical;
    cached.availableElectricBikes = telemetry.availableElectricBikes ?? cached.availableElectricBikes;
    cached.availableMechanicBikes = telemetry.availableMechanicBikes ?? cached.availableMechanicBikes;
    cached.timestamp = new Date(telemetry.timestamp ?? Date.now());

    // Update the marker in factory to reflect changes
    const marker = this.factory.getMarkerById(idStation);
    if (marker) {
      marker.update(cached);
      // Refresh popup content to show updated bike counts
      marker.updatePopupContent();
    }

    if(this.onUpdate){ this.onUpdate(cached, this.factory); }
    console.log(`[Stations WS] Telemetry station ${idStation}: ⚡ ${cached.electric}, ⚙️ ${cached.mechanical}`);
  } catch(e){
    console.error('[Stations WS] Error parsing telemetry:', e, 'payload:', message.body);
  }
}
```

### 4. **Método `handleUpdate()` - Eliminado**

El método `handleUpdate()` ha sido eliminado. Ahora el método `handleTelemetry()` maneja todas las actualizaciones de telemetría en tiempo real.

---

## Flujo de Datos

```
Backend (Estaciones Service)
    ↓
Publica en /topic/station.update/user
{
  "idStation": 123,
  "timestamp": 1764197585935,
  "availableElectricBikes": 10,
  "availableMechanicBikes": 5
}
    ↓
Frontend (WebSocket Client)
    ↓
STOMP recibe en /topic/station.update/user
    ↓
handleTelemetry()
    ├─ Extrae ID de estación del payload
    ├─ Parsea telemetría (eléctricas, mecánicas)
    ├─ Actualiza cache de estaciones
    ├─ Actualiza marcador en mapa
    ├─ REGENERA popup (updatePopupContent)
    └─ Notifica listeners
    ↓
Popup del Mapa Actualizado
    ├─ ⚡ Eléctricas: 10
    └─ ⚙️ Mecánicas: 5
```

---

## Extracción de ID desde el Payload

El ID de la estación se extrae directamente del JSON recibido:

```typescript
const telemetry = JSON.parse(message.body) as {
  idStation?: number;
  timestamp?: number;
  availableElectricBikes?: number;
  availableMechanicBikes?: number;
};

const idStation = telemetry.idStation;
```

---

## Integración con Popups Reactivos

El handler utiliza el método `updatePopupContent()` que agregamos anteriormente en `StationFlyweight`:

```typescript
const marker = this.factory.getMarkerById(idStation);
if (marker) {
  marker.update(cached);
  marker.updatePopupContent();  // ← Regenera popup con nuevos datos
}
```

Esto asegura que cuando las bicicletas cambien en tiempo real, el popup se actualice inmediatamente con los nuevos números.

---
---

## Validaciones

1. **ID válido**: Se verifica que el ID extraído del payload sea un número válido y no esté ausente
2. **Estación cacheada**: Se verifica que la estación ya esté en caché (debe estar cargada en bulk)
3. **Manejo de errores**: Try-catch para evitar crashes por payloads inválidos

---

## Logging

Se agregaron logs para debugging:

```
// Cuando se recibe telemetría válida
[Stations WS] Telemetry station 123: ⚡ 10, ⚙️ 5

// Cuando falta ID válido
[Stations WS] Telemetry sin idStation válido, se ignora. Payload: { timestamp: 1764197585935 }

// Cuando la estación no está cacheada
[Stations WS] Telemetry recibido para estación no cacheada (456), se ignora

// Cuando hay error en parsing
[Stations WS] Error parsing telemetry: SyntaxError: Unexpected token } in JSON at position 45
```

---

## Compatibility

- ✅ Compatible con suscripción previa `/topic/stations.bulk` (carga inicial)
- ✅ No interfiere con otros servicios WebSocket
- ✅ Reutiliza métodos de `StationFlyweight` (updatePopupContent)
- ✅ Usa tipos TypeScript existentes

---

## Performance

- **Actualización incremental**: Solo actualiza el marcador específico
- **Sin recreación**: No recreamos marcadores, solo actualizamos datos
- **Regeneración de popup**: Solo cuando hay cambios en telemetría
- **Caching**: Reutiliza datos cacheados cuando sea posible

---

## Ejemplo de Payload

```json
{
  "idStation": 123,
  "timestamp": 1764197585935,
  "availableElectricBikes": 10,
  "availableMechanicBikes": 5
}
```

---

## Conclusión

✅ **El WebSocket ahora recibe y procesa telemetría en tiempo real de bicicletas disponibles.**

Los popups del mapa se actualizan automáticamente cuando:

1. Cambia el idioma (watchers previos)
2. Llega telemetría de disponibilidad de bicis (nuevo handler)

**Todo sin recargar la página.**
