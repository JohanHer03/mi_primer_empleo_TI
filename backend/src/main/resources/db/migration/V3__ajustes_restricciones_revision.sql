-- Ajustes de la revisión del PR #1. V1 ya está aplicada, por eso se corrige con una migración nueva.

-- 1. Email único sin distinguir mayúsculas: Ana@Correo.com y ana@correo.com son la misma cuenta.
--    El adaptador de persistencia debe buscar con lower(email) (por ejemplo, findByEmailIgnoreCase).
ALTER TABLE usuarios DROP CONSTRAINT uq_usuarios_email;
CREATE UNIQUE INDEX uq_usuarios_email ON usuarios (lower(email));

-- 2. NIT normalizado a solo dígitos (con dígito de verificación, sin puntos ni guion),
--    para que la restricción UNIQUE detecte el mismo NIT escrito de formas distintas.
ALTER TABLE empresas
    ADD CONSTRAINT ck_empresas_nit_digitos CHECK (nit ~ '^[0-9]{6,15}$');

-- 3. Una vacante no puede cerrar antes del día en que se publica (hora de Colombia).
ALTER TABLE vacantes
    ADD CONSTRAINT ck_vacantes_fecha_cierre
        CHECK (fecha_cierre >= (fecha_publicacion AT TIME ZONE 'America/Bogota')::date);

-- 4. El semestre solo aplica a estudiantes: obligatorio para ESTUDIANTE, vacío para EGRESADO.
ALTER TABLE candidatos
    ADD CONSTRAINT ck_candidatos_semestre_estado CHECK (
        (estado_academico = 'ESTUDIANTE' AND semestre IS NOT NULL)
        OR (estado_academico = 'EGRESADO' AND semestre IS NULL)
    );
