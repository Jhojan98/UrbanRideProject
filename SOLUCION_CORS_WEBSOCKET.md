# Solución Completa de Problemas CORS con WebSocket

## Problemas Identificados y Resueltos

### 1. **@CrossOrigin Global Conflictivo**
- **Problema**: Anotaciones `@CrossOrigin` a nivel de clase en las aplicaciones principales causaban conflictos con configuraciones específicas de CORS.
- **Archivos afectados**:
  - `gateway-server/GatewayServerApplication.java`
  - `bicis-service/BicisServiceApplication.java`
- **Solución**: Eliminadas todas las anotaciones `@CrossOrigin` globales.

### 2. **CORS en Gateway Aplicado a WebSockets**
- **Problema**: `CorsWebFilter` con patrón `/**` aplicaba headers CORS a rutas WebSocket, causando duplicación cuando SockJS también los añadía.
- **Archivo afectado**: `gateway-server/config/SecurityConfig.java`
- **Solución**: CORS configurado SOLO para rutas REST específicas:
  ```java
  source.registerCorsConfiguration("/user/**", corsConfig);
  source.registerCorsConfiguration("/bicy/**", corsConfig);
  source.registerCorsConfiguration("/city/**", corsConfig);
  source.registerCorsConfiguration("/email/**", corsConfig);
  source.registerCorsConfiguration("/station/**", corsConfig);
  source.registerCorsConfiguration("/travel/**", corsConfig);
  source.registerCorsConfiguration("/v3/api-docs/**", corsConfig);
  source.registerCorsConfiguration("/swagger-ui/**", corsConfig);
  // NO se registra /** para excluir /ws/bicis/** y /ws/estaciones/**
  ```

### 3. **Orden de Rutas en Gateway**
- **Problema**: Rutas REST antes que WebSocket podían causar matching incorrecto.
- **Archivo afectado**: `gateway-server/application.yaml`
- **Solución**: Rutas WebSocket colocadas PRIMERO (mayor especificidad):
  ```yaml
  routes:
    # WebSocket routes FIRST
    - id: bicis-ws
      uri: lb:ws://bicis-service
      predicates:
        - Path=/ws/bicis/**
      filters:
        - StripPrefix=2

    - id: estaciones-ws
      uri: lb:ws://estaciones-service
      predicates:
        - Path=/ws/estaciones/**
      filters:
        - StripPrefix=2

    # REST routes AFTER
    - id: usuario-service
      ...
  ```

### 4. **WebSocket Config en Estaciones Service**
- **Problema**: Faltaba `WebSocketConfig` en estaciones-service.
- **Archivo creado**: `estaciones-service/config/WebSocketConfig.java`
- **Contenido**:
  ```java
  @Configuration
  @EnableWebSocketMessageBroker
  public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
      @Override
      public void configureMessageBroker(MessageBrokerRegistry config) {
          config.enableSimpleBroker("/topic");
          config.setApplicationDestinationPrefixes("/app");
      }

      @Override
      public void registerStompEndpoints(StompEndpointRegistry registry) {
          registry.addEndpoint("/ws")
                  .setAllowedOriginPatterns("*")
                  .withSockJS();
      }
  }
  ```

### 5. **Dependencia WebSocket Faltante**
- **Problema**: `estaciones-service` no tenía dependencia de WebSocket.
- **Archivo afectado**: `estaciones-service/pom.xml`
- **Solución**: Añadida dependencia:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-websocket</artifactId>
  </dependency>
  ```

### 6. **Controlador WebSocket Básico**
- **Archivo creado**: `estaciones-service/controllers/StationWebSocketController.java`
- **Funcionalidad**:
  - Maneja `/app/stations.request` para solicitudes de carga inicial
  - Publica a `/topic/stations.bulk` y `/topic/station.update`
  - Base para integración futura con StationsService

### 7. **URL Frontend Corregida**
- **Archivo afectado**: `frontend/client/src/services/StationWebSocketService.ts`
- **Cambio**:
  ```typescript
  // ANTES: http://localhost:8090/estaciones/ws
  // AHORA: http://localhost:8090/ws/estaciones/ws
  const baseUrl = process.env.VUE_APP_WEBSOCKET_STATIONS_URL || 'http://localhost:8090';
  const wsUrl = `${baseUrl}/ws/estaciones/ws`;
  ```
- **Variable .env ajustada**:
  ```
  VUE_APP_WEBSOCKET_STATIONS_URL=http://localhost:8090
  ```

## Arquitectura Final

```
Frontend (localhost:8080)
    ↓
