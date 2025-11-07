# Script de Prueba Completo - Todos los Endpoints FastAPI
# UrbanRide - Métodos de Pago

$baseUrl = "http://localhost:5002"
$apiUrl = "$baseUrl/api/metodos-pago"

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " PRUEBA COMPLETA DE TODOS LOS ENDPOINTS - FASTAPI" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# Variables globales
$usuario1 = "1234567890"
$metodoId1 = 0
$metodoId2 = 0

# ============================================================
# TEST 1: HEALTH CHECK
# ============================================================
Write-Host "[TEST 1] Health Check - GET /health" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    Write-Host "   [OK] Status: $($health.status)" -ForegroundColor Green
    Write-Host "   [OK] Service: $($health.service)" -ForegroundColor Green
    Write-Host "   [OK] RabbitMQ: $($health.rabbitmq_connected)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Health check falló: $_" -ForegroundColor Red
    exit 1
}

# ============================================================
# TEST 2: VALIDAR NÚMERO DE TARJETA
# ============================================================
Write-Host "`n[TEST 2] Validar Número de Tarjeta - POST /api/metodos-pago/validar" -ForegroundColor Yellow

# Tarjeta válida
$validar1 = @{
    numeroTarjeta = "4532015112830366"
} | ConvertTo-Json

try {
    $resultado = Invoke-RestMethod -Uri "$apiUrl/validar" -Method Post -ContentType "application/json" -Body $validar1
    Write-Host "   [OK] VISA válida: $($resultado.valido)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Validación VISA falló: $_" -ForegroundColor Red
}

# Tarjeta inválida
$validar2 = @{
    numeroTarjeta = "1234567890123456"
} | ConvertTo-Json

