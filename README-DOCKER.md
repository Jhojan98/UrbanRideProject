# UrbanRide Project - Guía de Despliegue con Docker

## 🚀 Características Implementadas

### Microservicio de Usuarios con Gestión de Métodos de Pago

El sistema ahora incluye funcionalidad completa para gestionar tarjetas de crédito, débito y otros métodos de pago:

- ✅ **CRUD completo de métodos de pago**
- ✅ **Validación de tarjetas** (Algoritmo de Luhn)
- ✅ **Detección automática de marca** (VISA, MasterCard, AMEX, etc.)
- ✅ **Gestión de método de pago principal**
- ✅ **Enmascaramiento de números de tarjeta** para seguridad
- ✅ **Múltiples métodos de pago por usuario**
- ✅ **Soporte para diferentes tipos**: Crédito, Débito, PSE, Efectivo

## 📋 Pre-requisitos

- Docker Desktop instalado (version 20.10+)
- Docker Compose (version 2.0+)
- Al menos 2GB de RAM disponible
- Puertos disponibles: 5432, 8080, 5050

## 🐳 Arquitectura Docker

El proyecto incluye los siguientes servicios:

1. **PostgreSQL** (puerto 5432): Base de datos principal
2. **Usuario Service** (puerto 8080): Microservicio Spring Boot
3. **pgAdmin** (puerto 5050): Interfaz web para administrar PostgreSQL (opcional)

## 🚀 Inicio Rápido

### 1. Construir y levantar todos los servicios

```bash
cd "c:\Users\Administrador\Downloads\UrbanRideProject-main (1)\UrbanRideProject-main"
docker-compose up --build
```

### 2. Levantar servicios en segundo plano (detached)

```bash
docker-compose up -d
```

### 3. Ver logs de los servicios

```bash
# Ver todos los logs
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f usuario-service
docker-compose logs -f postgres
```

### 4. Detener servicios

```bash
docker-compose down
```

### 5. Detener y eliminar volúmenes (limpieza completa)

```bash
docker-compose down -v
```

## 📡 Endpoints API - Métodos de Pago

### Base URL
```
http://localhost:8080/api/metodos-pago
```

### 1. Listar métodos de pago de un usuario
```http
GET /api/metodos-pago/usuario/{usuarioCC}
```

### 2. Obtener método de pago específico
```http
GET /api/metodos-pago/{id}/usuario/{usuarioCC}
```

### 3. Obtener método de pago principal
```http
GET /api/metodos-pago/usuario/{usuarioCC}/principal
```

### 4. Agregar nuevo método de pago
```http
POST /api/metodos-pago
Content-Type: application/json

{
  "k_usuarioCC": "1234567890",
  "t_tipoTarjeta": "CREDITO",
  "n_numeroTarjetaCompleto": "4532015112830366",
  "n_nombreTitular": "JUAN PEREZ",
  "f_fechaExpiracion": "2026-12-31",
  "b_principal": true,
  "n_direccionFacturacion": "Calle 123 #45-67",
  "n_codigoPostal": "110111"
}
```

### 5. Actualizar método de pago
```http
PUT /api/metodos-pago/{id}/usuario/{usuarioCC}
Content-Type: application/json

{
  "n_nombreTitular": "JUAN PEREZ ACTUALIZADO",
  "n_direccionFacturacion": "Nueva Dirección 456"
}
```

### 6. Eliminar método de pago
```http
DELETE /api/metodos-pago/{id}/usuario/{usuarioCC}
```

### 7. Establecer como método principal
```http
PATCH /api/metodos-pago/{id}/usuario/{usuarioCC}/principal
```

### 8. Validar número de tarjeta
```http
POST /api/metodos-pago/validar
Content-Type: application/json

{
  "numeroTarjeta": "4532015112830366"
}
```

## 🧪 Números de Tarjeta de Prueba (válidos según Luhn)

Estos números pueden usarse para pruebas:

