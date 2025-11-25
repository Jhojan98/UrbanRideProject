ROLLBACK;

BEGIN;

-- Usuarios de prueba
INSERT INTO public.users (
    k_uid_user,
    n_user_name,
    t_subscription_type,
    v_balance
) VALUES
    ('USR-101001', 'Ana','NONE', 150.00),
    ('USR-101002', 'Carlos','NONE', 20.00),
    ('USR-101003', 'Sofía','NONE', 0.00)
ON CONFLICT (k_uid_user) DO NOTHING;

-- Catálogo de multas base (guardamos en una tabla temporal para obtener los IDs)
WITH inserted_fines AS (
    INSERT INTO public.fine (k_id_fine, d_descripcion, v_amount) VALUES
        (001, 'Uso inadecuado de bicicleta compartida', 25000.00),
        (002, 'Daños a infraestructura', 45000.00),
        (003, 'Estacionamiento irregular', 12000.50)
    RETURNING k_id_fine, d_descripcion
)
-- Asignaciones de multas a usuarios usando los IDs reales
INSERT INTO public.user_fine (
    n_reason,
    t_state,
    k_id_fine,
    k_uid_user,
    v_amount_snapshot,
    f_assigned_at,
    f_update_at
)
SELECT 
    'Uso inadecuado de bicicleta compartida', 
    'PENDING', 
    k_id_fine, 
    'USR-101001', 
    25000.00, 
    NOW(), 
    NOW()
FROM inserted_fines WHERE d_descripcion = 'Uso inadecuado de bicicleta compartida'
UNION ALL
SELECT 
    'Daños al candado de la estación', 
    'PAID', 
    k_id_fine, 
    'USR-101002', 
    45000.00, 
    NOW() - INTERVAL '2 day', 
    NOW() - INTERVAL '1 day'
FROM inserted_fines WHERE d_descripcion = 'Daños a infraestructura'
UNION ALL
SELECT 
    'Estacionamiento fuera de zona permitida', 
    'PAID', 
    k_id_fine, 
    'USR-101001', 
    12000.50, 
    NOW() - INTERVAL '7 day', 
    NOW() - INTERVAL '6 day'
FROM inserted_fines WHERE d_descripcion = 'Estacionamiento irregular'
UNION ALL
SELECT 
    'Bloqueo prolongado de bicicleta', 
    'PENDING', 
    k_id_fine, 
    'USR-101003', 
    45000.00, 
    NOW(), 
    NOW()
FROM inserted_fines WHERE d_descripcion = 'Daños a infraestructura';

COMMIT;