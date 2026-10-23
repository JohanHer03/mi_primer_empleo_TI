-- Supabase otorga a los roles anon y authenticated acceso a las tablas de public a través de su Data API.
-- Este proyecto solo accede por JDBC desde el backend (CLAUDE.md), así que se les retiran los permisos.
-- No usa RLS: la autorización sigue siendo de Spring Security.
-- El bloque solo actúa si los roles existen, para que la migración también corra en un PostgreSQL local.

DO $$
DECLARE
    rol TEXT;
BEGIN
    FOREACH rol IN ARRAY ARRAY['anon', 'authenticated'] LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = rol) THEN
            EXECUTE format('REVOKE ALL ON ALL TABLES IN SCHEMA public FROM %I', rol);
            EXECUTE format('REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM %I', rol);
            -- También para las tablas que creen migraciones futuras
            EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM %I', rol);
            EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM %I', rol);
        END IF;
    END LOOP;
END $$;
