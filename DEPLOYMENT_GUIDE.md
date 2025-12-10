# 🚀 Guía de Despliegue Completo - EcoRide

## 📋 Índice
1. [Arquitectura del Sistema](#arquitectura)
2. [Pre-requisitos](#pre-requisitos)
3. [Configuración del Backend](#backend)
4. [Configuración del Frontend](#frontend)
5. [Pruebas con Mosquitto MQTT](#mosquitto)
6. [Verificación de Pagos](#pagos)
7. [Comandos de Despliegue](#despliegue)

---

## 🏗️ Arquitectura del Sistema <a name="arquitectura"></a>

```
┌─────────────────────────────────────────────────────────────┐
│                    USUARIOS                                  │
│          Client (8081)    │    Admin (8080)                 │
└──────────────┬────────────┴────────────┬───────────────────┘
               │                         │
               └────────────┬────────────┘
                            │
                    ┌───────▼───────┐
                    │   Gateway     │
                    │   :8090       │
                    └───────┬───────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
    ┌───▼────┐         ┌────▼────┐        ┌────▼────┐
    │Eureka  │         │Services │        │Services │
    │:8761   │         │         │        │         │
    └────────┘         └─────────┘        └─────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
    ┌───▼────┐         ┌────▼────┐        ┌────▼────┐
    │Postgres│         │RabbitMQ │        │Mosquitto│
    │:5432   │         │:5672    │        │:1883    │
    └────────┘         └─────────┘        └─────────┘
```

**Flujo de datos MQTT (Telemetría de bicicletas y estaciones):**
```
IoT Device/Simulator → Mosquitto (MQTT) → estaciones-service → WebSocket → Frontend
```

---

## ✅ Pre-requisitos <a name="pre-requisitos"></a>

### Software necesario:
- Docker >= 20.10
- Docker Compose >= 2.0
- Node.js >= 18 (para desarrollo local)
- mosquitto-clients (para pruebas MQTT)

### Instalación de mosquitto-clients:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install mosquitto-clients

# MacOS
brew install mosquitto

# Verificar instalación
mosquitto_pub --help
```

---

## 🔧 Configuración del Backend <a name="backend"></a>

### 1. Variables de entorno del backend

El backend ya tiene Mosquitto configurado en `movilidad-sostenible/docker-compose.yaml`

**Ubicación:** `/movilidad-sostenible/.env`

```env
# PostgreSQL
POSTGRES_ROOT_USER=postgres
POSTGRES_ROOT_PASSWORD=postgres
POSTGRES_ROOT_DB=movilidad_sostenible

# RabbitMQ
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=admin

# Mosquitto ya está configurado en docker-compose
```

### 2. Servicio que consume Mosquitto

El servicio **estaciones-service** es el que:
- Se suscribe a topics MQTT de Mosquitto
- Procesa telemetría de estaciones/bicicletas
- Publica actualizaciones vía WebSocket al frontend

**Topics MQTT:**
- `station/{idStation}/telemetry` - Telemetría de estaciones
- `bikes/{idBike}/telemetry` - Telemetría de bicicletas

### 3. Levantar el backend

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/movilidad-sostenible

# Construir y levantar todos los servicios
docker-compose up -d --build

# Ver logs de mosquitto
docker logs -f mosquitto

# Ver logs de estaciones-service
docker logs -f estaciones-service
```

### 4. Verificar que Mosquitto está corriendo

```bash
# Verificar que mosquitto responde
mosquitto_pub -h 34.9.26.232 -p 1883 -t "test/ping" -m "hello"

# Si no hay errores, mosquitto está funcionando ✅
```

---

## 🎨 Configuración del Frontend <a name="frontend"></a>

### 1. Variables de entorno

Ya configuraste:
- `/frontend/admin/.env` → Apunta a 34.9.26.232
- `/frontend/client/.env` → Necesita actualizarse

**Actualizar Client .env:**

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/frontend/client
```

Crear/actualizar `.env`:
```env
VUE_APP_FIREBASE_API_KEY=AIzaSyDOSBAINt_b-4Yt7vO-uB86NIkT7uXUoik
VUE_APP_FIREBASE_AUTH_DOMAIN=test-firebase-8217b.firebaseapp.com
VUE_APP_FIREBASE_PROJECT_ID=test-firebase-8217b
VUE_APP_FIREBASE_STORAGE_BUCKET=test-firebase-8217b.firebasestorage.app
VUE_APP_FIREBASE_MESSAGING_SENDER_ID=867969237177
VUE_APP_FIREBASE_APP_ID=1:867969237177:web:5b51ca1fa38eed8c4e4f4d
VUE_APP_FIREBASE_MEASUREMENT_ID=G-XXXXXXXXXX

# Backend en producción
VUE_APP_API_URL=http://34.9.26.232:8090

# WebSocket para datos en tiempo real
VUE_APP_WEBSOCKET_STATIONS_URL=http://34.9.26.232:8005
VUE_APP_WEBSOCKET_BICYCLES_URL=http://34.9.26.232:8002
```

### 2. Construir imágenes Docker del frontend

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/frontend

# Construir admin
docker build -t ecoride-admin:latest ./admin

# Construir client
docker build -t ecoride-client:latest ./client

# Verificar imágenes
docker images | grep ecoride
```

### 3. Desplegar frontend con Docker Compose

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/frontend

# Levantar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f
```

**Acceso:**
- Admin: http://localhost:8080
- Client: http://localhost:8081

---

## 🦟 Pruebas con Mosquitto MQTT <a name="mosquitto"></a>

### ¿Qué es Mosquitto y para qué sirve?

**Mosquitto** es un broker MQTT (Message Queuing Telemetry Transport) ligero que permite:
- Recibir telemetría de dispositivos IoT (sensores de bicicletas, estaciones)
- Publicar datos en tiempo real
- Comunicación asíncrona entre servicios

**En EcoRide:**
- Dispositivos IoT simulados publican datos de bicicletas/estaciones
- Mosquitto distribuye esos mensajes
- `estaciones-service` se suscribe y procesa los datos
- Frontend recibe actualizaciones vía WebSocket

### 1. Usar el script de simulación del backend

El backend ya tiene un script de simulación en:
```bash
/movilidad-sostenible/mosquitto/simulate.sh
```

**Ejecutar simulación:**

```bash
cd /home/xmara83/Escritorio/UD/UrbanRideProject/movilidad-sostenible/mosquitto

# Simular bicicleta 123456 enviando telemetría cada 5 segundos
BIKE_ID=123456 BROKER_HOST=34.9.26.232 INTERVAL=5 MAX_MESSAGES=10 ./simulate.sh

# Ver los mensajes publicados
```

**Lo que hace el script:**
- Publica mensajes JSON al topic `bikes/{BIKE_ID}/telemetry`
- Simula batería, ubicación GPS, velocidad
- Disminuye batería gradualmente

### 2. Pruebas manuales con mosquitto_pub

**Publicar telemetría de estación manualmente:**

```bash
# Estación ID 1 con 5 bicis mecánicas y 3 eléctricas disponibles
mosquitto_pub -h 34.9.26.232 -p 1883 \
  -t "station/1/telemetry" \
  -m '{
    "idStation": 1,
    "availableMechanicBikes": 5,
    "availableElectricBikes": 3,
    "timestamp": '$(date +%s%3N)'
  }'
```

**Publicar telemetría de bicicleta:**

```bash
# Bicicleta ID 101 con 75% batería
mosquitto_pub -h 34.9.26.232 -p 1883 \
  -t "bikes/101/telemetry" \
  -m '{
    "idBicycle": 101,
    "battery": 75,
    "latitude": 4.7109,
    "longitude": -74.0721,
    "timestamp": '$(date +%s%3N)'
  }'
```

### 3. Suscribirse para ver mensajes (debug)

```bash
# Ver TODOS los mensajes publicados
mosquitto_sub -h 34.9.26.232 -p 1883 -t "#" -v

# Ver solo mensajes de estaciones
mosquitto_sub -h 34.9.26.232 -p 1883 -t "station/+/telemetry" -v

# Ver solo mensajes de bicicletas
mosquitto_sub -h 34.9.26.232 -p 1883 -t "bikes/+/telemetry" -v
```

### 4. Verificar que el frontend recibe los datos

1. Abre el cliente: http://localhost:8081
2. Ve a la vista de reservación
3. Abre la consola del navegador (F12)
4. Busca logs que digan: `[Stations WS] Telemetry received`
5. Publica telemetría con mosquitto_pub
6. Deberías ver la disponibilidad actualizada en tiempo real ✅

---

## 💳 Verificación de Pagos <a name="pagos"></a>

### Flujo de Pago Implementado

**✅ Ya funciona correctamente:**

1. Usuario va a "Recargar Saldo"
2. Selecciona monto y hace clic en "Pagar con Stripe"
3. Stripe procesa el pago
4. Redirige a `/pago/success?session_id=xxx`
5. `PaymentSuccesComponent` automáticamente:
   - Marca el pago como completado
   - Actualiza el balance del usuario
   - Espera 3 segundos
   - **Redirige a `/my-profile`** ✅

**Si el usuario cancela:**
- Redirige a `/pago/cancel`
- Muestra botón para volver al inicio

### Probar el flujo completo

```bash
# 1. Iniciar sesión en el cliente
# 2. Ir a "Mi Perfil" → "Recargar Saldo"
# 3. Usar tarjeta de prueba de Stripe:
#    Número: 4242 4242 4242 4242
#    Fecha: Cualquier fecha futura
#    CVC: Cualquier 3 dígitos
# 4. Completar pago
# 5. Verificar que redirige a Mi Perfil después de 3 segundos ✅
```

**El código ya está implementado en:**
```vue
<!-- PaymentSuccesComponent.vue línea 123-125 -->
setTimeout(() => {
  this.$router.push('/my-profile');
}, 3000);
```

---

## 🚀 Comandos de Despliegue <a name="despliegue"></a>

### Despliegue Completo (Backend + Frontend)

```bash
# 1. Levantar backend
cd /home/xmara83/Escritorio/UD/UrbanRideProject/movilidad-sostenible
docker-compose down
docker-compose up -d --build

# 2. Levantar frontend
cd /home/xmara83/Escritorio/UD/UrbanRideProject/frontend
docker-compose down
docker-compose up -d --build

# 3. Verificar que todo está corriendo
docker ps
```

### Verificación de Servicios

```bash
# Backend
curl http://34.9.26.232:8761  # Eureka
curl http://34.9.26.232:8090  # Gateway

# Frontend
curl http://localhost:8080    # Admin
curl http://localhost:8081    # Client

# Mosquitto
mosquitto_pub -h 34.9.26.232 -p 1883 -t "test" -m "ping"
```

### Ver Logs

```bash
# Backend
docker logs -f estaciones-service
docker logs -f gateway-server
docker logs -f mosquitto

# Frontend
docker logs -f admin-frontend
docker logs -f client-frontend
```

### Detener todo

```bash
# Backend
cd movilidad-sostenible && docker-compose down

# Frontend
cd frontend && docker-compose down
```

---

## 🎯 Checklist Final

- [ ] Backend corriendo (Eureka, Gateway, Servicios)
- [ ] PostgreSQL con datos iniciales
- [ ] RabbitMQ funcionando
- [ ] Mosquitto aceptando conexiones
- [ ] Frontend Admin construido y desplegado
- [ ] Frontend Client construido y desplegado
- [ ] WebSocket conectando estaciones
- [ ] Telemetría MQTT fluyendo
- [ ] Pagos redirigiendo correctamente
- [ ] Login funcionando en ambos frontends

---

## 📞 Troubleshooting

**Problema: "Failed to fetch" en login**
```bash
# Verificar que gateway está corriendo
curl http://34.9.26.232:8090/actuator/health

# Verificar logs
docker logs gateway-server
```

**Problema: Telemetría no llega al frontend**
```bash
# 1. Verificar mosquitto
mosquitto_sub -h 34.9.26.232 -p 1883 -t "#" -v

# 2. Verificar estaciones-service
docker logs -f estaciones-service | grep MQTT

# 3. Verificar WebSocket en frontend (F12 consola)
```

**Problema: Token expirado (401)**
```bash
# Cerrar sesión e iniciar de nuevo
# Los tokens de Firebase expiran después de 1 hora
```

---

¡Tu sistema EcoRide está listo para desplegar! 🚴‍♂️🌱
