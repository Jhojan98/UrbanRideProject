#!/bin/bash

# Script de ayuda para gestionar contenedores Docker de Frontend

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funciones auxiliares
log_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

log_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

log_warn() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

log_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

# Verificar que Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker no está instalado"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose no está instalado"
        exit 1
    fi

    log_success "Docker detectado"
}

# Crear red si no existe
ensure_network() {
    if ! docker network ls | grep -q "urbanride-network"; then
        log_info "Creando red urbanride-network..."
        docker network create urbanride-network
        log_success "Red creada"
    else
        log_success "Red urbanride-network ya existe"
    fi
}

# Mostrar menú
show_menu() {
    echo ""
    echo -e "${BLUE}=== Frontend Docker Manager ===${NC}"
    echo ""
    echo "1. Construir e iniciar (producción)"
    echo "2. Construir e iniciar (desarrollo)"
    echo "3. Detener contenedores"
    echo "4. Detener y eliminar"
    echo "5. Ver logs (admin)"
    echo "6. Ver logs (client)"
    echo "7. Reconstruir sin caché"
    echo "8. Estado de contenedores"
    echo "9. Salir"
    echo ""
}

# Funciones principales
start_production() {
    log_info "Iniciando en modo producción..."
    ensure_network
    docker-compose up -d --build
    log_success "Contenedores iniciados"
    log_info "Admin: http://localhost:8001"
    log_info "Client: http://localhost:8002"
}

start_development() {
    log_info "Iniciando en modo desarrollo..."
    ensure_network
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    log_success "Contenedores iniciados en modo desarrollo"
    log_info "Admin: http://localhost:8001"
    log_info "Client: http://localhost:8002"
}

stop_containers() {
    log_info "Deteniendo contenedores..."
    docker-compose stop
    log_success "Contenedores detenidos"
}

remove_containers() {
    log_info "Deteniendo y eliminando contenedores..."
    docker-compose down
    log_success "Contenedores eliminados"
}

view_admin_logs() {
    log_info "Mostrando logs de admin-frontend..."
    docker logs -f admin-frontend
}

view_client_logs() {
    log_info "Mostrando logs de client-frontend..."
    docker logs -f client-frontend
}

rebuild_nocache() {
    log_info "Reconstruyendo sin caché..."
    ensure_network
    docker-compose up -d --build --no-cache
    log_success "Reconstrucción completada"
}

show_status() {
    log_info "Estado de contenedores:"
    docker-compose ps
}

# Main
main() {
    check_docker

    if [ $# -eq 0 ]; then
        # Modo interactivo
        while true; do
            show_menu
            read -p "Selecciona una opción (1-9): " choice
            echo ""

            case $choice in
                1) start_production ;;
                2) start_development ;;
                3) stop_containers ;;
                4) remove_containers ;;
                5) view_admin_logs ;;
                6) view_client_logs ;;
                7) rebuild_nocache ;;
                8) show_status ;;
                9) log_info "Saliendo..."; exit 0 ;;
                *) log_error "Opción inválida" ;;
            esac
        done
    else
        # Modo comando directo
        case $1 in
            prod|production) start_production ;;
            dev|development) start_development ;;
            stop) stop_containers ;;
            remove|down) remove_containers ;;
            logs:admin) view_admin_logs ;;
            logs:client) view_client_logs ;;
            rebuild) rebuild_nocache ;;
            status|ps) show_status ;;
            *)
                log_error "Comando desconocido: $1"
                echo "Comandos disponibles: prod, dev, stop, remove, logs:admin, logs:client, rebuild, status"
                exit 1
                ;;
        esac
    fi
}

main "$@"
