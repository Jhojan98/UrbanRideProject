# ⚡ Configuración del WebSocket - Estaciones

## Resumen

El WebSocket del servicio de estaciones se utiliza para recibir actualizaciones en tiempo real sobre la disponibilidad de bicicletas (mecánicas y eléctricas) en cada estación.

## Configuración

### Archivo de Entorno

El WebSocket se configura mediante la variable de entorno `VUE_APP_WEBSOCKET_STATIONS_URL` en el archivo `.env` de la carpeta `client`.

#### Desarrollo Local

```env
# client/.env
VUE_APP_API_URL=http://localhost:8090
VUE_APP_WEBSOCKET_STATIONS_URL=http://localhost:8005
VUE_APP_EXCHANGE_RATE_API_URL=https://api.exchangerate-api.com/v4/latest
```

#### Producción

```env
# client/.env
VUE_APP_API_URL=http://34.9.26.232:8090
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005
VUE_APP_EXCHANGE_RATE_API_URL=https://api.exchangerate-api.com/v4/latest
```

### Docker Build

Cuando se construye la imagen Docker, la variable de entorno se debe pasar como `--build-arg`:

```bash
docker build \
  --build-arg VUE_APP_API_URL=http://34.9.26.232:8090 \
  --build-arg VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005 \
  --build-arg VUE_APP_EXCHANGE_RATE_API_URL=https://api.exchangerate-api.com/v4/latest \
  --build-arg VUE_APP_FIREBASE_API_KEY=... \
  --build-arg VUE_APP_FIREBASE_AUTH_DOMAIN=... \
  -t ecoride-client:latest \
  -f client/Dockerfile \
  .
```

O con docker-compose (si está configurado):

```bash
docker-compose build --build-arg VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005
```

## Cómo Funciona

### Flujo de Conexión

1. **Inicialización**: Cuando el componente `MapComponent.vue` se monta, intenta conectarse al WebSocket
2. **URL**: Se obtiene de la variable de entorno `VUE_APP_WEBSOCKET_STATIONS_URL`
3. **Conexión**: Se usa STOMP sobre SockJS para conexión persistente
4. **Suscripción**: Se suscribe al tema `/topic/station.update/user`
5. **Actualizaciones**: Recibe telemetría en tiempo real con disponibilidad de bicicletas

### Fallbacks Automáticos

El servicio `StationWebSocketService.ts` implementa un sistema de fallbacks inteligente:

```typescript
1. Intenta usar VUE_APP_WEBSOCKET_STATIONS_URL (variable de entorno)
2. Si no existe, verifica si está en desarrollo (NODE_ENV === 'development') → usa localhost:8005
3. Si no, intenta derivar del VUE_APP_API_URL (mismo host, puerto 8005)
4. Fallback final: localhost:8005 en desarrollo
```

## Troubleshooting

### Error: `GET http://localhost:8005/ws/info net::ERR_CONNECTION_REFUSED`

**Causa**: La variable de entorno no se inyectó correctamente en producción.

**Soluciones**:

1. **Verificar que `.env` tiene la configuración correcta**:
   ```bash
   cat client/.env
   ```

2. **Verificar que Docker incluye la variable**:
   ```bash
   docker inspect ecoride-client | grep VUE_APP_WEBSOCKET
   ```

3. **Reconstruir la imagen con variables**:
   ```bash
   docker-compose down
   docker-compose build --no-cache
   docker-compose up -d
   ```

4. **Verificar que el servicio de estaciones está disponible**:
   ```bash
   curl http://34.9.26.232:8005/ws/info
   ```

### Error: Conexión rechazada al puerto 8005

**Causa**: El servicio de estaciones-service no está disponible en el puerto 8005.

**Verificación**:
```bash
# Verificar que el servicio responde
curl -v http://34.9.26.232:8005/health

# Verificar puerto abierto
nc -zv 34.9.26.232 8005
```

### Logs para Debugging

En el navegador, abrir la consola (F12) y buscar logs que comiencen con `[Stations WS]`:

```
[Stations WS] Environment VUE_APP_WEBSOCKET_STATIONS_URL: http://34.9.26.232:8005
[Stations WS] Resolved base URL: http://34.9.26.232:8005
[Stations WS] Connecting to: http://34.9.26.232:8005/ws
[Stations WS] Connected
```

## Archivos Relevantes

- **Configuración**: `frontend/client/.env`
- **Servicio**: `frontend/client/src/services/StationWebSocketService.ts`
- **Componente Principal**: `frontend/client/src/components/reservation/MapComponent.vue`
- **Dockerfile**: `frontend/client/Dockerfile`
- **Vue Config**: `frontend/client/vue.config.js`

## Variables de Entorno Requeridas

```env
# WebSocket directo a estaciones-service (puerto 8005)
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005

# API Gateway (para consultas REST)
VUE_APP_API_URL=http://34.9.26.232:8090

# Firebase (autenticación)
VUE_APP_FIREBASE_API_KEY=...
VUE_APP_FIREBASE_AUTH_DOMAIN=...
VUE_APP_FIREBASE_PROJECT_ID=...
VUE_APP_FIREBASE_STORAGE_BUCKET=...
VUE_APP_FIREBASE_MESSAGING_SENDER_ID=...
VUE_APP_FIREBASE_APP_ID=...
VUE_APP_FIREBASE_MEASUREMENT_ID=...

# Tasas de cambio
VUE_APP_EXCHANGE_RATE_API_URL=https://api.exchangerate-api.com/v4/latest
```

## Deployments Recomendados

### Desarrollo
```bash
# Terminal 1: Backend en localhost:8090
# Terminal 2: Frontend en localhost:8081 con vue-cli-service serve

npm run serve
# Usa VUE_APP_WEBSOCKET_STATIONS_URL=http://localhost:8005
```

### Producción con Docker
```bash
# Reconstruir con variables de entorno correctas
docker-compose build

# Levantar servicios
docker-compose up -d

# Verificar logs
docker-compose logs -f ecoride-client
```

### Kubernetes/Production Remote
```bash
# 1. Actualizar .env con URLs de producción
# 2. Buildear imagen
# 3. Empujar a registry
# 4. Desplegar con docker-compose o kubectl con variables inyectadas
```

---

**Última actualización**: Diciembre 11, 2025
