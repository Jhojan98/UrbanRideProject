# Dashboard de Bicicletas - Documentación

## 📋 Descripción

El Dashboard de Bicicletas permite visualizar y monitorear en tiempo real todas las bicicletas del sistema UrbanRide, mostrando su ubicación en un mapa interactivo y sus datos en una tabla detallada.

## 🎯 Características

### 1. **Tabla de Bicicletas** (`BicycleList.vue`)
Muestra todas las bicicletas registradas con:
- **Serie**: Número de serie de la bicicleta
- **ID**: Identificador único
- **Tipo**: Eléctrica ⚡ o Mecánica 🔧
- **Estado de Bloqueo**: Desbloqueada (verde) / Bloqueada (gris) / Error (rojo)
- **Batería**: Nivel de batería para bicicletas eléctricas (con indicadores de color)
- **Ubicación**: Coordenadas lat/lon

### 2. **Mapa Interactivo** (`BicycleMapComponent.vue`)
Visualización en mapa usando **Leaflet** con marcadores diferenciados:
- 🚲 **Verde**: Bicicleta eléctrica con batería suficiente
- 🚲 **Rojo pulsante**: Bicicleta eléctrica con batería baja (<20%)
- 🚲 **Azul**: Bicicleta mecánica
- 🔒 **Gris**: Bicicleta bloqueada o con error

Al hacer clic en un marcador, se muestra un popup con:
- Serie e ID de la bicicleta
- Tipo (Eléctrica/Mecánica)
- Estado de bloqueo
- Nivel de batería (solo eléctricas)
- Coordenadas exactas
- Última actualización (si disponible)

## 🔄 Flujo de Datos

### Carga Inicial
```
1. BicyclesView.vue (onMounted)
   ↓
2. bikeStore.fetchBikes() → GET /bicy/
   ↓
3. Bicicletas cargadas en bikes[]
   ↓
4. BicycleList renderiza tabla
   ↓
5. BicycleMapComponent renderiza marcadores usando BicycleFactory
```

### Actualizaciones en Tiempo Real
```
1. bikeStore.connectWebSocket()
   ↓
2. BicycleWebSocketService.connect()
   ↓
3. Suscripción a /topic/bicycle.location
   ↓
4. Recibe: { latitude, longitude, battery, timestamp }
   ↓
5. bikeStore.handleLocationUpdate()
   ↓
6. Actualiza bikes[] → reactivity automática
   ↓
7. BicycleMapComponent detecta cambio (watch)
   ↓
8. Actualiza marcadores en el mapa
```

## 🏗️ Arquitectura

### Patrón Flyweight
El sistema utiliza el patrón **Flyweight** para optimizar el uso de memoria en los marcadores del mapa:

- **Estado Intrínseco** (compartido): Iconos de marcadores
  - `BicycleFlyweight`: Contiene los iconos estáticos (verde, rojo, azul, gris)

- **Estado Extrínseco** (único): Datos de cada bicicleta
  - `BicycleMarker`: Posición, datos específicos de cada bicicleta

- **Factory**: Gestiona el pool de marcadores
  - `BicycleFactory`: Reutiliza marcadores existentes o crea nuevos

### WebSocket Service
`BicycleWebSocketService` maneja la comunicación en tiempo real:
- Conexión STOMP sobre SockJS
- Suscripción a `/topic/bicycle.location`
- Callback system para notificar actualizaciones
- Reconexión automática en caso de desconexión
- Validación de datos recibidos

### Pinia Store
`bikeStore` centraliza el estado:
```typescript
state: {
  bikes: Bike[]                    // Array de todas las bicicletas
  bicycleFactory: BicycleFactory   // Factory para gestión de marcadores
  isWebSocketConnected: boolean    // Estado de conexión WS
}

actions: {
  fetchBikes()           // Carga inicial desde API
  connectWebSocket()     // Inicia WebSocket
  handleLocationUpdate() // Procesa actualizaciones WS
  disconnectWebSocket()  // Cierra conexión
  getBikeById(id)        // Busca bicicleta por ID
}

getters: {
  allBikes              // Array de bicicletas
  factory               // Instancia de BicycleFactory
  wsConnected           // Estado de conexión
}
```

## 🚀 Uso

### Acceder al Dashboard
1. Iniciar sesión en el sistema
2. Hacer clic en **"Dashboard de Bicicletas"** en el menú de navegación
3. Las bicicletas se cargarán automáticamente

### Variables de Entorno
```env
VUE_APP_API_URL=http://localhost:8080          # API REST para carga inicial
VUE_APP_WEBSOCKET_BICYCLES_URL=http://localhost:8003  # WebSocket para updates
```

## 📡 API Backend

### REST Endpoint
```
GET /bicy/
Response: Bike[]

Bike {
  id: string
  series: number
  model: "MECHANIC" | "ELECTRIC"
  lockStatus: "LOCKED" | "UNLOCKED" | "ERROR"
  lat: number
  lon: number
  battery: string
  timestamp?: Date
}
```

### WebSocket Topic
```
Topic: /topic/bicycle.location

Message Format:
{
  "latitude": 4.710982,
  "longitude": -74.072131,
  "battery": 94.85,
  "timestamp": 1765169236133
}

Note: El bikeId debe venir en el header del mensaje o en el payload
```

## 🎨 Componentes

### Jerarquía
```
BicyclesView.vue (Vista principal)
├── BicycleList.vue (Tabla de bicicletas)
└── BicycleMapComponent.vue (Mapa interactivo)
    └── BicycleFlyweight (Patrón para marcadores)
        ├── BicycleFlyweight (Iconos compartidos)
        ├── BicycleMarker (Marcadores individuales)
        └── BicycleFactory (Gestor del pool)
```

## 🔧 Mantenimiento

### Agregar Nuevo Campo
1. Actualizar interface `Bike` en `models/Bike.ts`
2. Modificar `BicycleList.vue` para mostrar en tabla
3. Actualizar `createPopupContent()` en `BicycleFlyweight.ts` para el popup

### Cambiar Iconos de Marcadores
Editar los `L.divIcon` en `BicycleFlyweight.ts`:
- `icon` (verde)
- `lowBatteryIcon` (rojo)
- `mechanicIcon` (azul)
- `lockedIcon` (gris)

### Modificar Topic WebSocket
Cambiar la suscripción en `BicycleWebSocketService.ts`:
```typescript
this.client.subscribe('/topic/bicycle.location', ...)
```

## 🐛 Debugging

### Console Logs
El sistema incluye logs detallados:
```
[BikeStore] Bicicletas cargadas: X
[Bicycles WS] Conectando a http://...
[Bicycles WS] Actualización recibida: {...}
🚲 Actualizando marcadores con X bicicletas
```

### Verificar Conexión WebSocket
```javascript
// En DevTools Console
const bikeStore = useBikeStore()
console.log('Conectado:', bikeStore.wsConnected)
console.log('Bicicletas:', bikeStore.allBikes.length)
```

## ⚠️ Limitaciones Conocidas

1. **Rendimiento**: Con más de 1000 bicicletas, considerar virtualización de tabla
2. **Clustering**: Marcadores superpuestos no se agrupan automáticamente
3. **Filtros**: Actualmente no hay filtros por tipo o estado (feature pendiente)

## 📚 Referencias

- [Leaflet Documentation](https://leafletjs.com/)
- [STOMP.js Documentation](https://stomp-js.github.io/stomp-websocket/)
- [Flyweight Pattern](https://refactoring.guru/design-patterns/flyweight)
