TRUNCATE TABLE city RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE admins RESTART IDENTITY CASCADE;
TRUNCATE TABLE fine RESTART IDENTITY CASCADE;
TRUNCATE TABLE station RESTART IDENTITY CASCADE;
TRUNCATE TABLE bicycle RESTART IDENTITY CASCADE;
TRUNCATE TABLE slots RESTART IDENTITY CASCADE;
TRUNCATE TABLE panic_button RESTART IDENTITY CASCADE;
TRUNCATE TABLE travel RESTART IDENTITY CASCADE;
TRUNCATE TABLE complaints_and_claims RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_fine RESTART IDENTITY CASCADE;
TRUNCATE TABLE maintenance RESTART IDENTITY CASCADE;

-- 1. Cities
INSERT INTO city (n_city_name) VALUES 
('Bogotá'),
('Medellín'),
('Cali');

-- 2. Users (k_uid_user is a string from auth server, using placeholders)
INSERT INTO users (k_uid_user, n_user_name, t_subscription_type, v_balance) VALUES
('user-001', 'Juan Perez', 'MONTHLY', 50000),
('user-002', 'Maria Rodriguez', 'NONE', 15000),
('user-003', 'Carlos Gomez', 'NONE', 0),
('user-004', 'Ana Martinez', 'MONTHLY', 100000),
('user-005', 'Luisa Herrera', 'NONE', 25000);

-- 3. Admins
INSERT INTO admins (k_uid_admin, n_admin_name) VALUES
('admin-001', 'Super Admin'),
('admin-002', 'Soporte Tecnico');

-- 4. Fines (Types)
INSERT INTO fine (v_amount, d_description) VALUES
(50000, 'Daño a la bicicleta'),
(20000, 'Devolución tardía'),
(100000, 'Pérdida de bicicleta'),
(15000, 'Estacionamiento incorrecto');

-- 5. Stations
-- Assuming city IDs: 1=Bogotá, 2=Medellín, 3=Cali
INSERT INTO station (n_station_name, n_latitude, n_length, k_id_city, t_type, t_cctv_status) VALUES
('Estación Central', 4.6097, -74.0817, 1, 'METRO', true),
('Estación Norte', 4.7110, -74.0721, 1, 'RESIDENTIAL', true),
('Estación Poblado', 6.2092, -75.5712, 2, 'FINANCIAL CENTER', true),
('Estación Estadio', 6.2518, -75.5636, 2, 'METRO', false),
('Estación Chipichape', 3.4756, -76.5269, 3, 'FINANCIAL CENTER', true);

-- 6. Bicycles
INSERT INTO bicycle (k_id_bicycle, k_series, n_model, t_padlock_status, f_last_update, n_latitude, n_length, v_battery) VALUES
('MECH-1001', 1001, 'MECHANIC', 'LOCKED', NOW(), 4.6097, -74.0817, 95.5),
('ELEC-1002', 1002, 'ELECTRIC', 'LOCKED', NOW(), 4.7110, -74.0721, 80.0),
('MECH-1003', 1003, 'MECHANIC', 'LOCKED', NOW(), 6.2092, -75.5712, 45.0),
('MECH-1004', 1004, 'MECHANIC', 'UNLOCKED', NOW(), 6.2518, -75.5636, 100.0),
('MECH-1005', 1005, 'MECHANIC', 'LOCKED', NOW(), 3.4756, -76.5269, 60.0),
('ELEC-1006', 1006, 'ELECTRIC', 'LOCKED', NOW(), 4.6097, -74.0817, 15.0),
('MECH-1007', 1007, 'MECHANIC', 'LOCKED', NOW(), 4.7110, -74.0721, 88.0),
('MECH-1008', 1008, 'MECHANIC', 'UNLOCKED', NOW(), 6.2092, -75.5712, 92.0),
('ELEC-1009', 1009, 'ELECTRIC', 'MAINTENANCE', NOW(), 6.2518, -75.5636, 30.0),
('MECH-1010', 1010, 'MECHANIC', 'ERROR', NOW(), 3.4756, -76.5269, 75.0);

