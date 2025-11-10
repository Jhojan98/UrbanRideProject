-- Create per-service users and grant minimal privileges
-- Arquitectura simplificada: solo schema public
-- Create roles/users
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'auth_user') THEN
      CREATE ROLE auth_user LOGIN PASSWORD 'auth_pass';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'bicycle_user') THEN
      CREATE ROLE bicycle_user LOGIN PASSWORD 'bicycle_pass';
   END IF;
END$$;

-- Ensure they can connect to the database
GRANT CONNECT ON DATABASE mydatabase TO auth_user;
GRANT CONNECT ON DATABASE mydatabase TO bicycle_user;

-- Otorgar uso del schema public (necesario para acceder a las tablas)
GRANT USAGE ON SCHEMA public TO auth_user;
GRANT USAGE ON SCHEMA public TO bicycle_user;


-- Permisos para bicycle_user sobre tabla bicicleta
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bicycle_user') THEN
      -- Otorgar uso del schema public
      GRANT USAGE ON SCHEMA public TO bicycle_user;
      
      -- Permisos sobre la tabla bicicleta
      GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.bicicleta TO bicycle_user;
      
      -- Permisos sobre las secuencias de bicicleta
      GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.bicicleta_k_id_bicicleta_seq TO bicycle_user;
      GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.bicicleta_k_serie_seq TO bicycle_user;
      
      RAISE NOTICE 'Permisos otorgados a bicycle_user sobre public.bicicleta';
   END IF;
END$$;

-- Permisos para auth_user sobre tabla usuario
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'auth_user') THEN
      -- Otorgar uso del schema public
      GRANT USAGE ON SCHEMA public TO auth_user;
      
      -- Permisos sobre la tabla usuario
      GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.usuario TO auth_user;
      
      -- Permisos sobre la secuencia de usuario
      GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.usuario_k_cedula_ciudadania_usuario_seq TO auth_user;
      
      RAISE NOTICE 'Permisos otorgados a auth_user sobre public.usuario';
   END IF;
END$$;

-- Grants RBAC movidos a 04-rbac-grants.sql