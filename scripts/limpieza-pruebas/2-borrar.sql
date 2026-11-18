-- Limpieza de las cuentas de prueba: PASO 2, borrado PERMANENTE (no se puede deshacer).
-- Ejecútalo solo después de revisar los números del paso 1. Todo va en una transacción: si algo falla, no se borra nada.
--
-- Orden obligado por las claves foráneas de V1:
--   1. postulaciones a vacantes de empresas de prueba (fk_postulaciones_vacante es RESTRICT)
--   2. vacantes de esas empresas (fk_vacantes_empresa es RESTRICT; sus vacante_habilidades se borran en cascada)
--   3. usuarios de prueba (en cascada: su empresa o candidato, candidato_habilidades y las postulaciones del candidato)

BEGIN;

CREATE TEMP TABLE usuarios_prueba ON COMMIT DROP AS
    SELECT id FROM usuarios WHERE lower(email) LIKE '%@example.com' AND rol <> 'ADMIN';

DELETE FROM postulaciones
WHERE vacante_id IN (
    SELECT v.id FROM vacantes v
    JOIN empresas e ON e.id = v.empresa_id
    WHERE e.usuario_id IN (SELECT id FROM usuarios_prueba)
);

DELETE FROM vacantes
WHERE empresa_id IN (SELECT id FROM empresas WHERE usuario_id IN (SELECT id FROM usuarios_prueba));

DELETE FROM usuarios WHERE id IN (SELECT id FROM usuarios_prueba);

-- Comprobación: debe responder 0 en todas las filas
SELECT 'usuarios @example.com que quedan' AS que, count(*) AS cuantos
FROM usuarios WHERE lower(email) LIKE '%@example.com' AND rol <> 'ADMIN'
UNION ALL SELECT 'empresas sin usuario', count(*) FROM empresas WHERE usuario_id NOT IN (SELECT id FROM usuarios)
UNION ALL SELECT 'candidatos sin usuario', count(*) FROM candidatos WHERE usuario_id NOT IN (SELECT id FROM usuarios);

COMMIT;
