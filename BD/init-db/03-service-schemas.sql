-- Create schemas for microservices if they need them
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS bicycle;

-- Grant permissions to service roles
GRANT USAGE, CREATE ON SCHEMA auth TO manager_users;
GRANT USAGE, CREATE ON SCHEMA bicycle TO manager_bicycle;

-- Default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO manager_users;
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO manager_users;

ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO manager_bicycle;
ALTER DEFAULT PRIVILEGES IN SCHEMA bicycle GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO manager_bicycle;

SELECT 'Service schemas created successfully' AS status;
