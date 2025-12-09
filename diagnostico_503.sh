#!/bin/bash

# Script para diagnosticar problema 503 en /travel/start

echo "=========================================="
echo "Diagnostico Error 503 - Travel Service"
echo "=========================================="
echo ""

# 1. Verificar si el contenedor está corriendo
echo "1. Verificando si viaje-service está corriendo..."
docker-compose ps viaje-service
echo ""

# 2. Verificar logs recientes
echo "2. Últimos 20 líneas de logs del viaje-service:"
docker-compose logs viaje-service --tail=20
echo ""

# 3. Verificar si está registrado en Eureka
echo "3. Verificando si viaje-service está registrado en Eureka..."
curl -s http://localhost:8761/eureka/apps | grep -i "viaje-service" || echo "No encontrado en Eureka"
echo ""

# 4. Verificar conectividad directa al viaje-service
echo "4. Intentando conectar directamente a viaje-service:8003..."
docker-compose exec gateway-server curl -v http://viaje-service:8003/travel 2>&1 | head -20
echo ""

# 5. Verificar si Redis está disponible
echo "5. Verificando Redis..."
docker-compose exec viaje-service redis-cli -h redis -p 6379 -a redispassword ping 2>/dev/null || echo "Redis no disponible"
echo ""

# 6. Verificar dependencias del viaje-service
echo "6. Verificando dependencias..."
echo "   - slots-service:"
docker-compose exec gateway-server curl -s http://slots-service:8007/slot 2>&1 | head -c 100
echo ""
echo "   - usuario-service:"
docker-compose exec gateway-server curl -s http://usuario-service:8001/user 2>&1 | head -c 100
echo ""
echo "   - estaciones-service:"
docker-compose exec gateway-server curl -s http://estaciones-service:8005/station 2>&1 | head -c 100
echo ""

echo "=========================================="
echo "Fin del diagnóstico"
echo "=========================================="
