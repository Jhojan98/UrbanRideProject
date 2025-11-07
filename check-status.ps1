# Script para verificar el estado de los servicios Docker
# PowerShell script

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  UrbanRide - Estado de Servicios" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Verificar contenedores
Write-Host "📦 Contenedores:" -ForegroundColor Yellow
docker ps -a --filter "name=urbanride" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""

# Verificar si el servicio responde
Write-Host "🏥 Health Check:" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 2
    Write-Host "  ✓ Usuario Service: UP" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "  ✗ Usuario Service: No disponible (puede estar iniciando...)" -ForegroundColor Yellow
}
Write-Host ""

# Verificar PostgreSQL
Write-Host "🐘 PostgreSQL:" -ForegroundColor Yellow
try {
    $pgResult = docker exec urbanride-postgres pg_isready -U urbanride 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ PostgreSQL: Disponible" -ForegroundColor Green
    } else {
        Write-Host "  ✗ PostgreSQL: No disponible" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ PostgreSQL: Error al verificar" -ForegroundColor Red
}
Write-Host ""

# Mostrar últimos logs del servicio
Write-Host "📋 Últimos logs del Usuario Service:" -ForegroundColor Yellow
docker logs urbanride-usuario-service --tail 30 2>&1
Write-Host ""

Write-Host "💡 Comandos útiles:" -ForegroundColor Cyan
Write-Host "  Ver logs en tiempo real: docker-compose logs -f usuario-service" -ForegroundColor White
Write-Host "  Reiniciar servicio: docker-compose restart usuario-service" -ForegroundColor White
Write-Host "  Ver todos los servicios: docker-compose ps" -ForegroundColor White
