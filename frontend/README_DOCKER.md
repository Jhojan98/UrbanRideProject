# 🚀 Despliegue Frontend - EcoRide

## Quick Start - Solo Frontend en Docker

**Requisitos:**
- Docker instalado
- Backend corriendo en 34.9.26.232:8090 (ya lo tienes)

## 1. Preparar configuración

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/frontend
```

### Verificar que los .env están correctos:

**admin/.env** debe tener:
```env
VUE_APP_API_URL=http://34.9.26.232:8090
```

**client/.env** debe tener:
```env
VUE_APP_API_URL=http://34.9.26.232:8090
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005
VUE_APP_WEBSOCKET_BICYCLES_URL=http://34.9.26.232:8002
```

## 2. Construir imágenes Docker

```bash
# Construir ambas imágenes
docker-compose build
```

Este comando:
- Compila admin (Vue.js) → Docker image
- Compila client (Vue.js) → Docker image

## 3. Levantar contenedores

```bash
# Levantar frontend
docker-compose up -d

# Ver que está corriendo
docker ps
```

## 4. Acceder

- **Admin:** http://localhost:8080
- **Client:** http://localhost:8081

## Comandos útiles

```bash
# Ver logs
docker-compose logs -f

# Logs de admin solo
docker-compose logs -f ecoride-admin

# Logs de client solo
docker-compose logs -f ecoride-client

# Detener todo
docker-compose down

# Reconstruir
docker-compose up -d --build
```

## ¿Qué está pasando?

1. **Docker compila** ambos frontends con `npm run build`
2. **Genera imágenes** optimizadas con Nginx
3. **Admin corre en puerto 8080**
4. **Client corre en puerto 8081**
5. **Ambos conectan** al backend en 34.9.26.232:8090

## Si algo no funciona

```bash
# Ver error en detalle
docker-compose logs ecoride-admin
docker-compose logs ecoride-client

# Verificar que el backend responde
curl http://34.9.26.232:8090

# Verificar credenciales
cat admin/.env
cat client/.env
```

---

¡Eso es todo! Tu frontend está dockerizado y listo. 🎉
