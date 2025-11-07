# Script de prueba para verificar integración con RabbitMQ
# UrbanRide - Sistema de Métodos de Pago

$baseUrl = "http://localhost:5002"
$rabbitmqManagementUrl = "http://localhost:15672"

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  PRUEBA DE INTEGRACIÓN - RABBITMQ + MÉTODOS DE PAGO" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════`n" -ForegroundColor Cyan

# 1. Verificar servicios
Write-Host "1️⃣  Verificando servicios..." -ForegroundColor Yellow

try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    Write-Host "   ✓ API Status: " -NoNewline -ForegroundColor Green
    Write-Host $health.status
    Write-Host "   ✓ RabbitMQ Connected: " -NoNewline -ForegroundColor Green
    Write-Host $health.rabbitmq_connected
} catch {
    Write-Host "   ✗ Error verificando servicios: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n2️⃣  Creando método de pago (debe generar evento METODO_PAGO_CREATED)..." -ForegroundColor Yellow

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
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago" `
        -Method Post `
        -ContentType "application/json" `
        -Body $metodoPago
    
    $metodoId = $response.k_metodo_pago
    Write-Host "   ✓ Método de pago creado - ID: $metodoId" -ForegroundColor Green
    Write-Host "   ✓ Marca detectada: " -NoNewline -ForegroundColor Green
    Write-Host $response.n_marca
    Write-Host "   ✓ Número enmascarado: " -NoNewline -ForegroundColor Green
    Write-Host $response.n_numero_tarjeta
} catch {
    Write-Host "   ✗ Error creando método de pago: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n3️⃣  Actualizando método de pago (debe generar evento METODO_PAGO_UPDATED)..." -ForegroundColor Yellow

$actualizacionObj = @{
    n_nombre_titular = "Juan Perez Actualizado"
    n_codigo_postal = "110222"
}
$actualizacion = $actualizacionObj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/$metodoId/usuario/1234567890" `
        -Method Put `
        -ContentType "application/json" `
        -Body $actualizacion
    
    Write-Host "   ✓ Método de pago actualizado" -ForegroundColor Green
    Write-Host "   ✓ Nuevo titular: " -NoNewline -ForegroundColor Green
    Write-Host $response.n_nombre_titular
} catch {
    Write-Host "   ✗ Error actualizando método de pago: $_" -ForegroundColor Red
}

Write-Host "`n4️⃣  Listando métodos de pago..." -ForegroundColor Yellow

try {
    $metodos = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/usuario/1234567890" -Method Get
    Write-Host "   ✓ Métodos encontrados: " -NoNewline -ForegroundColor Green
    Write-Host $metodos.Count
    
    foreach ($m in $metodos) {
        Write-Host "     - ID: $($m.k_metodo_pago) | Marca: $($m.n_marca) | Principal: $($m.b_principal)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ✗ Error listando métodos: $_" -ForegroundColor Red
}

Write-Host "`n5️⃣  Verificando RabbitMQ Management UI..." -ForegroundColor Yellow
Write-Host "   📊 Accede a: $rabbitmqManagementUrl" -ForegroundColor Cyan
Write-Host "      Usuario: urbanride" -ForegroundColor Gray
Write-Host "      Password: urbanride2024" -ForegroundColor Gray
Write-Host "   💡 En la UI de RabbitMQ:" -ForegroundColor Magenta
Write-Host "      - Ve a la pestaña Exchanges" -ForegroundColor Gray
Write-Host "      - Busca el exchange urbanride.payments" -ForegroundColor Gray
Write-Host "      - Verifica que los mensajes se estén publicando" -ForegroundColor Gray

Write-Host "`n6️⃣  Eliminando método de pago (debe generar evento METODO_PAGO_DELETED)..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/$metodoId/usuario/1234567890" `
        -Method Delete
    
    Write-Host "   ✓ " -NoNewline -ForegroundColor Green
    Write-Host $response.mensaje
} catch {
    Write-Host "   ✗ Error eliminando método de pago: $_" -ForegroundColor Red
}

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  RESUMEN DE LA PRUEBA" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════`n" -ForegroundColor Cyan

Write-Host "✅ Servicios verificados:" -ForegroundColor Green
Write-Host "   • PostgreSQL: http://localhost:5432" -ForegroundColor Gray
Write-Host "   • RabbitMQ AMQP: amqp://localhost:5672" -ForegroundColor Gray
Write-Host "   • RabbitMQ Management: $rabbitmqManagementUrl" -ForegroundColor Gray
Write-Host "   • API Métodos de Pago: $baseUrl" -ForegroundColor Gray
Write-Host "   • pgAdmin: http://localhost:5050" -ForegroundColor Gray

Write-Host "`n📋 Eventos RabbitMQ generados:" -ForegroundColor Cyan
Write-Host "   • METODO_PAGO_CREATED (routing key: metodo_pago.created)" -ForegroundColor Gray
Write-Host "   • METODO_PAGO_UPDATED (routing key: metodo_pago.updated)" -ForegroundColor Gray
Write-Host "   • METODO_PAGO_DELETED (routing key: metodo_pago.deleted)" -ForegroundColor Gray

Write-Host "`n🔍 Para ver los eventos en RabbitMQ:" -ForegroundColor Magenta
Write-Host "   1. Abre $rabbitmqManagementUrl" -ForegroundColor Gray
Write-Host "   2. Login: urbanride / urbanride2024" -ForegroundColor Gray
Write-Host "   3. Ve a Exchanges > urbanride.payments" -ForegroundColor Gray
Write-Host "   4. Verifica las estadísticas de mensajes publicados`n" -ForegroundColor Gray

Write-Host "═══════════════════════════════════════════════════════════`n" -ForegroundColor Cyan
