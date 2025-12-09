# Diagnóstico Error 503 - Service Unavailable

## Problema
El endpoint `/travel/start` retorna HTTP 503 cuando se intenta reservar una bicicleta.

## Causas Posibles

### 1. **Servicios no están corriendo**
```bash
# Verifica si los contenedores están activos
docker-compose ps

# Servicios que se necesitan:
- viaje-service (puerto 8003)
- slots-service (puerto 8007)
- usuario-service (puerto 8001)
- estaciones-service (puerto 8005)
- gateway-server (puerto 8090)
- Redis
- PostgreSQL
- RabbitMQ
```

### 2. **Verifica los logs del viaje-service**
```bash
docker-compose logs viaje-service -f
docker-compose logs gateway-server -f
```

### 3. **Prueba la conexión directamente**
```bash
# Verifica si viaje-service está respondiendo
curl -X GET http://localhost:8003/travel

# Si responde, prueba el endpoint /start
curl -X POST http://localhost:8003/travel/start \
  -H "Content-Type: application/json" \
  -d '{
    "userUid": "user-001",
    "stationStartId": 1,
    "stationEndId": 2,
    "bikeType": "ELECTRIC"
  }'
```

### 4. **Verifica las dependencias internas**
El servicio `/travel/start` necesita:
- ✅ **SlotsClient** → slots-service (reservar slots)
- ✅ **UserClientRest** → usuario-service (verificar bloqueos)
- ✅ **StationClientRest** → estaciones-service (obtener tipo de estación)
- ✅ **Redis** → para guardar reservas temporales
- ✅ **RabbitMQ** → para mensajes de expiración

### 5. **Posibles errores en application.properties**
Verifica que en `viaje-service/src/main/resources/application.properties`:
```properties
# URL del servidor de descubrimiento (Eureka)
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka

# URL de Redis
spring.redis.host=redis
spring.redis.port=6379
spring.redis.password=redispassword

# URLs de los clientes
slots.service.url=http://slots-service:8007
usuario.service.url=http://usuario-service:8001
estaciones.service.url=http://estaciones-service:8005
```

### 6. **Reinicia todos los servicios**
```bash
# Detén todo
docker-compose down

# Limpia volúmenes (opcional si hay problemas de BD)
docker-compose down -v

# Inicia nuevamente
docker-compose up -d

# Verifica logs
docker-compose logs -f
```

## Solución Paso a Paso

1. **Verifica que todos los contenedores estén corriendo**
   ```bash
   docker-compose ps
   ```

2. **Si alguno no está corriendo, levántalo**
   ```bash
   docker-compose up -d nombre-servicio
   ```

3. **Verifica los logs del gateway**
   ```bash
   docker-compose logs gateway-server
   ```

4. **Si hay errores de conexión, verifica la red Docker**
   ```bash
   docker network ls
   docker network inspect movilidad-sostenible_default
   ```

5. **Prueba manualmente el endpoint desde la consola**
   ```bash
   # Dentro de un contenedor
   docker-compose exec gateway-server curl http://viaje-service:8003/travel
   ```

## Checklist Final
- [ ] ¿Todos los contenedores están corriendo? (docker-compose ps)
- [ ] ¿El viaje-service responde en su puerto 8003?
- [ ] ¿Redis está disponible?
- [ ] ¿Los servicios de slots, usuario y estaciones están disponibles?
- [ ] ¿El gateway-server puede alcanzar al viaje-service?
- [ ] ¿Los logs no muestran excepciones de conexión?
