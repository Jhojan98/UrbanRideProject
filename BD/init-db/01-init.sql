-- Crear schemas para los microservicios
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS bicycle;

-- Otorgar permisos al usuario postgres
GRANT ALL PRIVILEGES ON SCHEMA auth TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA bicycle TO postgres;

-- (Nuevo) Otorgar permisos mínimos del schema bicycle al rol de servicio
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bicycle_user') THEN
      GRANT USAGE, CREATE ON SCHEMA bicycle TO bicycle_user;
      ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO bicycle_user;
      ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO bicycle_user;
   END IF;
END$$;

-- (Nuevo) Otorgar permisos mínimos del schema auth al rol de servicio
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'auth_user') THEN
      GRANT USAGE, CREATE ON SCHEMA auth TO auth_user;
      ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO auth_user;
      ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO auth_user;
   END IF;
END$$;