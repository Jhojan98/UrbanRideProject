#!/bin/bash

echo "🔄 Deteniendo contenedores actuales..."
sudo docker compose down

echo "🗑️  Eliminando imágenes antiguas..."
sudo docker rmi frontend-admin-frontend frontend-client-frontend 2>/dev/null || true

echo "🏗️  Reconstruyendo imágenes con nueva configuración Firebase..."
sudo docker compose build --no-cache

echo "🚀 Iniciando contenedores..."
sudo docker compose up -d

echo "✅ Contenedores reconstruidos!"
echo ""
echo "📊 Estado de los contenedores:"
sudo docker compose ps

echo ""
echo "🌐 URLs disponibles:"
echo "   Admin:  http://localhost:8080"
echo "   Client: http://localhost:8081"
echo ""
echo "📝 Para ver logs:"
echo "   Admin:  sudo docker logs ecoride-admin -f"
echo "   Client: sudo docker logs ecoride-client -f"
