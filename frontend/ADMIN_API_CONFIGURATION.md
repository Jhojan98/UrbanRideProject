# ⚙️ Configuración de API - Frontend Admin

## Problema Reportado

```
http://maintenance-service:5006/maintenance/ net::ERR_NAME_NOT_RESOLVED
```

El admin intentaba conectarse directamente a `http://maintenance-service:5006/` en lugar de usar el API Gateway en `http://34.9.26.232:8090/`.

## Solución Implementada

### 1. **Actualización de `vue.config.js`**

Se corrigió para:
- Cargar variables desde `.env` local (no desde la carpeta padre)
- Usar `VUE_APP_API_URL` desde variables de entorno
- Proxificar correctamente todas las rutas `/api/`

```javascript
// Cargar desde archivo local .env
const envPath = path.resolve(__dirname, '.env')
dotenv.config({ path: envPath })

// Proxy ahora usa variable de entorno
devServer: {
  proxy: {
    '^/api': {
      target: process.env.VUE_APP_API_URL || 'http://34.9.26.232:8090',
      changeOrigin: true,
      pathRewrite: { '^/api': '' },
      // ... headers CORS
    }
  }
}
```

### 2. **Actualización de `Dockerfile`**

Agregadas todas las variables de entorno necesarias durante el build:

```dockerfile
# Variables de entorno para el build
ARG VUE_APP_API_URL
ARG VUE_APP_WEBSOCKET_STATIONS_URL
ARG VUE_APP_WEBSOCKET_BICYCLES_URL
# ... Firebase vars ...

# Establecer en ENV
ENV VUE_APP_API_URL=$VUE_APP_API_URL
```

### 3. **Mejorado `maintenanceStore.ts`**

Agregado logging detallado para diagnosticar problemas:

```typescript
console.log('[Maintenance Store] Fetching from:', url);
console.log('[Maintenance Store] Successfully created maintenance:', created);
```

## Configuración Requerida

### Archivo `.env` (Admin)

```env
# API Gateway Configuration
VUE_APP_API_URL=http://34.9.26.232:8090/

# WebSocket Configuration
VUE_APP_WEBSOCKET_BICYCLES_URL=http://34.9.26.232:8002
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005

# Firebase (igual que client)
VUE_APP_FIREBASE_API_KEY=...
VUE_APP_FIREBASE_AUTH_DOMAIN=...
# ... etc ...
```

## Flujo de Peticiones

```
Admin Browser Request
  ↓
  /api/maintenance → (Vue CLI Dev Server Proxy)
  ↓
  http://34.9.26.232:8090/maintenance → (API Gateway)
  ↓
  maintenance-service:5006 → (Backend)
```

## Build en Producción

```bash
# Con variables de entorno
docker build \
  --build-arg VUE_APP_API_URL=http://34.9.26.232:8090 \
  --build-arg VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005 \
  --build-arg VUE_APP_WEBSOCKET_BICYCLES_URL=http://34.9.26.232:8002 \
  --build-arg VUE_APP_FIREBASE_API_KEY=... \
  # ... otras vars ...
  -t ecoride-admin:latest \
  -f admin/Dockerfile \
  .
```

O con docker-compose:
```bash
docker-compose build --build-arg VUE_APP_API_URL=http://34.9.26.232:8090
```

## Logs para Debugging

En la consola del navegador (F12 → Console):

```
[Maintenance Store] Fetching from: /api/maintenance/maintenance/
[Maintenance Store] Successfully fetched maintenances: 5
```

Si ves `http://maintenance-service:5006/` significa que:
- ❌ Las variables de entorno no se inyectaron en el build
- ❌ El `vue.config.js` no se está aplicando
- ❌ El Dockerfile no incluyó las variables ARG

## Diferencias con Client

| Aspecto | Client | Admin |
|---------|--------|-------|
| Puerto desarrollo | 8081 | 8080 |
| WebSocket Estaciones | ✓ | ✓ |
| WebSocket Bicis | ✗ | ✓ |
| API Gateway | ✓ | ✓ |
| Proxy `/api/` | ✓ | ✓ |

## Troubleshooting

### Error: `net::ERR_NAME_NOT_RESOLVED`

1. Verificar `.env`:
   ```bash
   cat frontend/admin/.env | grep VUE_APP_API_URL
   ```

2. Reconstruir:
   ```bash
   docker-compose down
   docker-compose build --no-cache admin-frontend
   docker-compose up -d admin-frontend
   ```

3. Verificar logs:
   ```bash
   docker-compose logs -f admin-frontend | grep "VUE_APP"
   ```

### Error: `Connection refused`

1. Verificar que API Gateway está disponible:
   ```bash
   curl http://34.9.26.232:8090/health
   ```

2. Verificar que maintenance-service está registrado:
   ```bash
   curl http://34.9.26.232:8090/eureka/apps
   ```

## Variables de Entorno Completas

```env
# API Gateway
VUE_APP_API_URL=http://34.9.26.232:8090/

# WebSockets (directos a microservicios)
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005
VUE_APP_WEBSOCKET_BICYCLES_URL=http://34.9.26.232:8002

# Firebase
VUE_APP_FIREBASE_API_KEY=AIzaSyDOSBAINt_b-4Yt7vO-uB86NIkT7uXUoik
VUE_APP_FIREBASE_AUTH_DOMAIN=test-firebase-8217b.firebaseapp.com
VUE_APP_FIREBASE_PROJECT_ID=test-firebase-8217b
VUE_APP_FIREBASE_STORAGE_BUCKET=test-firebase-8217b.firebasestorage.app
VUE_APP_FIREBASE_MESSAGING_SENDER_ID=867969237177
VUE_APP_FIREBASE_APP_ID=1:867969237177:web:5b51ca1fa38eed8c4e4f4d
VUE_APP_FIREBASE_MEASUREMENT_ID=G-XXXXXXXXXX
```

---

**Última actualización**: Diciembre 11, 2025
