-- Limpieza de las cuentas de prueba: PASO 1, solo lectura.
-- Cuenta lo que borraría el paso 2. Ejecútalo en el editor SQL de Supabase y revisa los números antes de seguir.
-- Cuentas de prueba: correo terminado en @example.com (Postman y pruebas de Claude Code), nunca el administrador.
-- Las cuentas de demostración (@demo.example, @andinacodigo.example, ...) terminan en .example y NO se tocan.

WITH prueba AS (
    SELECT id FROM usuarios WHERE lower(email) LIKE '%@example.com' AND rol <> 'ADMIN'
),
empresas_prueba AS (
    SELECT e.id FROM empresas e JOIN prueba p ON p.id = e.usuario_id
),
candidatos_prueba AS (
    SELECT c.id FROM candidatos c JOIN prueba p ON p.id = c.usuario_id
),
vacantes_prueba AS (
    SELECT v.id FROM vacantes v JOIN empresas_prueba e ON e.id = v.empresa_id
)
SELECT 'usuarios de prueba' AS que, count(*) AS cuantos FROM prueba
UNION ALL SELECT '  de ellos, empresas', count(*) FROM empresas_prueba
UNION ALL SELECT '  de ellos, candidatos', count(*) FROM candidatos_prueba
UNION ALL SELECT 'vacantes de empresas de prueba', count(*) FROM vacantes_prueba
UNION ALL SELECT 'postulaciones que se borran',
    count(*) FROM postulaciones
    WHERE vacante_id IN (SELECT id FROM vacantes_prueba) OR candidato_id IN (SELECT id FROM candidatos_prueba)
UNION ALL SELECT 'postulaciones de candidatos que NO son de prueba a vacantes de prueba (también se borran)',
    count(*) FROM postulaciones
    WHERE vacante_id IN (SELECT id FROM vacantes_prueba) AND candidato_id NOT IN (SELECT id FROM candidatos_prueba)
UNION ALL SELECT 'usuarios que se conservan', count(*) FROM usuarios WHERE id NOT IN (SELECT id FROM prueba);

-- Para ver los correos que se borrarían:
-- SELECT email, rol, fecha_creacion FROM usuarios
-- WHERE lower(email) LIKE '%@example.com' AND rol <> 'ADMIN' ORDER BY fecha_creacion;
