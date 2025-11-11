-- Create per-service users and grant minimal privileges
-- Idempotent: uses DO blocks to avoid errors if rerun
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

-- Auth schema privileges
GRANT USAGE, CREATE ON SCHEMA auth TO auth_user;
-- Default privileges so future tables/sequences are accessible
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO auth_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO auth_user;

-- Bicycle schema privileges
GRANT USAGE, CREATE ON SCHEMA bicycle TO bicycle_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO bicycle_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO bicycle_user;
