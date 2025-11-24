ROLLBACK;

BEGIN;

-- Usuarios de prueba
INSERT INTO public.users (
    k_user_cc,
    k_uid_user,
    n_user_first_name,
    n_user_second_name,
    n_user_first_lastname,
    n_user_second_lastname,
    f_user_birthdate,
    t_subscription_type,
    f_user_registration_date,
    t_is_verified,
    v_balance
) VALUES
    (101001, 'USR-101001', 'Ana', 'Lucía', 'Martínez', 'López',
     DATE '1994-03-15', 'NONE', CURRENT_DATE - INTERVAL '180 day', TRUE, 150.00),
    (101002, 'USR-101002', 'Carlos', NULL, 'Gómez', 'Ruiz',
     DATE '1988-07-22', 'NONE', CURRENT_DATE - INTERVAL '30 day', FALSE, 20.00),
    (101003, 'USR-101003', 'Sofía', 'Elena', 'Pineda', NULL,
     DATE '1999-12-01', 'NONE', CURRENT_DATE - INTERVAL '5 day', TRUE, 0.00)
ON CONFLICT (k_user_cc) DO NOTHING;

-- Catálogo de multas base (guardamos en una tabla temporal para obtener los IDs)
WITH inserted_fines AS (
    INSERT INTO public.fine (n_name, d_description, v_amount) VALUES
        ('Uso inadecuado de bicicleta', 'Multa por uso inadecuado de bicicleta compartida', 25000.00),
        ('Daños a infraestructura', 'Multa por daños al candado de la estación', 45000.00),
        ('Estacionamiento irregular', 'Multa por estacionamiento fuera de zona permitida', 12000.50)
    RETURNING k_id_fine, n_name
)
-- Asignaciones de multas a usuarios usando los IDs reales
INSERT INTO public.user_fine (
    n_reason,
    t_state,
    k_id_multa,
    k_user_cc,
    v_amount_snapshot,
    f_assigned_at,
    f_updated_at
)
SELECT 
    'Uso inadecuado de bicicleta compartida', 
    'PENDING', 
    k_id_fine, 
    101001, 
    25000.00, 
    NOW(), 
    NOW()
FROM inserted_fines WHERE n_name = 'Uso inadecuado de bicicleta'
UNION ALL
SELECT 
    'Daños al candado de la estación', 
    'PAID', 
    k_id_fine, 
    101002, 
    45000.00, 
    NOW() - INTERVAL '2 day', 
    NOW() - INTERVAL '1 day'
FROM inserted_fines WHERE n_name = 'Daños a infraestructura'
UNION ALL
SELECT 
    'Estacionamiento fuera de zona permitida', 
    'CANCELLED', 
    k_id_fine, 
    101001, 
    12000.50, 
    NOW() - INTERVAL '7 day', 
    NOW() - INTERVAL '6 day'
FROM inserted_fines WHERE n_name = 'Estacionamiento irregular'
UNION ALL
SELECT 
    'Bloqueo prolongado de bicicleta', 
    'PENDING', 
    k_id_fine, 
    101003, 
    45000.00, 
    NOW(), 
    NOW()
FROM inserted_fines WHERE n_name = 'Daños a infraestructura';

COMMIT;