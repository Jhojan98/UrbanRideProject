# Patrón Flyweight para Gestión de Bicicletas

## Descripción

Implementación del patrón de diseño **Flyweight** para la gestión eficiente de bicicletas en el mapa. Este patrón optimiza el uso de memoria al compartir datos comunes entre múltiples instancias de bicicletas.

## Estructura

### 1. **Modelo de Datos** (`models/Bicycle.ts`)

```typescript
interface Bicycle {
    id: string;
    lat: string;
    lon: string;
    battery: string;
    timestamp: Date;
}
```

- `timestamp`: Convertido automáticamente desde `long` (milisegundos) a `Date`
- `BicycleDTO`: DTO recibido del WebSocket con timestamp como `number`

### 2. **Patrón Flyweight** (`patterns/BicycleFlyweight.ts`)

#### **BicycleFlyweight** (Estado Intrínseco - Compartido)
- Contiene los íconos de los marcadores (compartidos entre todas las bicicletas)
- Dos tipos de íconos:
  - **Normal**: Batería ≥ 20% (verde 🚲)
  - **Batería baja**: Batería < 20% (rojo 🚲 con animación pulse)

#### **BicycleMarker** (Estado Extrínseco - Único)
- Datos específicos de cada bicicleta (posición, batería, ID)
- Métodos:
  - `render(map)`: Crea/actualiza el marcador en el mapa
  - `update(bicycle)`: Actualiza los datos de la bicicleta
  - `remove()`: Elimina el marcador del mapa
  - `getBicycle()`: Obtiene los datos actuales

#### **BicycleFactory** (Gestor del Pool)
- Mantiene un `Map<string, BicycleMarker>` con todas las bicicletas
- Métodos:
  - `getBicycleMarker(bicycle)`: Obtiene o crea un marcador (reutilización)
  - `removeBicycleMarker(id)`: Elimina un marcador específico
  - `getAllMarkers()`: Obtiene todos los marcadores activos
  - `clear()`: Limpia todos los marcadores
  - `size()`: Cantidad de bicicletas en el pool

### 3. **Servicio WebSocket** (`services/BicycleWebSocketService.ts`)

Gestiona la conexión WebSocket y suscripción al tópico `/topic/bicycle.location`.

#### Características:
- Usa **STOMP** sobre **SockJS**
- Reconexión automática cada 5 segundos
- Heartbeat bidireccional (4 segundos)
- Conversión automática de `timestamp` (long → Date)
- Notificación en tiempo real al recibir actualizaciones

#### Métodos:
- `connect(onUpdate)`: Conecta al WebSocket y se suscribe al tópico
- `disconnect()`: Desconecta el WebSocket
- `getIsConnected()`: Verifica estado de conexión
- `getBicycleCount()`: Cantidad de bicicletas en el pool

## Uso en el Mapa

### MapComponent.vue

```typescript
import { BicycleFactory } from '@/patterns/BicycleFlyweight'
import { BicycleWebSocketService } from '@/services/BicycleWebSocketService'

const bicycleFactory = new BicycleFactory()
const wsService = new BicycleWebSocketService(bicycleFactory)

onMounted(() => {
    // Inicializar mapa...
    
    // Conectar WebSocket
    wsService.connect((factory: BicycleFactory) => {
        // Renderizar bicicletas actualizadas
        factory.getAllMarkers().forEach(marker => {
            marker.render(map.value as LeafletMap)
        })
    })
})

onUnmounted(() => {
    wsService.disconnect()
    bicycleFactory.clear()
})
```

## Configuración

### Variables de Entorno

Agregar en `frontend/.env`:

```env
VUE_APP_WEBSOCKET_URL=wss://localhost:8090/ws/bicis/ws/
```

### Dependencias

```bash
npm install @stomp/stompjs sockjs-client
npm install --save-dev @types/sockjs-client
```

## Formato del Mensaje WebSocket

```json
{
    "id": "bike-001",
    "lat": "4.1514",
    "lon": "-73.6370",
    "battery": "85",
    "timestamp": 1732483200000
}
```

## Ventajas del Patrón Flyweight

1. **Reducción de memoria**: Los íconos se comparten entre todas las bicicletas
2. **Reutilización**: Los marcadores se reutilizan en lugar de crear nuevos
3. **Performance**: Actualización eficiente sin destruir/recrear objetos
4. **Escalabilidad**: Puede manejar cientos de bicicletas simultáneamente
5. **Separación de responsabilidades**: Estado intrínseco vs extrínseco

## Visualización

- **Popup**: Al hacer clic en una bicicleta muestra:
  - ID de la bicicleta
  - Nivel de batería (con color según nivel)
  - Coordenadas (lat/lon)
  - Última actualización (fecha y hora)

- **Colores de batería**:
  - Verde: ≥ 50%
  - Naranja: 20-49%
  - Rojo: < 20%

## Logs

La consola muestra:
- ✅ Conexión exitosa al WebSocket
- 📡 Suscripción al tópico
- 📦 Cada bicicleta recibida con sus datos
- 🚲 Total de bicicletas en el mapa
- ⚠️ Errores de conexión o parsing
