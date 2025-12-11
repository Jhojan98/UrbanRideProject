#!/bin/bash

# 🚀 Script de Despliegue Rápido - EcoRide
# Este script despliega todo el sistema automáticamente

set -e  # Detener en caso de error

echo "🚴 ====================================="
echo "🚴  ECORIDE - Despliegue Completo"
echo "🚴 ====================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Directorio raíz del proyecto
PROJECT_ROOT="/home/xmara83/Escritorio/UD/UrbanRideProject"

# ============================================
# 1. VERIFICAR PRE-REQUISITOS
# ============================================
echo "📋 Verificando pre-requisitos..."

if ! command -v docker &> /dev/null; then
    print_error "Docker no está instalado"
    exit 1
fi
print_success "Docker encontrado"

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose no está instalado"
    exit 1
fi
print_success "Docker Compose encontrado"

echo ""

# ============================================
# 2. DESPLEGAR BACKEND
# ============================================
echo "🔧 Desplegando Backend..."
cd "$PROJECT_ROOT/movilidad-sostenible"

print_warning "Deteniendo contenedores anteriores..."
docker-compose down

print_warning "Construyendo y levantando servicios..."
docker-compose up -d --build

print_success "Backend desplegado"
echo ""

# Esperar a que Eureka esté listo
echo "⏳ Esperando a que Eureka se inicie (30s)..."
sleep 30

# ============================================
# 3. DESPLEGAR FRONTEND
# ============================================
echo "🎨 Desplegando Frontend..."
cd "$PROJECT_ROOT/frontend"

print_warning "Deteniendo contenedores anteriores..."
docker-compose down

print_warning "Construyendo imágenes..."
docker-compose build

print_warning "Levantando servicios..."
docker-compose up -d

print_success "Frontend desplegado"
echo ""

# ============================================
# 4. VERIFICAR SERVICIOS
# ============================================
echo "🔍 Verificando servicios..."

# Función para verificar servicio
check_service() {
    local name=$1
    local url=$2
    
    if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302"; then
        print_success "$name está funcionando"
        return 0
    else
        print_warning "$name no responde aún"
        return 1
    fi
}

sleep 10  # Dar tiempo a que los servicios inicien

echo "Backend:"
check_service "Eureka" "http://34.9.26.232:8761" || true
check_service "Gateway" "http://34.9.26.232:8090" || true

echo ""
echo "Frontend:"
check_service "Admin" "http://localhost:8080" || true
check_service "Client" "http://localhost:8081" || true

echo ""

# ============================================
# 5. MOSTRAR RESUMEN
# ============================================
echo "📊 ====================================="
echo "📊  RESUMEN DEL DESPLIEGUE"
echo "📊 ====================================="
echo ""
echo "🌐 URLs de Acceso:"
echo "   Admin:      http://localhost:8080"
echo "   Client:     http://localhost:8081"
echo "   Eureka:     http://34.9.26.232:8761"
echo "   Gateway:    http://34.9.26.232:8090"
echo "   RabbitMQ:   http://34.9.26.232:15672"
echo ""
echo "🔧 Servicios de Infraestructura:"
echo "   PostgreSQL: 34.9.26.232:5432"
echo "   Mosquitto:  34.9.26.232:1883"
echo "   RabbitMQ:   34.9.26.232:5672"
echo ""
echo "📝 Comandos Útiles:"
echo "   Ver logs backend:    cd $PROJECT_ROOT/movilidad-sostenible && docker-compose logs -f"
echo "   Ver logs frontend:   cd $PROJECT_ROOT/frontend && docker-compose logs -f"
echo "   Detener todo:        $0 stop"
echo "   Ver contenedores:    docker ps"
echo ""
echo "🦟 Probar Mosquitto:"
echo "   mosquitto_pub -h 34.9.26.232 -p 1883 -t 'test' -m 'hello'"
echo "   mosquitto_sub -h 34.9.26.232 -p 1883 -t '#' -v"
echo ""
print_success "¡Despliegue completado! 🚀"
echo ""
echo "Para simular telemetría de bicicletas:"
echo "   cd $PROJECT_ROOT/movilidad-sostenible/mosquitto"
echo "   BIKE_ID=123456 BROKER_HOST=34.9.26.232 INTERVAL=5 ./simulate.sh"
echo ""
