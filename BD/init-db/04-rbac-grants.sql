/* ---------------------------------------------------- */
/*  Grants para tablas RBAC                            */
/*  Ejecutar después de 03-roles-and-permissions.sql   */
/* ---------------------------------------------------- */

DO $$
DECLARE seq_name text;
BEGIN
   IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'auth_user') THEN
      -- Lectura de roles y permisos
      GRANT SELECT ON TABLE public.rol TO auth_user;
      GRANT SELECT ON TABLE public.permiso TO auth_user;
      GRANT SELECT ON TABLE public.rol_permiso TO auth_user;
      -- Gestión básica de asignaciones usuario_rol
      GRANT SELECT, INSERT, DELETE ON TABLE public.usuario_rol TO auth_user;

      -- Secuencias (opcional, solo si se crean nuevos roles/permisos vía app)
      FOR seq_name IN SELECT sequence_name FROM information_schema.sequences 
                      WHERE sequence_schema='public' 
                        AND sequence_name IN ('rol_k_id_rol_seq','permiso_k_id_permiso_seq') LOOP
         EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE public.%I TO auth_user', seq_name);
      END LOOP;

      RAISE NOTICE 'Grants RBAC otorgados a auth_user';
   END IF;
END$$;
