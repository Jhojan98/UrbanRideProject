/* ---------------------------------------------------- */
/*  Crear usuario admin y asignar rol ADMIN             */
/*  Ejecutar después de 01, 02 y 03                     */
/* ---------------------------------------------------- */

-- Variables (ajusta el correo y la contraseña si deseas)
-- Por defecto: admin@example.com / contraseña: 123

-- Asegurar que existe el rol ADMIN
INSERT INTO rol (n_nombre) VALUES ('ADMIN') ON CONFLICT (n_nombre) DO NOTHING;

-- Crear usuario admin si no existe
DO $$
DECLARE v_user_id int;
BEGIN
  SELECT k_cedula_ciudadania_usuario INTO v_user_id
  FROM usuario
  WHERE n_correo_electronico = 'admin@example.com';

  IF v_user_id IS NULL THEN
    INSERT INTO usuario (
      n_usuario,
      n_contrasena,
      hashed_password,
      is_verified,
      otp,
      n_primer_nombre,
      n_segundo_nombre,
      n_primer_apellido,
      n_segundo_apellido,
      f_fecha_nacimiento,
      n_correo_electronico,
      t_tipo_suscripcion,
      f_fecha_de_registro
    ) VALUES (
      'admin',
      '__legacy__',
      '$$2b$12$5daR1jsnXCh5dAkQkteLZeux1Jz0cssDLSHyF.yU7lCWjPfEa5HBi',
      true,
      NULL,
      'Admin',
      NULL,
      'Root',
      NULL,
      '1990-01-01',
      'admin@example.com',
      'NINGUNA',
      to_char(now(), 'YYYY-MM-DD')
    ) RETURNING k_cedula_ciudadania_usuario INTO v_user_id;
  END IF;

  -- Asignar rol ADMIN
  PERFORM 1 FROM usuario_rol ur
   WHERE ur.k_cedula_ciudadania_usuario = v_user_id
     AND ur.k_id_rol = (SELECT k_id_rol FROM rol WHERE n_nombre='ADMIN');

  IF NOT FOUND THEN
    INSERT INTO usuario_rol (k_cedula_ciudadania_usuario, k_id_rol)
    VALUES (
      v_user_id,
      (SELECT k_id_rol FROM rol WHERE n_nombre='ADMIN')
    )
    ON CONFLICT DO NOTHING;
  END IF;
END$$;
