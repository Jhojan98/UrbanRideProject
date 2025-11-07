# Script de prueba para Recarga de Saldo MOCK
# UrbanRide - Sistema de Bicicletas

$baseUrl = "http://localhost:5002"

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " PRUEBA DE RECARGA DE SALDO - MOCK" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# 1. Crear método de pago para pruebas
Write-Host "[1] Creando método de pago para pruebas..." -ForegroundColor Yellow

$metodoPagoObj = @{
    k_usuario_cc = "1234567890"
    t_tipo_tarjeta = "CREDITO"
    n_nombre_titular = "Maria Rodriguez"
    f_fecha_expiracion = "2027-12-31"
    n_numero_tarjeta_completo = "5425233430109903"
    b_principal = $true
    n_direccion_facturacion = "Carrera 7 #10-20"
    n_codigo_postal = "110111"
}
$metodoPago = $metodoPagoObj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago" -Method Post -ContentType "application/json" -Body $metodoPago
    $metodoId = $response.k_metodo_pago
    Write-Host "   [OK] Método de pago creado - ID: $metodoId" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($response.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Tarjeta: $($response.n_numero_tarjeta)" -ForegroundColor Green
    Write-Host "   [OK] Saldo inicial: `$$($response.v_saldo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error creando método de pago: $_" -ForegroundColor Red
    exit 1
}

# 2. Consultar saldo inicial
Write-Host "`n[2] Consultando saldo inicial..." -ForegroundColor Yellow

try {
    $saldo = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/saldo/$metodoId" -Method Get
    Write-Host "   [OK] Saldo actual: `$$($saldo.saldo_actual)" -ForegroundColor Green
    Write-Host "   [OK] Usuario: $($saldo.k_usuario_cc)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error consultando saldo: $_" -ForegroundColor Red
}

# 3. Primera recarga
Write-Host "`n[3] Realizando primera recarga de `$50,000..." -ForegroundColor Yellow

$recarga1Obj = @{
    k_metodo_pago = $metodoId
    monto = 50000
    descripcion = "Recarga inicial - Prueba"
}
$recarga1 = $recarga1Obj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recarga1
    Write-Host "   [OK] $($response.mensaje)" -ForegroundColor Green
    Write-Host "   [OK] Saldo anterior: `$$($response.saldo_anterior)" -ForegroundColor Gray
    Write-Host "   [OK] Monto recargado: `$$($response.monto_recargado)" -ForegroundColor Cyan
    Write-Host "   [OK] Saldo nuevo: `$$($response.saldo_nuevo)" -ForegroundColor Green
    Write-Host "   [OK] Fecha: $($response.fecha_recarga)" -ForegroundColor Gray
} catch {
    Write-Host "   [ERROR] Error en recarga: $_" -ForegroundColor Red
    exit 1
}

# 4. Segunda recarga
Write-Host "`n[4] Realizando segunda recarga de `$25,000..." -ForegroundColor Yellow

$recarga2Obj = @{
    k_metodo_pago = $metodoId
    monto = 25000
    descripcion = "Recarga adicional"
}
$recarga2 = $recarga2Obj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recarga2
    Write-Host "   [OK] $($response.mensaje)" -ForegroundColor Green
    Write-Host "   [OK] Saldo anterior: `$$($response.saldo_anterior)" -ForegroundColor Gray
    Write-Host "   [OK] Monto recargado: `$$($response.monto_recargado)" -ForegroundColor Cyan
    Write-Host "   [OK] Saldo nuevo: `$$($response.saldo_nuevo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error en recarga: $_" -ForegroundColor Red
}

# 5. Tercera recarga
Write-Host "`n[5] Realizando tercera recarga de `$100,000..." -ForegroundColor Yellow

$recarga3Obj = @{
    k_metodo_pago = $metodoId
    monto = 100000
    descripcion = "Recarga mensual"
}
$recarga3 = $recarga3Obj | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recarga3
    Write-Host "   [OK] $($response.mensaje)" -ForegroundColor Green
    Write-Host "   [OK] Saldo anterior: `$$($response.saldo_anterior)" -ForegroundColor Gray
    Write-Host "   [OK] Monto recargado: `$$($response.monto_recargado)" -ForegroundColor Cyan
    Write-Host "   [OK] Saldo nuevo: `$$($response.saldo_nuevo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Error en recarga: $_" -ForegroundColor Red
}

# 6. Consultar saldo final
Write-Host "`n[6] Consultando saldo final..." -ForegroundColor Yellow

try {
    $saldo = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/saldo/$metodoId" -Method Get
    Write-Host "   [OK] Saldo final: `$$($saldo.saldo_actual)" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($saldo.n_marca)" -ForegroundColor Gray
    Write-Host "   [OK] Tarjeta: $($saldo.n_numero_tarjeta)" -ForegroundColor Gray
} catch {
    Write-Host "   [ERROR] Error consultando saldo: $_" -ForegroundColor Red
}

