-- HU-10 y HU-07: catálogo de habilidades (docs/contrato-api.md, sección 4.1).
-- Las vacantes necesitan habilidades del catálogo, así que se carga uno inicial.

-- 1. Nombre único sin distinguir mayúsculas: "kotlin" y "Kotlin" son la misma habilidad.
--    El adaptador de persistencia debe buscar con lower(nombre).
ALTER TABLE habilidades DROP CONSTRAINT uq_habilidades_nombre;
CREATE UNIQUE INDEX uq_habilidades_nombre ON habilidades (lower(nombre));

-- 2. Catálogo inicial (datos de referencia, no personales). ON CONFLICT por si alguna ya existe.
INSERT INTO habilidades (nombre, categoria) VALUES
    ('Java', 'LENGUAJE'),
    ('JavaScript', 'LENGUAJE'),
    ('TypeScript', 'LENGUAJE'),
    ('Python', 'LENGUAJE'),
    ('C#', 'LENGUAJE'),
    ('Kotlin', 'LENGUAJE'),
    ('PHP', 'LENGUAJE'),
    ('Go', 'LENGUAJE'),
    ('SQL', 'LENGUAJE'),
    ('Spring Boot', 'FRAMEWORK'),
    ('React', 'FRAMEWORK'),
    ('Angular', 'FRAMEWORK'),
    ('Vue.js', 'FRAMEWORK'),
    ('Node.js', 'FRAMEWORK'),
    ('Express', 'FRAMEWORK'),
    ('.NET', 'FRAMEWORK'),
    ('Django', 'FRAMEWORK'),
    ('PostgreSQL', 'BASE_DATOS'),
    ('MySQL', 'BASE_DATOS'),
    ('SQL Server', 'BASE_DATOS'),
    ('MongoDB', 'BASE_DATOS'),
    ('Redis', 'BASE_DATOS'),
    ('Git', 'HERRAMIENTA'),
    ('Docker', 'HERRAMIENTA'),
    ('Linux', 'HERRAMIENTA'),
    ('AWS', 'HERRAMIENTA'),
    ('Azure', 'HERRAMIENTA'),
    ('Postman', 'HERRAMIENTA'),
    ('Jira', 'HERRAMIENTA'),
    ('Scrum', 'HERRAMIENTA'),
    ('Trabajo en equipo', 'BLANDA'),
    ('Comunicación', 'BLANDA'),
    ('Resolución de problemas', 'BLANDA'),
    ('Aprendizaje autónomo', 'BLANDA'),
    ('Inglés', 'BLANDA')
ON CONFLICT DO NOTHING;
