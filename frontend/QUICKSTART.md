## 🚀 Quick Start - Frontend Docker

### Requisitos previos
- Docker Desktop instalado y ejecutándose
- 8GB RAM disponible
- Puertos 8001 y 8002 libres

### Paso 1: Configurar variables de entorno
```bash
# Copiar template y editar con tus credenciales
cp .env.example .env

# Editar .env con:
# - Firebase credentials
# - URLs de backend
# - Configuración general
```

### Paso 2: Crear red Docker (una sola vez)
```powershell
docker network create urbanride-network
```

### Paso 3: Iniciar contenedores

**Opción A: Script PowerShell (Windows - Recomendado)**
```powershell
.\docker-manage.ps1 prod
# o modo desarrollo:
.\docker-manage.ps1 dev
```

**Opción B: Script Bash (Linux/Mac)**
```bash
chmod +x docker-manage.sh
./docker-manage.sh prod
# o modo desarrollo:
./docker-manage.sh dev
```

**Opción C: Docker Compose directo**
```bash
# Producción
docker-compose up -d --build

# Desarrollo
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

### Paso 4: Acceder a las aplicaciones

| Aplicación | URL |
|-----------|-----|
| Admin | [http://localhost:8001](http://localhost:8001) |
| Client | [http://localhost:8002](http://localhost:8002) |

### Comandos útiles

```powershell
# Ver estado de contenedores
docker-compose ps

# Ver logs en tiempo real
docker logs -f admin-frontend
docker logs -f client-frontend

# Detener contenedores
docker-compose stop

# Eliminar contenedores
docker-compose down

# Reconstruir sin caché
docker-compose up -d --build --no-cache
```

### Modos

**Modo Producción**
- Build optimizado
- Sin volúmenes (código fijo)
- Cambios requieren rebuild

**Modo Desarrollo**
- Volúmenes montados
- Hot reload (cambios inmediatos)
- Ideal para desarrollo local

### Solución rápida de problemas

**¿El puerto está en uso?**
```powershell
# Cambiar en docker-compose.yml
ports:
  - "9001:80"  # Cambiar 8001 a otro puerto
```

**¿Red no encontrada?**
```powershell
docker network create urbanride-network
```

**¿Código no se actualiza?**
```powershell
docker-compose up -d --build --no-cache
```

### Documentación completa
Ver `DOCKER_SETUP.md` para guía detallada.

---
¡Listo! Los contenedores deberían estar corriendo. 🎉