# 7. Consultar saldo total del usuario
Write-Host "`n[7] Consultando saldo total del usuario..." -ForegroundColor Yellow

try {
    $saldoTotal = Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/usuario/1234567890/saldo-total" -Method Get
    Write-Host "   [OK] Saldo total: `$$($saldoTotal.saldo_total)" -ForegroundColor Green
    Write-Host "   [OK] Cantidad de métodos: $($saldoTotal.cantidad_metodos)" -ForegroundColor Gray
    
    Write-Host "`n   Detalle de métodos:" -ForegroundColor Cyan
    foreach ($metodo in $saldoTotal.metodos_pago) {
        $principal = if ($metodo.b_principal) { "★ Principal" } else { "" }
        Write-Host "      - ID: $($metodo.k_metodo_pago) | $($metodo.n_marca) | $($metodo.n_numero_tarjeta) | Saldo: `$$($metodo.saldo) $principal" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [ERROR] Error consultando saldo total: $_" -ForegroundColor Red
}

# 8. Probar validaciones
Write-Host "`n[8] Probando validaciones..." -ForegroundColor Yellow

# Monto muy bajo
Write-Host "   [a] Intentando recarga con monto menor al mínimo (`$500)..." -ForegroundColor Magenta
$recargaBajaObj = @{
    k_metodo_pago = $metodoId
    monto = 500
}
$recargaBaja = $recargaBajaObj | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recargaBaja
    Write-Host "       [WARN] No se validó el monto mínimo" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: $_" -ForegroundColor Green
}

# Monto muy alto
Write-Host "`n   [b] Intentando recarga con monto mayor al máximo (`$10,000,000)..." -ForegroundColor Magenta
$recargaAltaObj = @{
    k_metodo_pago = $metodoId
    monto = 10000000
}
$recargaAlta = $recargaAltaObj | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recargaAlta
    Write-Host "       [WARN] No se validó el monto máximo" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: $_" -ForegroundColor Green
}

# Método de pago inexistente
Write-Host "`n   [c] Intentando recarga con método de pago inexistente..." -ForegroundColor Magenta
$recargaInvalidaObj = @{
    k_metodo_pago = 99999
    monto = 10000
}
$recargaInvalida = $recargaInvalidaObj | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/api/metodos-pago/recarga" -Method Post -ContentType "application/json" -Body $recargaInvalida
    Write-Host "       [WARN] No se validó el método de pago" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: Método no encontrado" -ForegroundColor Green
}

# 9. Verificar eventos en RabbitMQ
Write-Host "`n[9] Verificar eventos en RabbitMQ Management..." -ForegroundColor Yellow
Write-Host "   [INFO] Abre: http://localhost:15672" -ForegroundColor Cyan
Write-Host "   [INFO] Login: urbanride / urbanride2024" -ForegroundColor Gray
Write-Host "   [INFO] Ve a Exchanges > urbanride.payments" -ForegroundColor Gray
Write-Host "   [INFO] Busca eventos con routing key: metodo_pago.recarga" -ForegroundColor Gray

# Resumen
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " RESUMEN DE PRUEBAS DE RECARGA" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "[OK] Funcionalidades probadas:" -ForegroundColor Green
Write-Host "   * Creación de método de pago con saldo inicial (`$0)" -ForegroundColor Gray
Write-Host "   * Consulta de saldo individual" -ForegroundColor Gray
Write-Host "   * Recarga de saldo (3 recargas exitosas)" -ForegroundColor Gray
Write-Host "   * Consulta de saldo total del usuario" -ForegroundColor Gray
Write-Host "   * Validaciones de monto mínimo y máximo" -ForegroundColor Gray
Write-Host "   * Validación de método de pago inexistente" -ForegroundColor Gray

Write-Host "`n[INFO] Endpoints disponibles:" -ForegroundColor Cyan
Write-Host "   * POST /api/metodos-pago/recarga - Recargar saldo" -ForegroundColor Gray
Write-Host "   * GET /api/metodos-pago/saldo/{id} - Consultar saldo" -ForegroundColor Gray
Write-Host "   * GET /api/metodos-pago/usuario/{cc}/saldo-total - Saldo total" -ForegroundColor Gray

Write-Host "`n[INFO] Eventos RabbitMQ:" -ForegroundColor Cyan
Write-Host "   * Routing key: metodo_pago.recarga" -ForegroundColor Gray
Write-Host "   * Event type: SALDO_RECARGADO" -ForegroundColor Gray

Write-Host "`n[TIP] Para documentación completa:" -ForegroundColor Magenta
Write-Host "   Abre: http://localhost:5002/docs`n" -ForegroundColor Gray

Write-Host "============================================================`n" -ForegroundColor Cyan
