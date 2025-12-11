#!/bin/bash

# 🛑 Script para Detener Todos los Servicios - EcoRide

echo "🛑 Deteniendo todos los servicios de EcoRide..."
echo ""

PROJECT_ROOT="/home/xmara83/Escritorio/UD/UrbanRideProject"

# Detener Frontend
echo "🎨 Deteniendo Frontend..."
cd "$PROJECT_ROOT/frontend"
docker-compose down
echo "✅ Frontend detenido"
echo ""

# Detener Backend
echo "🔧 Deteniendo Backend..."
cd "$PROJECT_ROOT/movilidad-sostenible"
docker-compose down
echo "✅ Backend detenido"
echo ""

# Mostrar contenedores que aún están corriendo
echo "📊 Contenedores activos:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo "✅ Todos los servicios han sido detenidos"
