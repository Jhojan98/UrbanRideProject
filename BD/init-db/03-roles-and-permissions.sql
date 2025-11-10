/* ---------------------------------------------------- */
/*  Roles y Permisos (RBAC)                             */
/*  Crear tablas de rol, permiso, asignaciones y grants */
/* ---------------------------------------------------- */

-- Tablas RBAC
CREATE TABLE IF NOT EXISTS rol (
    k_id_rol serial PRIMARY KEY,
    n_nombre varchar(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS permiso (
    k_id_permiso serial PRIMARY KEY,
    n_nombre varchar(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS rol_permiso (
    k_id_rol int NOT NULL REFERENCES rol(k_id_rol) ON DELETE CASCADE,
    k_id_permiso int NOT NULL REFERENCES permiso(k_id_permiso) ON DELETE CASCADE,
    PRIMARY KEY (k_id_rol, k_id_permiso)
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    k_cedula_ciudadania_usuario int NOT NULL REFERENCES usuario(k_cedula_ciudadania_usuario) ON DELETE CASCADE,
    k_id_rol int NOT NULL REFERENCES rol(k_id_rol) ON DELETE CASCADE,
    PRIMARY KEY (k_cedula_ciudadania_usuario, k_id_rol)
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS IX_usuario_rol_usuario ON usuario_rol(k_cedula_ciudadania_usuario);
CREATE INDEX IF NOT EXISTS IX_usuario_rol_rol ON usuario_rol(k_id_rol);

-- Datos semilla básicos
INSERT INTO rol (n_nombre) VALUES ('USER') ON CONFLICT (n_nombre) DO NOTHING;
INSERT INTO rol (n_nombre) VALUES ('ADMIN') ON CONFLICT (n_nombre) DO NOTHING;

-- Permisos ejemplo (ajusta según tus endpoints/acciones)
INSERT INTO permiso (n_nombre) VALUES
('USER_READ'),
('USER_WRITE'),
('BIKE_READ'),
('BIKE_WRITE')
ON CONFLICT (n_nombre) DO NOTHING;

-- Mapear permisos a ADMIN
DO $$
DECLARE admin_id int; perm_id int;
BEGIN
  SELECT k_id_rol INTO admin_id FROM rol WHERE n_nombre='ADMIN';
  IF admin_id IS NOT NULL THEN
    FOR perm_id IN SELECT k_id_permiso FROM permiso LOOP
      INSERT INTO rol_permiso (k_id_rol, k_id_permiso)
      VALUES (admin_id, perm_id)
      ON CONFLICT DO NOTHING;
    END LOOP;
  END IF;
END$$;

-- Asignar USER por defecto a usuarios existentes (si procede)
DO $$
DECLARE user_role_id int;
BEGIN
  SELECT k_id_rol INTO user_role_id FROM rol WHERE n_nombre='USER';
  IF user_role_id IS NOT NULL THEN
    INSERT INTO usuario_rol (k_cedula_ciudadania_usuario, k_id_rol)
    SELECT u.k_cedula_ciudadania_usuario, user_role_id
    FROM usuario u
    ON CONFLICT DO NOTHING;
  END IF;
END$$;

/* Permisos para roles/usuarios de servicio (mínimos) */
-- Nota: Los grants por tabla para auth_user/bicycle_user se mantienen en 02 y/o en scripts de permisos separados.
