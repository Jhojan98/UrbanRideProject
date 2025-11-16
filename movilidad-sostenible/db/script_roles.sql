-- Script de creación de roles y privilegios para esquema compartido
-- Ejecutar una vez conectado como superusuario (ej: postgres)

-- 1. Crear rol migrador (dueño de objetos)
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='flyway') THEN
        CREATE ROLE flyway LOGIN PASSWORD 'FLYWAY_PASS' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
END $$;

-- 2. Roles por microservicio
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_users') THEN
        CREATE ROLE manager_users LOGIN PASSWORD 'g_user' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_bicycle') THEN
        CREATE ROLE manager_bicycle LOGIN PASSWORD 'g_bicy' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_travel') THEN
        CREATE ROLE manager_travel LOGIN PASSWORD 'g_trav' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_station') THEN
        CREATE ROLE manager_station LOGIN PASSWORD 'g_stat' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_city') THEN
        CREATE ROLE manager_city LOGIN PASSWORD 'g_city' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_email') THEN
        CREATE ROLE manager_email LOGIN PASSWORD 'g_email' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_payment') THEN
        CREATE ROLE manager_payment LOGIN PASSWORD 'g_pay' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='manager_fine') THEN
        CREATE ROLE manager_fine LOGIN PASSWORD 'g_fine' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    END IF;
END $$;

-- 3. Revocar privilegios públicos
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- 4. Otorgar USAGE de schema
GRANT USAGE ON SCHEMA public TO manager_users, manager_bicycle, manager_travel, manager_station, manager_city, manager_email, manager_payment, manager_fine;

-- 5. (POST migración V1) Privilegios sobre tablas
DO $$ DECLARE
    _exists boolean;
BEGIN
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='city') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON city TO manager_city; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='station') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON station TO manager_station; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='bicycle') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON bicycle TO manager_bicycle; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='users') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON users TO manager_users; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='email_verification') INTO _exists; IF _exists THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON email_verification TO manager_users;
        GRANT SELECT, INSERT, UPDATE, DELETE ON email_verification TO manager_email;
    END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='travel') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON travel TO manager_travel; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='fine') INTO _exists; IF _exists THEN GRANT SELECT ON fine TO manager_fine; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='user_fine') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON user_fine TO manager_users; END IF;
    SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='payment_method') INTO _exists; IF _exists THEN GRANT SELECT, INSERT, UPDATE, DELETE ON payment_method TO manager_payment; END IF;
END $$;

-- 6. Privilegios sobre secuencias creadas (serial / identity)
DO $$ DECLARE r RECORD; BEGIN
    FOR r IN SELECT sequence_schema, sequence_name FROM information_schema.sequences WHERE sequence_schema='public' LOOP
        EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE %I.%I TO manager_users, manager_bicycle, manager_travel, manager_station, manager_city, manager_email, manager_payment, manager_fine', r.sequence_schema, r.sequence_name);
    END LOOP;
END $$;

-- 7. Establecer default privileges para futuras migraciones (ejecutar tras crear tablas con flyway)
ALTER DEFAULT PRIVILEGES FOR USER flyway IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO manager_users, manager_bicycle, manager_travel, manager_station, manager_city, manager_email, manager_payment, manager_fine;
ALTER DEFAULT PRIVILEGES FOR USER flyway IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO manager_users, manager_bicycle, manager_travel, manager_station, manager_city, manager_email, manager_payment, manager_fine;

-- 8. Otorgar permisos de conexión y uso/creación de esquema al rol flyway
GRANT CONNECT ON DATABASE movilidad_sostenible TO flyway;
GRANT USAGE, CREATE ON SCHEMA public TO flyway;

-- Fin script
