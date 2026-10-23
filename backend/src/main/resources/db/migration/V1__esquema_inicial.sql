-- Esquema inicial del MVP (HU-01 a HU-10): las 8 entidades de docs/fundamentacion.md.
-- Enums como VARCHAR + CHECK para mapearlos con @Enumerated(EnumType.STRING).
-- Las tablas de relación usan clave primaria compuesta (no necesitan id propio).

CREATE TABLE usuarios (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('CANDIDATO', 'EMPRESA', 'ADMIN'))
);

CREATE TABLE candidatos (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id        UUID         NOT NULL,
    nombres           VARCHAR(100) NOT NULL,
    apellidos         VARCHAR(100) NOT NULL,
    universidad       VARCHAR(150),
    programa          VARCHAR(150),
    estado_academico  VARCHAR(20)  NOT NULL,
    semestre          SMALLINT,
    ciudad            VARCHAR(100),
    github_url        VARCHAR(255),
    resumen           TEXT,
    fecha_creacion    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_candidatos_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_candidatos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_candidatos_estado_academico CHECK (estado_academico IN ('ESTUDIANTE', 'EGRESADO')),
    CONSTRAINT ck_candidatos_semestre CHECK (semestre IS NULL OR semestre BETWEEN 1 AND 12)
);

CREATE TABLE empresas (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id     UUID         NOT NULL,
    razon_social   VARCHAR(200) NOT NULL,
    nit            VARCHAR(20)  NOT NULL,
    sector         VARCHAR(100),
    ciudad         VARCHAR(100),
    sitio_web      VARCHAR(255),
    verificada     BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_empresas_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_empresas_nit UNIQUE (nit),
    CONSTRAINT fk_empresas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE habilidades (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre         VARCHAR(100) NOT NULL,
    categoria      VARCHAR(20)  NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_habilidades_nombre UNIQUE (nombre),
    CONSTRAINT ck_habilidades_categoria
        CHECK (categoria IN ('LENGUAJE', 'FRAMEWORK', 'BASE_DATOS', 'HERRAMIENTA', 'BLANDA'))
);

CREATE TABLE vacantes (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id        UUID          NOT NULL,
    titulo            VARCHAR(150)  NOT NULL,
    descripcion       TEXT          NOT NULL,
    modalidad         VARCHAR(20)   NOT NULL,
    tipo              VARCHAR(20)   NOT NULL,
    salario_min       NUMERIC(12,2),
    salario_max       NUMERIC(12,2),
    ciudad            VARCHAR(100)  NOT NULL,
    estado            VARCHAR(20)   NOT NULL DEFAULT 'ABIERTA',
    fecha_publicacion TIMESTAMPTZ   NOT NULL DEFAULT now(),
    fecha_cierre      DATE          NOT NULL,
    fecha_creacion    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- Las vacantes se cierran, no se borran: se conserva el historial de postulaciones
    CONSTRAINT fk_vacantes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT ck_vacantes_modalidad CHECK (modalidad IN ('PRESENCIAL', 'HIBRIDA', 'REMOTA')),
    CONSTRAINT ck_vacantes_tipo CHECK (tipo IN ('PRACTICA', 'JUNIOR')),
    CONSTRAINT ck_vacantes_estado CHECK (estado IN ('ABIERTA', 'CERRADA')),
    CONSTRAINT ck_vacantes_salarios CHECK (
        (salario_min IS NULL OR salario_min >= 0)
        AND (salario_max IS NULL OR salario_max >= 0)
        AND (salario_min IS NULL OR salario_max IS NULL OR salario_min <= salario_max)
    )
);

CREATE TABLE postulaciones (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    candidato_id   UUID         NOT NULL,
    vacante_id     UUID         NOT NULL,
    fecha          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    estado         VARCHAR(20)  NOT NULL DEFAULT 'ENVIADA',
    compatibilidad NUMERIC(5,2) NOT NULL,
    -- Regla 1: un candidato no se postula dos veces a la misma vacante
    CONSTRAINT uq_postulaciones_candidato_vacante UNIQUE (candidato_id, vacante_id),
    CONSTRAINT fk_postulaciones_candidato FOREIGN KEY (candidato_id) REFERENCES candidatos (id) ON DELETE CASCADE,
    CONSTRAINT fk_postulaciones_vacante FOREIGN KEY (vacante_id) REFERENCES vacantes (id) ON DELETE RESTRICT,
    CONSTRAINT ck_postulaciones_estado
        CHECK (estado IN ('ENVIADA', 'EN_REVISION', 'ENTREVISTA', 'RECHAZADA', 'CONTRATADO')),
    CONSTRAINT ck_postulaciones_compatibilidad CHECK (compatibilidad BETWEEN 0 AND 100)
);

CREATE TABLE candidato_habilidades (
    candidato_id UUID        NOT NULL,
    habilidad_id UUID        NOT NULL,
    nivel        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_candidato_habilidades PRIMARY KEY (candidato_id, habilidad_id),
    CONSTRAINT fk_candidato_habilidades_candidato
        FOREIGN KEY (candidato_id) REFERENCES candidatos (id) ON DELETE CASCADE,
    -- No se puede borrar del catálogo una habilidad en uso
    CONSTRAINT fk_candidato_habilidades_habilidad
        FOREIGN KEY (habilidad_id) REFERENCES habilidades (id) ON DELETE RESTRICT,
    CONSTRAINT ck_candidato_habilidades_nivel CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO'))
);

CREATE TABLE vacante_habilidades (
    vacante_id      UUID        NOT NULL,
    habilidad_id    UUID        NOT NULL,
    nivel_requerido VARCHAR(20) NOT NULL,
    obligatoria     BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_vacante_habilidades PRIMARY KEY (vacante_id, habilidad_id),
    CONSTRAINT fk_vacante_habilidades_vacante
        FOREIGN KEY (vacante_id) REFERENCES vacantes (id) ON DELETE CASCADE,
    CONSTRAINT fk_vacante_habilidades_habilidad
        FOREIGN KEY (habilidad_id) REFERENCES habilidades (id) ON DELETE RESTRICT,
    CONSTRAINT ck_vacante_habilidades_nivel CHECK (nivel_requerido IN ('BASICO', 'INTERMEDIO', 'AVANZADO'))
);

-- Índices de claves foráneas y búsquedas (las UNIQUE ya crean el suyo)
CREATE INDEX idx_vacantes_empresa ON vacantes (empresa_id);
CREATE INDEX idx_vacantes_estado_cierre ON vacantes (estado, fecha_cierre);
CREATE INDEX idx_vacantes_ciudad ON vacantes (ciudad);
CREATE INDEX idx_vacantes_tipo_modalidad ON vacantes (tipo, modalidad);
CREATE INDEX idx_postulaciones_vacante ON postulaciones (vacante_id);
CREATE INDEX idx_candidato_habilidades_habilidad ON candidato_habilidades (habilidad_id);
CREATE INDEX idx_vacante_habilidades_habilidad ON vacante_habilidades (habilidad_id);