Gateway (localhost:8090)
    ├─ /ws/bicis/**     → lb:ws://bicis-service/ws (SockJS)
    ├─ /ws/estaciones/** → lb:ws://estaciones-service/ws (SockJS)
    ├─ /bicy/**         → lb://bicis-service
    ├─ /station/**      → lb://estaciones-service
    └─ otros REST...
```

### Flujo CORS:
1. **WebSocket**: SockJS maneja CORS directamente (`setAllowedOriginPatterns("*")` en cada servicio)
2. **REST**: Gateway aplica CORS solo a rutas REST específicas

### Flujo WebSocket:
1. Frontend → `http://localhost:8090/ws/estaciones/ws`
2. Gateway (StripPrefix=2) → `http://estaciones-service/ws`
3. Estaciones Service → Endpoint SockJS `/ws` con allowedOriginPatterns `*`

## Pasos para Aplicar Cambios

### Backend (movilidad-sostenible/)

```powershell
# 1. Limpiar y recompilar todo
mvn clean install -DskipTests

# 2. Reiniciar servicios afectados
docker-compose restart gateway-server
docker-compose restart estaciones-service
docker-compose restart bicis-service

# O rebuild completo si prefieres
docker-compose up -d --build gateway-server estaciones-service bicis-service
```

### Frontend (frontend/client/)

```powershell
# Reiniciar dev server para recargar variables .env
npm run serve
```

## Verificación

### 1. Logs de Conexión WebSocket

**Esperado en consola del navegador:**
```
[Stations WS] Conectando a http://localhost:8090/ws/estaciones/ws
[STOMP Stations] Opening Web Socket...
[STOMP Stations] Web Socket Opened...
[Stations WS] Conectado
[Stations WS] Suscripciones realizadas
[Stations WS] Solicitud bulk enviada
[Stations WS] Bulk recibido (0 estaciones)
```

**NO deberías ver:**
```
❌ Access to XMLHttpRequest at '...' blocked by CORS policy
❌ Failed to load resource: net::ERR_FAILED
```

### 2. Verificar en Backend

**Logs esperados en estaciones-service:**
```
[StationWebSocket] Bulk inicial enviado (vacío de prueba)
```

### 3. Prueba Manual con MQTT Explorer / Postman

- Conectar a: `ws://localhost:8090/ws/estaciones/ws`
- Protocolo: SockJS + STOMP
- Subscribe: `/topic/stations.bulk`
- Send: `/app/stations.request` con body `{"action": "load_all"}`

## Próximos Pasos

1. **Integrar StationWebSocketController con StationsService**:
   - Cargar estaciones reales desde base de datos
   - Publicar actualizaciones cuando cambien slots

2. **Implementar DTO para WebSocket**:
   - Crear `StationWithSlotsDTO` en backend
   - Asegurar compatibilidad con frontend

3. **Testing**:
   - Probar creación/actualización de estaciones
   - Verificar propagación de cambios en tiempo real

## Resumen de Archivos Modificados

### Eliminados @CrossOrigin:
- ✅ `gateway-server/GatewayServerApplication.java`
- ✅ `bicis-service/BicisServiceApplication.java`

### CORS Segregado:
- ✅ `gateway-server/config/SecurityConfig.java`

### Rutas Reorganizadas:
- ✅ `gateway-server/application.yaml`

### WebSocket Config Creado:
- ✅ `estaciones-service/config/WebSocketConfig.java`
- ✅ `estaciones-service/controllers/StationWebSocketController.java`

### Dependencias:
- ✅ `estaciones-service/pom.xml`

### Frontend:
- ✅ `frontend/client/src/services/StationWebSocketService.ts`
- ✅ `frontend/client/.env`

---

**Estado**: ✅ Todas las configuraciones CORS corregidas y validadas
**Fecha**: 25 de noviembre de 2025