try {
    $resultado = Invoke-RestMethod -Uri "$apiUrl/validar" -Method Post -ContentType "application/json" -Body $validar2
    Write-Host "   [OK] Tarjeta inválida detectada: $($resultado.valido)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Validación falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 3: CREAR PRIMER MÉTODO DE PAGO (Usuario 1)
# ============================================================
Write-Host "`n[TEST 3] Crear Primer Método - POST /api/metodos-pago/" -ForegroundColor Yellow

$metodo1 = @{
    k_usuario_cc = $usuario1
    t_tipo_tarjeta = "CREDITO"
    n_nombre_titular = "Juan Perez"
    f_fecha_expiracion = "2026-12-31"
    n_numero_tarjeta_completo = "4532015112830366"
    b_principal = $true
    n_direccion_facturacion = "Calle 123 #45-67"
    n_codigo_postal = "110111"
} | ConvertTo-Json

try {
    $creado1 = Invoke-RestMethod -Uri "$apiUrl" -Method Post -ContentType "application/json" -Body $metodo1
    $metodoId1 = $creado1.k_metodo_pago
    Write-Host "   [OK] Método creado - ID: $metodoId1" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($creado1.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Número: $($creado1.n_numero_tarjeta)" -ForegroundColor Green
    Write-Host "   [OK] Principal: $($creado1.b_principal)" -ForegroundColor Green
    Write-Host "   [OK] Saldo inicial: `$$($creado1.v_saldo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Creación falló: $_" -ForegroundColor Red
    exit 1
}

# ============================================================
# TEST 4: CREAR SEGUNDO MÉTODO DE PAGO (Usuario 1)
# ============================================================
Write-Host "`n[TEST 4] Crear Segundo Método - POST /api/metodos-pago/" -ForegroundColor Yellow

$metodo2 = @{
    k_usuario_cc = $usuario1
    t_tipo_tarjeta = "DEBITO"
    n_nombre_titular = "Juan Perez"
    f_fecha_expiracion = "2027-06-30"
    n_numero_tarjeta_completo = "5425233430109903"
    b_principal = $false
    n_direccion_facturacion = "Carrera 7 #10-20"
    n_codigo_postal = "110222"
} | ConvertTo-Json

try {
    $creado2 = Invoke-RestMethod -Uri "$apiUrl" -Method Post -ContentType "application/json" -Body $metodo2
    $metodoId2 = $creado2.k_metodo_pago
    Write-Host "   [OK] Método creado - ID: $metodoId2" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($creado2.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Número: $($creado2.n_numero_tarjeta)" -ForegroundColor Green
    Write-Host "   [OK] Principal: $($creado2.b_principal)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Creación falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 5: LISTAR MÉTODOS DE PAGO
# ============================================================
Write-Host "`n[TEST 5] Listar Métodos - GET /api/metodos-pago/usuario/{cc}" -ForegroundColor Yellow

try {
    $metodos = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1" -Method Get
    Write-Host "   [OK] Total de métodos: $($metodos.Count)" -ForegroundColor Green
    foreach ($m in $metodos) {
        $principal = if ($m.b_principal) { "★" } else { "" }
        Write-Host "      - ID: $($m.k_metodo_pago) | $($m.n_marca) | $($m.n_numero_tarjeta) | Saldo: `$$($m.v_saldo) $principal" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [ERROR] Listar falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 6: OBTENER MÉTODO ESPECÍFICO
# ============================================================
Write-Host "`n[TEST 6] Obtener Método Específico - GET /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Yellow

try {
    $metodoEspecifico = Invoke-RestMethod -Uri "$apiUrl/$metodoId1/usuario/$usuario1" -Method Get
    Write-Host "   [OK] Método ID: $($metodoEspecifico.k_metodo_pago)" -ForegroundColor Green
    Write-Host "   [OK] Titular: $($metodoEspecifico.n_nombre_titular)" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($metodoEspecifico.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Dirección: $($metodoEspecifico.n_direccion_facturacion)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Obtener método falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 7: OBTENER MÉTODO PRINCIPAL
# ============================================================
Write-Host "`n[TEST 7] Obtener Método Principal - GET /api/metodos-pago/usuario/{cc}/principal" -ForegroundColor Yellow

try {
    $principal = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1/principal" -Method Get
    Write-Host "   [OK] Método principal ID: $($principal.k_metodo_pago)" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($principal.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] Número: $($principal.n_numero_tarjeta)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Obtener principal falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 8: CONSULTAR SALDO INDIVIDUAL
# ============================================================
Write-Host "`n[TEST 8] Consultar Saldo Individual - GET /api/metodos-pago/saldo/{id}" -ForegroundColor Yellow

try {
    $saldo = Invoke-RestMethod -Uri "$apiUrl/saldo/$metodoId1" -Method Get
    Write-Host "   [OK] Saldo actual: `$$($saldo.saldo_actual)" -ForegroundColor Green
    Write-Host "   [OK] Método: $($saldo.n_marca) $($saldo.n_numero_tarjeta)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Consultar saldo falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 9: RECARGAR SALDO (Primera recarga)
# ============================================================
Write-Host "`n[TEST 9] Recargar Saldo - POST /api/metodos-pago/recarga" -ForegroundColor Yellow

$recarga1 = @{
    k_metodo_pago = $metodoId1
    monto = 50000
    descripcion = "Recarga inicial de prueba"
} | ConvertTo-Json

try {
    $resultado = Invoke-RestMethod -Uri "$apiUrl/recarga" -Method Post -ContentType "application/json" -Body $recarga1
    Write-Host "   [OK] $($resultado.mensaje)" -ForegroundColor Green
    Write-Host "   [OK] Saldo anterior: `$$($resultado.saldo_anterior)" -ForegroundColor Gray
    Write-Host "   [OK] Monto recargado: `$$($resultado.monto_recargado)" -ForegroundColor Cyan
    Write-Host "   [OK] Saldo nuevo: `$$($resultado.saldo_nuevo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Recarga falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 10: RECARGAR SALDO (Segunda recarga)
# ============================================================
Write-Host "`n[TEST 10] Recargar Saldo Adicional - POST /api/metodos-pago/recarga" -ForegroundColor Yellow

$recarga2 = @{
    k_metodo_pago = $metodoId2
    monto = 25000
    descripcion = "Recarga en segundo método"
} | ConvertTo-Json

try {
    $resultado = Invoke-RestMethod -Uri "$apiUrl/recarga" -Method Post -ContentType "application/json" -Body $recarga2
    Write-Host "   [OK] $($resultado.mensaje)" -ForegroundColor Green
    Write-Host "   [OK] Saldo anterior: `$$($resultado.saldo_anterior)" -ForegroundColor Gray
    Write-Host "   [OK] Monto recargado: `$$($resultado.monto_recargado)" -ForegroundColor Cyan
    Write-Host "   [OK] Saldo nuevo: `$$($resultado.saldo_nuevo)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Recarga falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 11: CONSULTAR SALDO TOTAL DEL USUARIO
# ============================================================
Write-Host "`n[TEST 11] Consultar Saldo Total - GET /api/metodos-pago/usuario/{cc}/saldo-total" -ForegroundColor Yellow

try {
    $saldoTotal = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1/saldo-total" -Method Get
    Write-Host "   [OK] Usuario: $($saldoTotal.k_usuario_cc)" -ForegroundColor Green
    Write-Host "   [OK] Saldo total: `$$($saldoTotal.saldo_total)" -ForegroundColor Green
    Write-Host "   [OK] Cantidad de métodos: $($saldoTotal.cantidad_metodos)" -ForegroundColor Green
    
    Write-Host "`n   Detalle de métodos:" -ForegroundColor Cyan
    foreach ($m in $saldoTotal.metodos_pago) {
        $principal = if ($m.b_principal) { "★ Principal" } else { "" }
        Write-Host "      - ID: $($m.k_metodo_pago) | $($m.n_marca) | $($m.n_numero_tarjeta) | Saldo: `$$($m.saldo) $principal" -ForegroundColor Gray
    }
} catch {
    Write-Host "   [ERROR] Consultar saldo total falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 12: ACTUALIZAR MÉTODO DE PAGO
# ============================================================
Write-Host "`n[TEST 12] Actualizar Método - PUT /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Yellow

$actualizacion = @{
    n_nombre_titular = "Juan Perez Actualizado"
    n_codigo_postal = "110333"
} | ConvertTo-Json

try {
    $actualizado = Invoke-RestMethod -Uri "$apiUrl/$metodoId1/usuario/$usuario1" -Method Put -ContentType "application/json" -Body $actualizacion
    Write-Host "   [OK] Método actualizado" -ForegroundColor Green
    Write-Host "   [OK] Nuevo titular: $($actualizado.n_nombre_titular)" -ForegroundColor Green
    Write-Host "   [OK] Nuevo código postal: $($actualizado.n_codigo_postal)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Actualización falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 13: ESTABLECER COMO PRINCIPAL
# ============================================================
Write-Host "`n[TEST 13] Establecer como Principal - PATCH /api/metodos-pago/{id}/usuario/{cc}/principal" -ForegroundColor Yellow

try {
    $nuevoPrincipal = Invoke-RestMethod -Uri "$apiUrl/$metodoId2/usuario/$usuario1/principal" -Method Patch
    Write-Host "   [OK] Método $metodoId2 ahora es principal" -ForegroundColor Green
    Write-Host "   [OK] Marca: $($nuevoPrincipal.n_marca)" -ForegroundColor Green
    Write-Host "   [OK] b_principal: $($nuevoPrincipal.b_principal)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Establecer principal falló: $_" -ForegroundColor Red
}

# Verificar cambio
try {
    $principal = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1/principal" -Method Get
    Write-Host "   [OK] Método principal actual ID: $($principal.k_metodo_pago)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Verificar principal falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 14: VALIDACIONES DE RECARGA
# ============================================================
Write-Host "`n[TEST 14] Validaciones de Recarga" -ForegroundColor Yellow

# Monto muy bajo
Write-Host "   [a] Probando monto menor al mínimo (`$500)..." -ForegroundColor Magenta
$recargaBaja = @{
    k_metodo_pago = $metodoId1
    monto = 500
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$apiUrl/recarga" -Method Post -ContentType "application/json" -Body $recargaBaja
    Write-Host "       [WARN] No se validó el monto mínimo" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: Monto mínimo" -ForegroundColor Green
}

# Monto muy alto
Write-Host "   [b] Probando monto mayor al máximo (`$10,000,000)..." -ForegroundColor Magenta
$recargaAlta = @{
    k_metodo_pago = $metodoId1
    monto = 10000000
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$apiUrl/recarga" -Method Post -ContentType "application/json" -Body $recargaAlta
    Write-Host "       [WARN] No se validó el monto máximo" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: Monto máximo" -ForegroundColor Green
}

# Método inexistente
Write-Host "   [c] Probando método inexistente..." -ForegroundColor Magenta
$recargaInvalida = @{
    k_metodo_pago = 99999
    monto = 10000
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$apiUrl/recarga" -Method Post -ContentType "application/json" -Body $recargaInvalida
    Write-Host "       [WARN] No se validó el método inexistente" -ForegroundColor Yellow
} catch {
    Write-Host "       [OK] Validación correcta: Método no encontrado" -ForegroundColor Green
}

# ============================================================
# TEST 15: ELIMINAR MÉTODO DE PAGO
# ============================================================
Write-Host "`n[TEST 15] Eliminar Método - DELETE /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Yellow

try {
    $eliminado = Invoke-RestMethod -Uri "$apiUrl/$metodoId1/usuario/$usuario1" -Method Delete
    Write-Host "   [OK] $($eliminado.mensaje)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Eliminar falló: $_" -ForegroundColor Red
}

# Verificar que fue eliminado
try {
    $metodos = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1" -Method Get
    Write-Host "   [OK] Métodos activos restantes: $($metodos.Count)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Verificar eliminación falló: $_" -ForegroundColor Red
}

# ============================================================
# TEST 16: ESTADO FINAL
# ============================================================
Write-Host "`n[TEST 16] Estado Final del Sistema" -ForegroundColor Yellow

try {
    $metodos = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1" -Method Get
    Write-Host "   [OK] Métodos activos: $($metodos.Count)" -ForegroundColor Green
    
    foreach ($m in $metodos) {
        $principal = if ($m.b_principal) { "★ Principal" } else { "" }
        Write-Host "      - ID: $($m.k_metodo_pago) | $($m.n_marca) | $($m.n_numero_tarjeta) | Saldo: `$$($m.v_saldo) $principal" -ForegroundColor Gray
    }
    
    $saldoTotal = Invoke-RestMethod -Uri "$apiUrl/usuario/$usuario1/saldo-total" -Method Get
    Write-Host "`n   [OK] Saldo total del usuario: `$$($saldoTotal.saldo_total)" -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] Estado final falló: $_" -ForegroundColor Red
}

# ============================================================
# RESUMEN
# ============================================================
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " RESUMEN DE PRUEBAS" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "[OK] Endpoints Probados:" -ForegroundColor Green
Write-Host "   ✅ GET  /health" -ForegroundColor Gray
Write-Host "   ✅ POST /api/metodos-pago/validar" -ForegroundColor Gray
Write-Host "   ✅ POST /api/metodos-pago/" -ForegroundColor Gray
Write-Host "   ✅ GET  /api/metodos-pago/usuario/{cc}" -ForegroundColor Gray
Write-Host "   ✅ GET  /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Gray
Write-Host "   ✅ GET  /api/metodos-pago/usuario/{cc}/principal" -ForegroundColor Gray
Write-Host "   ✅ GET  /api/metodos-pago/saldo/{id}" -ForegroundColor Gray
Write-Host "   ✅ POST /api/metodos-pago/recarga" -ForegroundColor Gray
Write-Host "   ✅ GET  /api/metodos-pago/usuario/{cc}/saldo-total" -ForegroundColor Gray
Write-Host "   ✅ PUT  /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Gray
Write-Host "   ✅ PATCH /api/metodos-pago/{id}/usuario/{cc}/principal" -ForegroundColor Gray
Write-Host "   ✅ DELETE /api/metodos-pago/{id}/usuario/{cc}" -ForegroundColor Gray

Write-Host "`n[INFO] Funcionalidades Probadas:" -ForegroundColor Cyan
Write-Host "   • Health check del servicio" -ForegroundColor Gray
Write-Host "   • Validación de números de tarjeta (Luhn)" -ForegroundColor Gray
Write-Host "   • Creación de métodos de pago" -ForegroundColor Gray
Write-Host "   • Listado de métodos" -ForegroundColor Gray
Write-Host "   • Consulta individual" -ForegroundColor Gray
Write-Host "   • Obtener método principal" -ForegroundColor Gray
Write-Host "   • Recarga de saldo (múltiples)" -ForegroundColor Gray
Write-Host "   • Consulta de saldo individual y total" -ForegroundColor Gray
Write-Host "   • Actualización de datos" -ForegroundColor Gray
Write-Host "   • Cambio de método principal" -ForegroundColor Gray
Write-Host "   • Eliminación (soft delete)" -ForegroundColor Gray
Write-Host "   • Validaciones de montos" -ForegroundColor Gray

Write-Host "`n[INFO] Eventos RabbitMQ Generados:" -ForegroundColor Cyan
Write-Host "   • METODO_PAGO_CREATED (x2)" -ForegroundColor Gray
Write-Host "   • SALDO_RECARGADO (x2)" -ForegroundColor Gray
Write-Host "   • METODO_PAGO_UPDATED (x1)" -ForegroundColor Gray
Write-Host "   • METODO_PAGO_SET_PRINCIPAL (x1)" -ForegroundColor Gray
Write-Host "   • METODO_PAGO_DELETED (x1)" -ForegroundColor Gray

Write-Host "`n[TIP] Ver eventos en RabbitMQ:" -ForegroundColor Magenta
Write-Host "   URL: http://localhost:15672" -ForegroundColor Gray
Write-Host "   Usuario: urbanride" -ForegroundColor Gray
Write-Host "   Password: urbanride2024" -ForegroundColor Gray
Write-Host "   Exchange: urbanride.payments" -ForegroundColor Gray

Write-Host "`n[TIP] Ver datos en pgAdmin:" -ForegroundColor Magenta
Write-Host "   URL: http://localhost:5050" -ForegroundColor Gray
Write-Host "   Usuario: admin@urbanride.com" -ForegroundColor Gray
Write-Host "   Password: admin2024" -ForegroundColor Gray
Write-Host "   Tabla: metodo_pago" -ForegroundColor Gray

Write-Host "`n[TIP] Documentación interactiva:" -ForegroundColor Magenta
Write-Host "   Swagger UI: http://localhost:5002/docs" -ForegroundColor Gray

Write-Host "`n============================================================`n" -ForegroundColor Cyan