- **VISA**: 4532015112830366
- **MasterCard**: 5425233430109903
- **AMEX**: 378282246310005
- **Discover**: 6011111111111117

## 🔍 Verificar Estado de Servicios

### Health Check del microservicio
```bash
curl http://localhost:8080/actuator/health
```

### Conectarse a PostgreSQL
```bash
# Desde línea de comandos
docker exec -it urbanride-postgres psql -U urbanride -d sistemabicicletas

# Consultas útiles
\dt  # Listar tablas
SELECT * FROM metodo_pago;
SELECT * FROM usuario;
```

## 🎨 Acceder a pgAdmin

1. Abrir navegador: http://localhost:5050
2. Credenciales:
   - Email: `admin@urbanride.com`
   - Password: `admin2024`

3. Agregar servidor:
   - Host: `postgres`
   - Port: `5432`
   - Database: `sistemabicicletas`
   - Username: `urbanride`
   - Password: `urbanride2024`

## 🔧 Comandos Útiles

### Reconstruir solo el microservicio
```bash
docker-compose build usuario-service
docker-compose up -d usuario-service
```

### Ver recursos de Docker
```bash
docker ps  # Ver contenedores activos
docker images  # Ver imágenes
docker volume ls  # Ver volúmenes
```

### Limpiar recursos Docker
```bash
docker system prune -a  # Limpia todo (cuidado!)
docker volume prune  # Limpia volúmenes sin usar
```

## 📊 Estructura de la Base de Datos

### Tabla: metodo_pago

| Campo | Tipo | Descripción |
|-------|------|-------------|
| k_metodoPago | BIGSERIAL | ID único (auto-incremento) |
| k_usuario_cc | VARCHAR(50) | Cédula del usuario |
| t_tipoTarjeta | VARCHAR(20) | CREDITO, DEBITO, PSE, EFECTIVO |
| n_numeroTarjeta | VARCHAR(20) | Número enmascarado |
| n_numeroTarjetaCompleto | VARCHAR(20) | Número completo |
| n_nombreTitular | VARCHAR(100) | Nombre del titular |
| f_fechaExpiracion | DATE | Fecha de expiración |
| n_marca | VARCHAR(20) | VISA, MASTERCARD, etc. |
| b_principal | BOOLEAN | ¿Es el método principal? |
| b_activo | BOOLEAN | ¿Está activo? |
| f_fechaRegistro | TIMESTAMP | Fecha de registro |
| n_direccionFacturacion | VARCHAR(255) | Dirección de facturación |
| n_codigoPostal | VARCHAR(20) | Código postal |

## 🛡️ Seguridad

⚠️ **IMPORTANTE**: En producción:
1. Nunca almacenes números de tarjeta completos sin encriptación
2. Usa un servicio de tokenización (Stripe, PayU, etc.)
3. Implementa autenticación JWT
4. Habilita HTTPS/TLS
5. Encripta datos sensibles en la base de datos
6. Implementa rate limiting

## 🐛 Solución de Problemas

### El contenedor no inicia
```bash
docker-compose logs usuario-service
```

### Puerto 8080 ya está en uso
Cambia el puerto en `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Mapea al puerto 8081 en tu máquina
```

### Error de conexión a base de datos
Verifica que PostgreSQL esté listo:
```bash
docker-compose logs postgres
```

### Reiniciar todo desde cero
```bash
docker-compose down -v
docker-compose up --build
```

## 📝 Próximos Pasos

- [ ] Implementar autenticación JWT
- [ ] Agregar encriptación para números de tarjeta
- [ ] Integrar con pasarela de pagos real
- [ ] Implementar microservicio de pagos separado
- [ ] Agregar API Gateway
- [ ] Configurar Service Discovery (Eureka)

## 📞 Soporte

Para problemas o preguntas, revisa los logs con:
```bash
docker-compose logs -f
```

---
**Versión**: 1.0.0  
**Última actualización**: Noviembre 2025
