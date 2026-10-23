---
name: migracion-flyway
description: Crea migraciones Flyway para el esquema en Supabase (PostgreSQL). Usar cuando se agregue o modifique una tabla, columna, índice o restricción.
---

# Migración Flyway

`CLAUDE.md` manda sobre esta skill. Supabase se usa solo como PostgreSQL: nada de RLS, Auth ni Storage.

## Archivo

- Ubicación: `backend/src/main/resources/db/migration/`.
- Nombre: `V{n}__descripcion_en_snake_case.sql`, con `n` = el siguiente número libre (revisa los existentes).
- **Nunca edites una migración ya aplicada** (en Supabase o en la máquina de la pareja): corrige con una nueva.
  Flyway falla al arrancar si cambia el checksum de una aplicada.

## Convenciones

- Tablas en `snake_case` y plural (`vacantes`, `candidato_habilidades`); columnas en `snake_case`.
- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`.
- Claves foráneas con `ON DELETE` explícito (`CASCADE`, `RESTRICT` o `SET NULL`, según el caso).
- Enums como `VARCHAR(n)` con `CHECK (col IN (...))`, no tipos `ENUM` de Postgres,
  para que JPA los mapee con `@Enumerated(EnumType.STRING)` como String.
- Auditoría: `fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()`.
- Dinero como `NUMERIC(12,2)`; textos largos como `TEXT`.
- Restricciones de negocio en la base cuando aplique, por ejemplo `UNIQUE (candidato_id, vacante_id)`
  en postulaciones o `CHECK (salario_min <= salario_max)`.
- Índices para columnas de búsqueda y para cada clave foránea (Postgres no los crea solo).
- Nombra las restricciones e índices (`uq_...`, `ck_...`, `fk_...`, `idx_...`) para que los errores se entiendan.
- Comentario al inicio del archivo con la historia de usuario o el motivo del cambio.

## Después de crearla

1. Revisa que la entidad JPA coincida con la tabla (nombres, tipos, nulos): `ddl-auto=validate`
   hace fallar el arranque si no coinciden.
2. Ejecuta `./mvnw test` desde `/backend`.
3. Aplícala levantando la API (`./mvnw spring-boot:run`); Flyway migra al arrancar.

## Verificar en local

Con las variables de `.env` cargadas en la terminal, desde `/backend`:

```bash
./mvnw flyway:info       # lista migraciones y su estado (Pending, Success, Failed)
./mvnw flyway:validate   # comprueba que los archivos coinciden con lo aplicado
```

Si una migración queda en `Failed`, no la edites: corrígela con una nueva o pide revisión antes de usar `flyway:repair`.
