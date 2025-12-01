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
INSERT INTO fine (v_amount, d_descripcion) VALUES
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
INSERT INTO bicycle (k_series, n_model, t_padlock_status, f_last_update, n_latitude, n_length, v_battery) VALUES
(1001, 'Mountain X1', 'LOCK', NOW(), 4.6097, -74.0817, 95.5),
(1002, 'City Cruiser', 'LOCK', NOW(), 4.7110, -74.0721, 80.0),
(1003, 'Electric E1', 'LOCK', NOW(), 6.2092, -75.5712, 45.0),
(1004, 'Mountain X1', 'OPEN', NOW(), 6.2518, -75.5636, 100.0),
(1005, 'City Cruiser', 'LOCK', NOW(), 3.4756, -76.5269, 60.0),
(1006, 'Electric E1', 'LOCK', NOW(), 4.6097, -74.0817, 15.0),
(1007, 'Mountain X1', 'LOCK', NOW(), 4.7110, -74.0721, 88.0),
(1008, 'City Cruiser', 'OPEN', NOW(), 6.2092, -75.5712, 92.0),
(1009, 'Electric E1', 'LOCK', NOW(), 6.2518, -75.5636, 30.0),
(1010, 'Mountain X1', 'LOCK', NOW(), 3.4756, -76.5269, 75.0);

-- 7. Slots
-- Linking slots to stations and some bicycles
INSERT INTO slots (k_id_slot, t_padlock_status, k_id_station, k_id_bicycle) VALUES
('S-001-01', 'LOCK', 1, 1),
('S-001-02', 'LOCK', 1, 6),
('S-001-03', 'OPEN', 1, NULL),
('S-001-04', 'OPEN', 1, NULL),
('S-002-01', 'LOCK', 2, 2),
('S-002-02', 'LOCK', 2, 7),
('S-002-03', 'OPEN', 2, NULL),
('S-003-01', 'LOCK', 3, 3),
('S-003-02', 'OPEN', 3, NULL),
('S-003-03', 'OPEN', 3, NULL),
('S-004-01', 'OPEN', 4, NULL), -- Bike 4 is OPEN (in use?) so not in slot
('S-004-02', 'LOCK', 4, 9),
('S-005-01', 'LOCK', 5, 5),
('S-005-02', 'LOCK', 5, 10),
('S-005-03', 'OPEN', 5, NULL);

-- 8. Panic Button Activations
INSERT INTO panic_button (f_activation_date, k_id_station) VALUES
(NOW() - INTERVAL '5 days', 1),
(NOW() - INTERVAL '2 days', 3),
(NOW() - INTERVAL '10 hours', 4);

-- 9. Travel
-- Completed trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '30 minutes', 'COMPLETED', 1, 1, 2, 'user-001', 'LAST_MILE'),
(NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days' + INTERVAL '45 minutes', 'COMPLETED', 2, 2, 1, 'user-002', 'LONG_HAUL'),
(NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '15 minutes', 'COMPLETED', 3, 3, 4, 'user-004', 'LAST_MILE');

-- Active/In-progress trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '15 minutes', NULL, 'IN_PROGRESS', 4, 4, NULL, 'user-005', 'LAST_MILE'),
(NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '2 minutes', NULL, 'IN_PROGRESS', 8, 3, NULL, 'user-001', 'LONG_HAUL');

-- Cancelled trips
INSERT INTO travel (f_required_at, f_started_at, f_ended_at, t_status, k_id_bicycle, k_from_id_station, k_to_id_station, k_uid_user, t_travel_type) VALUES
(NOW() - INTERVAL '1 day', NULL, NULL, 'CANCELLED', 5, 5, NULL, 'user-003', 'LAST_MILE');

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
	t_maintenance_type,
	v_total_trips,
	t_triggered_by,
	d_description,
	t_status,
	f_date,
	k_id_bicycle,
	v_cost_usd
) VALUES
	('PREVENTIVO', 320, 'KILOMETRAJE', 'Revisión general y ajuste de frenos', 'RESOLVED', CURRENT_DATE - INTERVAL '12 days', '1', 45.00),
	('CORRECTIVO', 185, 'ALERTA_IOT', 'Cambio de pastillas de freno y limpieza de transmisión', 'RESOLVED', CURRENT_DATE - INTERVAL '6 days', '3', 78.50),
	('EMERGENCIA', 510, 'USUARIO', 'Reporte de ruido en la suspensión, pendiente revisión final', 'SOLVING', CURRENT_DATE - INTERVAL '1 day', '6', NULL),
	('PREVENTIVO', 260, 'TIEMPO', 'Lubricación y chequeo de batería', 'RESOLVED', CURRENT_DATE - INTERVAL '3 days', '9', 55.25);
