-- ============================================================================
-- Script de Verificación - Integración Payment Service
-- Ejecutar después de aplicar 03-upgrade-payment-method-table.sql y roles.sql
-- ============================================================================

\echo '====================================================='
\echo ' VERIFICACIÓN DE INTEGRACIÓN - PAYMENT SERVICE'
\echo '====================================================='
\echo ''

-- 1. Verificar que la tabla existe
\echo '1. Verificando existencia de tabla payment_method...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' AND tablename = 'payment_method'
        ) THEN '✓ Tabla payment_method existe en public schema'
        ELSE '✗ ERROR: Tabla payment_method NO encontrada'
    END AS resultado;
\echo ''

-- 2. Verificar número de columnas
\echo '2. Verificando estructura de columnas...'
SELECT 
    COUNT(*) as total_columnas,
    CASE 
        WHEN COUNT(*) = 14 THEN '✓ Todas las columnas presentes (14)'
        ELSE '✗ ERROR: Faltan columnas (esperadas: 14, encontradas: ' || COUNT(*) || ')'
    END AS resultado
FROM information_schema.columns 
WHERE table_name = 'payment_method' AND table_schema = 'public';
\echo ''

-- 3. Listar columnas
\echo '3. Columnas de la tabla:'
SELECT 
    column_name, 
    data_type,
    CASE WHEN is_nullable = 'NO' THEN 'NOT NULL' ELSE 'NULL' END as nullable
FROM information_schema.columns 
WHERE table_name = 'payment_method' AND table_schema = 'public'
ORDER BY ordinal_position;
\echo ''

-- 4. Verificar primary key
\echo '4. Verificando primary key...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_constraint 
            WHERE conrelid = 'public.payment_method'::regclass 
              AND contype = 'p'
        ) THEN '✓ Primary key definida: k_id_payment_method'
        ELSE '✗ ERROR: Primary key NO encontrada'
    END AS resultado;
\echo ''

-- 5. Verificar foreign key a users
\echo '5. Verificando foreign key a tabla users...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_constraint 
            WHERE conrelid = 'public.payment_method'::regclass 
              AND contype = 'f'
              AND confrelid = 'public.users'::regclass
        ) THEN '✓ Foreign key a users configurada'
        ELSE '✗ ERROR: Foreign key a users NO encontrada'
    END AS resultado;
\echo ''

-- 6. Verificar triggers
\echo '6. Verificando trigger de método principal único...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_trigger 
            WHERE tgrelid = 'public.payment_method'::regclass
              AND tgname = 'trigger_single_primary_payment'
        ) THEN '✓ Trigger trigger_single_primary_payment configurado'
        ELSE '✗ ERROR: Trigger NO encontrado'
    END AS resultado;
\echo ''

-- 7. Verificar función del trigger
\echo '7. Verificando función ensure_single_primary_payment...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_proc 
            WHERE proname = 'ensure_single_primary_payment'
        ) THEN '✓ Función ensure_single_primary_payment existe'
        ELSE '✗ ERROR: Función NO encontrada'
    END AS resultado;
\echo ''

-- 8. Verificar índices
\echo '8. Verificando índices...'
SELECT 
    COUNT(*) as total_indices,
    CASE 
        WHEN COUNT(*) >= 4 THEN '✓ Índices creados correctamente'
        ELSE '✗ ADVERTENCIA: Faltan índices (esperados: ≥4)'
    END AS resultado
FROM pg_indexes 
WHERE tablename = 'payment_method' AND schemaname = 'public';
\echo ''

-- 9. Listar índices
\echo '9. Índices existentes:'
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'payment_method' AND schemaname = 'public'
ORDER BY indexname;
\echo ''

-- 10. Verificar rol manager_pay_method
\echo '10. Verificando rol manager_pay_method...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_roles WHERE rolname = 'manager_pay_method'
        ) THEN '✓ Rol manager_pay_method existe'
        ELSE '✗ ERROR: Rol manager_pay_method NO encontrado'
    END AS resultado;
\echo ''

-- 11. Verificar permisos de tabla
\echo '11. Verificando permisos de tabla para manager_pay_method...'
SELECT 
    grantee,
    string_agg(privilege_type, ', ' ORDER BY privilege_type) as privileges
FROM information_schema.role_table_grants
WHERE table_name = 'payment_method' 
  AND table_schema = 'public'
  AND grantee = 'manager_pay_method'
GROUP BY grantee;
\echo ''

-- 12. Verificar secuencia
\echo '12. Verificando secuencia de ID...'
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM pg_class 
            WHERE relname = 'payment_method_k_id_payment_method_seq'
              AND relkind = 'S'
        ) THEN '✓ Secuencia payment_method_k_id_payment_method_seq existe'
        ELSE '✗ ERROR: Secuencia NO encontrada'
    END AS resultado;
\echo ''

-- 13. Verificar permisos de secuencia
\echo '13. Verificando permisos de secuencia...'
SELECT 
    CASE 
        WHEN has_sequence_privilege('manager_pay_method', 'payment_method_k_id_payment_method_seq', 'USAGE') 
        THEN '✓ manager_pay_method tiene permisos en secuencia'
        ELSE '✗ ERROR: Faltan permisos de secuencia'
    END AS resultado;
\echo ''

-- 14. Verificar constraints CHECK
\echo '14. Verificando constraints CHECK...'
SELECT 
    conname as constraint_name,
    pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conrelid = 'public.payment_method'::regclass
  AND contype = 'c'
ORDER BY conname;
\echo ''

-- 15. Test de inserción (opcional - comentar si no quieres insertar)
\echo '15. Test de inserción (SKIP - descomenta para probar)...'
-- Descomentar las siguientes líneas para probar inserción:
/*
BEGIN;
INSERT INTO public.payment_method 
    (k_user_cc, t_card_type, n_card_number, n_owner_name, f_expiration_date, n_brand, b_is_primary, v_balance)
VALUES 
    (1, 'CREDITO', '**** **** **** 1234', 'Juan Test', '2025-12-31', 'VISA', TRUE, 0);
SELECT '✓ Inserción de prueba exitosa' AS resultado;
ROLLBACK;
\echo 'Inserción de prueba revertida (ROLLBACK)'
*/
\echo ''

-- Resumen final
\echo '====================================================='
\echo ' RESUMEN DE VERIFICACIÓN'
\echo '====================================================='
\echo ''
\echo 'Si todos los checks anteriores muestran ✓, la integración'
\echo 'está completa y el microservicio puede conectarse.'
\echo ''
\echo 'Siguiente paso:'
\echo '  1. Configurar .env del microservicio'
\echo '  2. Iniciar: uvicorn app.main:app --port 5003 --reload'
\echo '  3. Abrir: http://localhost:5003/docs'
\echo ''
\echo '====================================================='
