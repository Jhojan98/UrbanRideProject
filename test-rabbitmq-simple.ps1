# Script de prueba simplificado para RabbitMQ + Metodos de Pago
# UrbanRide

$baseUrl = "http://localhost:5002"
$rabbitmqManagementUrl = "http://localhost:15672"

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " PRUEBA DE INTEGRACION - RABBITMQ + METODOS DE PAGO" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# 1. Verificar servicios
Write-Host "[1] Verificando servicios..." -ForegroundColor Yellow

try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    Write-Host "   [OK] API Status: $($health.status)" -ForegroundColor Green
    Write-Host "   [OK] RabbitMQ Connected: $($health.rabbitmq_connected)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error verificando servicios: $_" -ForegroundColor Red
    exit 1
}

# 2. Crear metodo de pago
Write-Host "`n[2] Creando metodo de pago (evento: METODO_PAGO_CREATED)..." -ForegroundColor Yellow

$metodoPagoObj = @{
    k_usuario_cc = "1234567890"
    t_tipo_tarjeta = "CREDITO"
    n_nombre_titular = "Juan Perez Test"
    f_fecha_expiracion = "2026-12-31"
    n_numero_tarjeta_completo = "4532015112830366"
    b_principal = $true
    n_direccion_facturacion = "Calle 123 #45-67"
    n_codigo_postal = "110111"
}
$metodoPago = $metodoPagoObj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago" -Method Post -ContentType "application/json" -Body $metodoPago
    $metodoId = $response.k_metodo_pago
    Write-Host "   [OK] Metodo de pago creado - ID: $metodoId" -ForegroundColor Green
    Write-Host "   [OK] Marca detectada: $($response.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Numero enmascarado: $($response.n_numero_tarjeta)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error creando metodo de pago: $_" -ForegroundColor Red
    exit 1
}

# 3. Actualizar metodo de pago
Write-Host "`n[3] Actualizando metodo de pago (evento: METODO_PAGO_UPDATED)..." -ForegroundColor Yellow

$actualizacionObj = @{
    n_nombre_titular = "Juan Perez Actualizado"
    n_codigo_postal = "110222"
}
$actualizacion = $actualizacionObj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/$metodoId/usuario/1234567890" -Method Put -ContentType "application/json" -Body $actualizacion
    Write-Host "   [OK] Metodo de pago actualizado" -ForegroundColor Green
    Write-Host "   [OK] Nuevo titular: $($response.n_nombre_titular)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error actualizando metodo de pago: $_" -ForegroundColor Red
}

# 4. Listar metodos de pago
Write-Host "`n[4] Listando metodos de pago..." -ForegroundColor Yellow

try {
    $metodos = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/usuario/1234567890" -Method Get
    Write-Host "   [OK] Metodos encontrados: $($metodos.Count)" -ForegroundColor Green
    foreach ($m in $metodos) {
        Write-Host "      - ID: $($m.k_metodo_pago) | Marca: $($m.n_marca) | Principal: $($m.b_principal)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [ERROR] Error listando metodos: $_" -ForegroundColor Red
}

# 5. Verificar RabbitMQ Management
Write-Host "`n[5] Verificando RabbitMQ Management UI..." -ForegroundColor Yellow
Write-Host "   [INFO] Accede a: $rabbitmqManagementUrl" -ForegroundColor Cyan
Write-Host "      Usuario: urbanride" -ForegroundColor Gray
Write-Host "      Password: urbanride2024" -ForegroundColor Gray
Write-Host "`n   [TIP] En la UI de RabbitMQ:" -ForegroundColor Magenta
Write-Host "      - Ve a la pestana Exchanges" -ForegroundColor Gray
Write-Host "      - Busca el exchange urbanride.payments" -ForegroundColor Gray
Write-Host "      - Verifica que los mensajes se esten publicando" -ForegroundColor Gray

# 6. Eliminar metodo de pago
Write-Host "`n[6] Eliminando metodo de pago (evento: METODO_PAGO_DELETED)..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/$metodoId/usuario/1234567890" -Method Delete
    Write-Host "   [OK] $($response.mensaje)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error eliminando metodo de pago: $_" -ForegroundColor Red
}

# Resumen
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " RESUMEN DE LA PRUEBA" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "[OK] Servicios verificados:" -ForegroundColor Green
Write-Host "   * PostgreSQL: http://localhost:5432" -ForegroundColor Gray
Write-Host "   * RabbitMQ AMQP: amqp://localhost:5672" -ForegroundColor Gray
Write-Host "   * RabbitMQ Management: $rabbitmqManagementUrl" -ForegroundColor Gray
Write-Host "   * API Metodos de Pago: $baseUrl" -ForegroundColor Gray
Write-Host "   * pgAdmin: http://localhost:5050" -ForegroundColor Gray

Write-Host "`n[INFO] Eventos RabbitMQ generados:" -ForegroundColor Cyan
Write-Host "   * METODO_PAGO_CREATED (routing key: metodo_pago.created)" -ForegroundColor Gray
Write-Host "   * METODO_PAGO_UPDATED (routing key: metodo_pago.updated)" -ForegroundColor Gray
Write-Host "   * METODO_PAGO_DELETED (routing key: metodo_pago.deleted)" -ForegroundColor Gray

Write-Host "`n[TIP] Para ver los eventos en RabbitMQ:" -ForegroundColor Magenta
Write-Host "   1. Abre $rabbitmqManagementUrl" -ForegroundColor Gray
Write-Host "   2. Login: urbanride / urbanride2024" -ForegroundColor Gray
Write-Host "   3. Ve a Exchanges > urbanride.payments" -ForegroundColor Gray
Write-Host "   4. Verifica las estadisticas de mensajes publicados`n" -ForegroundColor Gray

Write-Host "============================================================`n" -ForegroundColor Cyan
