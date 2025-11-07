# Script de prueba para la API de Métodos de Pago
# PowerShell script

$baseUrl = "http://localhost:8080/api/metodos-pago"
$usuarioCC = "1234567890"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  Test API - Métodos de Pago    " -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Función para hacer requests
function Invoke-ApiTest {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [string]$Description
    )
    
    Write-Host "TEST: $Description" -ForegroundColor Yellow
    Write-Host "  $Method $Url" -ForegroundColor Gray
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers
        }
        
        Write-Host "  ✓ SUCCESS" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 3
        Write-Host ""
        return $response
    } catch {
        Write-Host "  ✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return $null
    }
}

# Esperar a que el servicio esté listo
Write-Host "Verificando que el servicio esté disponible..." -ForegroundColor Yellow
$maxRetries = 10
$retryCount = 0
$serviceReady = $false

while (-not $serviceReady -and $retryCount -lt $maxRetries) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
        if ($health.status -eq "UP") {
            $serviceReady = $true
            Write-Host "✓ Servicio listo" -ForegroundColor Green
            Write-Host ""
        }
    } catch {
        $retryCount++
        Write-Host "  Esperando... intento $retryCount de $maxRetries" -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if (-not $serviceReady) {
    Write-Host "ERROR: El servicio no está disponible. Asegúrate de que Docker esté corriendo." -ForegroundColor Red
    exit 1
}

# 1. Validar número de tarjeta
Write-Host "=== 1. Validar Número de Tarjeta ===" -ForegroundColor Cyan
$validarBody = @{
    numeroTarjeta = "4532015112830366"
} | ConvertTo-Json

Invoke-ApiTest -Method POST -Url "$baseUrl/validar" -Body $validarBody -Description "Validar tarjeta VISA"

# 2. Agregar método de pago
Write-Host "=== 2. Agregar Método de Pago ===" -ForegroundColor Cyan
$nuevoMetodo = @{
    k_usuarioCC = $usuarioCC
    t_tipoTarjeta = "CREDITO"
    n_numeroTarjetaCompleto = "4532015112830366"
    n_nombreTitular = "JUAN PEREZ TEST"
    f_fechaExpiracion = "2026-12-31"
    b_principal = $true
    n_direccionFacturacion = "Calle Test 123"
    n_codigoPostal = "110111"
} | ConvertTo-Json

$metodoPagoCreado = Invoke-ApiTest -Method POST -Url $baseUrl -Body $nuevoMetodo -Description "Crear método de pago"

# 3. Listar métodos de pago del usuario
Write-Host "=== 3. Listar Métodos de Pago ===" -ForegroundColor Cyan
$metodosPago = Invoke-ApiTest -Method GET -Url "$baseUrl/usuario/$usuarioCC" -Description "Listar métodos de pago del usuario"

# 4. Obtener método de pago principal
Write-Host "=== 4. Obtener Método Principal ===" -ForegroundColor Cyan
Invoke-ApiTest -Method GET -Url "$baseUrl/usuario/$usuarioCC/principal" -Description "Obtener método de pago principal"

# 5. Agregar segundo método de pago
Write-Host "=== 5. Agregar Segundo Método ===" -ForegroundColor Cyan
$segundoMetodo = @{
    k_usuarioCC = $usuarioCC
    t_tipoTarjeta = "DEBITO"
    n_numeroTarjetaCompleto = "5425233430109903"
    n_nombreTitular = "JUAN PEREZ TEST"
    f_fechaExpiracion = "2025-06-30"
    b_principal = $false
    n_direccionFacturacion = "Calle Test 123"
    n_codigoPostal = "110111"
} | ConvertTo-Json

$segundoMetodoCreado = Invoke-ApiTest -Method POST -Url $baseUrl -Body $segundoMetodo -Description "Crear segundo método de pago"

# 6. Listar todos nuevamente
Write-Host "=== 6. Listar Todos los Métodos ===" -ForegroundColor Cyan
$todosMetodos = Invoke-ApiTest -Method GET -Url "$baseUrl/usuario/$usuarioCC" -Description "Listar todos los métodos"

if ($todosMetodos -and $todosMetodos.Count -ge 2) {
    $idSegundo = $todosMetodos[1].k_metodoPago
    
    # 7. Establecer segundo como principal
    Write-Host "=== 7. Cambiar Método Principal ===" -ForegroundColor Cyan
    Invoke-ApiTest -Method PATCH -Url "$baseUrl/$idSegundo/usuario/$usuarioCC/principal" -Description "Establecer segundo método como principal"
    
    # 8. Actualizar método de pago
    Write-Host "=== 8. Actualizar Método de Pago ===" -ForegroundColor Cyan
    $actualizacion = @{
        n_nombreTitular = "JUAN PEREZ ACTUALIZADO"
        n_direccionFacturacion = "Nueva Dirección 456"
    } | ConvertTo-Json
    
    Invoke-ApiTest -Method PUT -Url "$baseUrl/$idSegundo/usuario/$usuarioCC" -Body $actualizacion -Description "Actualizar información del método"
    
    # 9. Eliminar primer método
    Write-Host "=== 9. Eliminar Método de Pago ===" -ForegroundColor Cyan
    $idPrimero = $todosMetodos[0].k_metodoPago
    Invoke-ApiTest -Method DELETE -Url "$baseUrl/$idPrimero/usuario/$usuarioCC" -Description "Eliminar primer método de pago"
}

# 10. Listar métodos finales
Write-Host "=== 10. Estado Final ===" -ForegroundColor Cyan
Invoke-ApiTest -Method GET -Url "$baseUrl/usuario/$usuarioCC" -Description "Listar métodos de pago finales"

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "  Tests completados              " -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