-- 7. Slots
-- Linking slots to stations and some bicycles
INSERT INTO slots (k_id_slot, t_padlock_status, k_id_station, k_id_bicycle) VALUES
('S-001-01', 'LOCKED', 1, 'MECH-1001'),
('S-001-02', 'LOCKED', 1, 'ELEC-1006'),
('S-001-03', 'UNLOCKED', 1, NULL),
('S-001-04', 'UNLOCKED', 1, NULL),
('S-002-01', 'LOCKED', 2, 'ELEC-1002'),
('S-002-02', 'LOCKED', 2, 'MECH-1007'),
('S-002-03', 'UNLOCKED', 2, NULL),
('S-003-01', 'LOCKED', 3, 'MECH-1003'),
('S-003-02', 'UNLOCKED', 3, NULL),
('S-003-03', 'UNLOCKED', 3, NULL),
('S-004-01', 'UNLOCKED', 4, NULL), -- Bike 4 is UNLOCKED (in use?) so not in slot
('S-004-02', 'LOCKED', 4, 'ELEC-1009'),
('S-005-01', 'LOCKED', 5, 'MECH-1005'),
('S-005-02', 'LOCKED', 5, 'MECH-1010'),
('S-005-03', 'UNLOCKED', 5, NULL);
-- 8. Panic Button Activations
INSERT INTO panic_button (f_activation_date, k_id_station) VALUES
(NOW() - INTERVAL '5 days', 1),
(NOW() - INTERVAL '2 days', 3),
(NOW() - INTERVAL '10 hours', 4);

-- 9. Travel
-- Completed trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '30 minutes', 'COMPLETED', 'MECH-1001', 1, 2, 'user-001', 'LAST_MILE'),
(NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days' + INTERVAL '45 minutes', 'COMPLETED', 'ELEC-1002', 2, 1, 'user-002', 'LONG_HAUL'),
(NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '15 minutes', 'COMPLETED', 'MECH-1003', 3, 4, 'user-004', 'LAST_MILE');

-- Active/In-progress trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '15 minutes', NULL, 'IN_PROGRESS', 'MECH-1004', 4, NULL, 'user-005', 'LAST_MILE'),
(NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '2 minutes', NULL, 'IN_PROGRESS', 'MECH-1008', 3, NULL, 'user-001', 'LONG_HAUL');

-- Cancelled trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '1 day', NULL, NULL, 'IN_PROGRESS', 'MECH-1005', 5, NULL, 'user-003', 'LAST_MILE'); -- podria estar cancelado
-- 10. Complaints and Claims
INSERT INTO complaints_and_claims (d_description, t_status, k_id_travel) VALUES
('La bicicleta tenía la cadena suelta', 'PENDING', 1),
('Cobro incorrecto en el viaje', 'RESOLVED', 2);

-- 11. User Fines
INSERT INTO user_fine (n_reason, t_state, k_id_fine, k_uid_user, v_amount_snapshot, f_assigned_at, f_update_at) VALUES
('Devolución tardía de bicicleta', 'PENDING', 2, 'user-002', 20000, NOW() - INTERVAL '8 days', NULL),
('Daño en el manubrio', 'PAID', 1, 'user-003', 50000, NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days');

-- 12. Maintenance Records
INSERT INTO maintenance (
	t_entity_tipe,
	t_maintenace_type,
	t_triggered_by,
	d_description,
	t_status,
	f_date,
	v_cost,
	k_id_bicycle,
	k_id_station,
	k_id_lock
) VALUES
	('BICYCLE', 'PREVENTIVE', 'ADMIN', 'Revisión general y ajuste de frenos', 'PENDING', CURRENT_DATE, 45, 'MECH-1001', NULL, NULL),
	('BICYCLE', 'CORRECTIVE', 'IOT_ALERT', 'Cambio de pastillas de freno y limpieza de transmisión', 'RESOLVED', CURRENT_DATE, 79, 'MECH-1003', NULL, NULL),
	('BICYCLE', 'INSPECTION', 'USER', 'Reporte de ruido en la suspensión, pendiente revisión final', 'PENDING', CURRENT_DATE, NULL, 'ELEC-1006', NULL, NULL),
	('BICYCLE', 'PREVENTIVE', 'ADMIN', 'Lubricación y chequeo de batería', 'SOLVING', CURRENT_DATE, 55, 'ELEC-1009', NULL, NULL),
	('STATION', 'CORRECTIVE', 'ADMIN', 'Reemplazo de señalización y revisión de CCTV', 'PENDING', CURRENT_DATE, 120, NULL, 3, NULL),
	('LOCK', 'INSPECTION', 'USER', 'Verificación manual del candado por reporte de usuario', 'PENDING', CURRENT_DATE, 15, NULL, NULL, 'S-005-02');