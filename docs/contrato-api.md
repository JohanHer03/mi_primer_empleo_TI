# Contrato de la API

Acuerdo entre backend y frontend sobre rutas y DTOs, para construir en paralelo.
Se completa antes de cada fase del plan (`docs/plan-frontend.md`). Las rutas generales están en `docs/fundamentacion.md`.

| Sección | Estado |
| --- | --- |
| Convenciones generales | **Acordado** (2026-10-27). `PaginaDto` se implementa con el primer listado (semana 3) |
| Autenticación (fase 1) | **Acordado** (2026-10-27). Implementado y en producción (PR #11, #12 y #13) |
| Endpoints que faltan en la fundamentación | **Acordado** (2026-10-27). Sus DTOs completos se detallan antes de cada fase |
| Fase 2: habilidades, empresas y vacantes (semana 3) | **Acordado** (2026-10-27, PR #16). Implementado: backend 4.1 a 4.4 y pantallas de empresa |
| Fase 3: candidato y postulaciones (semana 4) | **Acordado** (2026-11-04, PR #21). Precisa 3.3 y adelanta `CalculadoraCompatibilidad`. Implementado: 5.1 y 5.2 |

Cuando el backend apruebe una sección, se cambia su estado a **Acordado** y cualquier cambio posterior se hace con un pull request que toque este archivo.

---

## 1. Convenciones generales

- **Base:** `/api`. En local `http://localhost:8080/api`; en Vercel, el mismo dominio del frontend.
- **Formato:** JSON en UTF-8. Nombres de campo en `camelCase` y en español (`razonSocial`, `fechaCierre`).
- **Identificadores:** UUID como texto (`"3f2c...-..."`).
- **Enums:** texto en mayúsculas, igual que en el dominio (`"CANDIDATO"`, `"ABIERTA"`).
- **Fechas:** solo fecha como `"2026-11-30"` (`LocalDate`); fecha y hora en UTC con zona, como `"2026-10-24T01:18:33Z"` (`Instant`).
- **Campos opcionales:** se envían como `null` o se omiten; nunca como cadena vacía.
- **Normalización:** el backend recorta espacios de los textos y guarda el correo en minúsculas.
- **Autenticación:** encabezado `Authorization: Bearer <token>` en todo, excepto `/api/auth/**` y `/api/docs/**`.

### Errores

Todas las respuestas de error usan el formato uniforme de `CLAUDE.md`:

```json
{
  "status": 409,
  "error": "Conflict",
  "mensaje": "Ya existe una cuenta con ese correo",
  "timestamp": "2026-10-24T01:18:33Z"
}
```

| Código | Cuándo | `mensaje` |
| --- | --- | --- |
| 400 | Validación de Bean Validation | `"campo: mensaje; campo: mensaje"` (lo arma `ManejadorGlobalErrores`) |
| 400 | JSON mal formado | `"El cuerpo de la petición no es válido"` |
| 401 | Sin token, token vencido o inválido, o credenciales incorrectas en el login | Según el caso |
| 403 | Rol sin permiso o recurso de otro usuario (regla 4) | `"No tienes permiso para realizar esta acción"` |
| 404 | Recurso o ruta inexistente | Según el caso |
| 409 | Duplicado o conflicto con una regla de negocio | Según el caso |

El frontend muestra `mensaje` tal cual, así que debe estar en español y ser comprensible para el usuario.
Ante un 401 en una ruta protegida, el frontend cierra la sesión y lleva al login.

### Paginación

Los listados reciben `page` (desde 0) y `size` (por defecto 10, máximo 50) y responden:

```json
{
  "contenido": [],
  "pagina": 0,
  "tamano": 10,
  "totalElementos": 0,
  "totalPaginas": 0
}
```

Es un DTO propio (`PaginaDto<T>`), no la `Page` de Spring, para no exponer sus campos internos.

---

## 2. Autenticación (fase 1: HU-01 y HU-06)

### 2.1 Token JWT

- Firmado con HS256 usando `JWT_SECRET`. Vence a los **120 minutos** (`app.jwt.expiracion-minutos`).
- Sin token de refresco en el MVP: cuando vence, el usuario vuelve a iniciar sesión.
- **Claims** que el frontend lee del token (ya implementado en `frontend/src/auth/sesion.ts`):

| Claim | Tipo | Contenido |
| --- | --- | --- |
| `sub` | texto | Id del usuario (UUID) |
| `email` | texto | Correo del usuario |
| `rol` | texto | `CANDIDATO`, `EMPRESA` o `ADMIN` |
| `iat` | número | Emisión, en segundos desde epoch |
| `exp` | número | Vencimiento, en segundos desde epoch |

### 2.2 Respuesta de autenticación (`AuthRespuesta`)

Login y registro responden lo mismo, así el usuario queda con la sesión iniciada al registrarse:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### 2.3 `POST /api/auth/registro/candidato`

> **Cambio respecto a la fundamentación**, que tenía un solo `POST /api/auth/registro`: dos rutas,
> una por rol, permiten un DTO con validaciones propias para cada uno en vez de campos condicionales.
> Los administradores no se registran por la API: se crean con una migración o a mano.

**Petición** (`RegistroCandidatoPeticion`):

```json
{
  "email": "laura.gomez@udea.edu.co",
  "password": "Medellin2026",
  "nombres": "Laura",
  "apellidos": "Gómez Restrepo",
  "estadoAcademico": "ESTUDIANTE",
  "semestre": 9,
  "universidad": "Universidad de Antioquia",
  "programa": "Ingeniería de Sistemas",
  "ciudad": "Medellín"
}
```

| Campo | Obligatorio | Validación (Bean Validation y esquema) |
| --- | --- | --- |
| `email` | Sí | `@NotBlank @Email @Size(max = 255)` |
| `password` | Sí | `@NotBlank @Size(min = 8, max = 72)`. El máximo de 72 es el límite de BCrypt |
| `nombres` | Sí | `@NotBlank @Size(max = 100)` |
| `apellidos` | Sí | `@NotBlank @Size(max = 100)` |
| `estadoAcademico` | Sí | `@NotNull`, `ESTUDIANTE` o `EGRESADO` |
| `semestre` | Según el estado | Entre 1 y 12. **Obligatorio si `ESTUDIANTE`, `null` si `EGRESADO`** (restricción `ck_candidatos_semestre_estado` de V3; se valida en el caso de uso con 400) |
| `universidad` | No | `@Size(max = 150)` |
| `programa` | No | `@Size(max = 150)` |
| `ciudad` | No | `@Size(max = 100)` |

**Respuestas:**

| Código | Cuerpo | Cuándo |
| --- | --- | --- |
| 201 | `AuthRespuesta` | Cuenta creada (usuario con rol `CANDIDATO` y su candidato, en una transacción) |
| 400 | Error | Validación, o semestre incoherente con el estado académico |
| 409 | Error: `"Ya existe una cuenta con ese correo"` | Correo repetido, sin distinguir mayúsculas (índice `uq_usuarios_email` de V3) |

### 2.4 `POST /api/auth/registro/empresa`

**Petición** (`RegistroEmpresaPeticion`):

```json
{
  "email": "talento@softwaremedellin.co",
  "password": "Medellin2026",
  "razonSocial": "Software Medellín S.A.S.",
  "nit": "9001234567",
  "sector": "Desarrollo de software",
  "ciudad": "Medellín",
  "sitioWeb": "https://softwaremedellin.co"
}
```

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `email` | Sí | Igual que en el candidato |
| `password` | Sí | Igual que en el candidato |
| `razonSocial` | Sí | `@NotBlank @Size(max = 200)` |
| `nit` | Sí | `@NotBlank`. El backend quita puntos, guiones y espacios y exige `^[0-9]{6,15}$`, con el dígito de verificación incluido (restricción `ck_empresas_nit_digitos` de V3). El frontend aplica la misma limpieza antes de enviar |
| `sector` | No | `@Size(max = 100)` |
| `ciudad` | No | `@Size(max = 100)` |
| `sitioWeb` | No | `@Size(max = 255)`, URL `http` o `https` |

**Respuestas:**

| Código | Cuerpo | Cuándo |
| --- | --- | --- |
| 201 | `AuthRespuesta` | Cuenta creada con `verificada = false` (regla 3: puede iniciar sesión, pero no publicar hasta que un administrador la verifique) |
| 400 | Error | Validación o NIT con formato inválido |
| 409 | Error: `"Ya existe una cuenta con ese correo"` | Correo repetido |
| 409 | Error: `"Ya existe una empresa registrada con ese NIT"` | NIT repetido (`uq_empresas_nit`) |

### 2.5 `POST /api/auth/login`

**Petición** (`LoginPeticion`):

```json
{ "email": "laura.gomez@udea.edu.co", "password": "Medellin2026" }
```

| Campo | Validación |
| --- | --- |
| `email` | `@NotBlank @Email` |
| `password` | `@NotBlank` |

**Respuestas:**

| Código | Cuerpo | Cuándo |
| --- | --- | --- |
| 200 | `AuthRespuesta` | Credenciales correctas |
| 400 | Error | Validación |
| 401 | Error: `"Correo o contraseña incorrectos"` | Correo inexistente **o** contraseña errónea; el mismo mensaje en ambos casos, para no revelar qué correos existen |
| 403 | Error: `"Tu cuenta está desactivada"` | `usuarios.activo = false` |

---

## 3. Endpoints que faltan en la fundamentación

Propuestos para las fases 2, 3 y 5. Los DTOs completos se detallan en este archivo antes de cada fase.

### 3.1 `GET /api/empresas?verificada=false&page=0&size=10` — ADMIN (HU-10)

Lista las empresas para que el administrador las verifique. `verificada` es opcional (sin él, lista todas).
Responde `PaginaDto<EmpresaResumen>`, ordenado por `fechaCreacion` ascendente (las más antiguas primero):

```json
{
  "id": "…",
  "razonSocial": "Software Medellín S.A.S.",
  "nit": "9001234567",
  "email": "talento@softwaremedellin.co",
  "sector": "Desarrollo de software",
  "ciudad": "Medellín",
  "sitioWeb": "https://softwaremedellin.co",
  "verificada": false,
  "fechaCreacion": "2026-10-24T01:18:33Z"
}
```

Se complementa con el `PATCH /api/empresas/{id}/verificar` que ya está en la fundamentación.

### 3.2 `GET /api/empresas/me/vacantes?estado=ABIERTA&page=0&size=10` — EMPRESA (HU-07, HU-08)

Lista las vacantes de la empresa autenticada; nunca las de otra (regla 4). `estado` es opcional.
Responde `PaginaDto<VacanteDeEmpresaResumen>`, ordenado por `fechaPublicacion` descendente:

```json
{
  "id": "…",
  "titulo": "Desarrollador Java junior",
  "tipo": "JUNIOR",
  "modalidad": "HIBRIDA",
  "ciudad": "Medellín",
  "estado": "ABIERTA",
  "fechaPublicacion": "2026-10-24T01:18:33Z",
  "fechaCierre": "2026-11-30",
  "totalPostulaciones": 12
}
```

### 3.3 Compatibilidad en `GET /api/vacantes` — CANDIDATO (HU-03)

HU-03 pide ordenar las vacantes por compatibilidad, así que el listado la incluye cuando consulta un candidato.

- **Filtros:** `modalidad`, `tipo`, `habilidadId`, `ciudad`, `page`, `size`; todos opcionales.
- **Orden:** `orden=compatibilidad` (por defecto para el candidato) u `orden=fechaPublicacion`.
- Solo muestra vacantes `ABIERTA` con `fechaCierre` vigente.
- `compatibilidad` (0 a 100, con un decimal) se calcula al consultar con `CalculadoraCompatibilidad` y **no se guarda**: lo que se guarda es la de la postulación (regla 6). Para otros roles va en `null`.
- **Nota para el backend:** para ordenar por un valor calculado, el caso de uso filtra en la base, calcula y ordena en memoria, y después pagina. Es suficiente para el volumen del MVP (cientos de vacantes).

Responde `PaginaDto<VacanteResumen>`:

```json
{
  "id": "…",
  "titulo": "Desarrollador Java junior",
  "empresa": { "id": "…", "razonSocial": "Software Medellín S.A.S." },
  "tipo": "JUNIOR",
  "modalidad": "HIBRIDA",
  "ciudad": "Medellín",
  "salarioMin": 2800000,
  "salarioMax": 3500000,
  "fechaCierre": "2026-11-30",
  "compatibilidad": 83.3
}
```

El detalle con "lo que te falta" sigue en `GET /api/vacantes/{id}/compatibilidad` (fundamentación, HU-04).

---

## 4. Fase 2: habilidades, empresas y vacantes (semana 3: HU-06, HU-07 y parte de HU-10)

Entregable: una empresa verificada publica, edita, cierra y lista sus vacantes.
Para poder probarlo de punta a punta se adelanta el **backend** de la verificación de empresas (HU-10);
su pantalla sigue en la fase 5 del frontend.

**Fechas "vigentes":** se comparan con el día actual en hora de Colombia (`America/Bogota`), igual que la restricción `ck_vacantes_fecha_cierre` de V3.

### 4.1 Habilidades

`HabilidadDto`:

```json
{ "id": "…", "nombre": "Spring Boot", "categoria": "FRAMEWORK" }
```

`categoria`: `LENGUAJE`, `FRAMEWORK`, `BASE_DATOS`, `HERRAMIENTA` o `BLANDA`.

| Endpoint | Rol | Respuesta |
| --- | --- | --- |
| `GET /api/habilidades?categoria=FRAMEWORK` | Autenticado | 200 con **lista** `HabilidadDto[]`, ordenada por categoría y nombre. `categoria` es opcional |
| `POST /api/habilidades` | ADMIN | 201 `HabilidadDto`; 400 validación; 409 `"Ya existe una habilidad con ese nombre"` |

- **Excepción a la paginación:** el catálogo responde la lista completa porque alimenta los selectores
  de los formularios y es pequeño (decenas de elementos). Si pasara de 200, se pagina.
- **Cuerpo del POST:** `{ "nombre": "Kotlin", "categoria": "LENGUAJE" }`. `nombre`: `@NotBlank @Size(max = 100)`, se recorta.
  `categoria`: `@NotNull`. El nombre es único **sin distinguir mayúsculas** (`kotlin` = `Kotlin`).
- Editar y borrar habilidades quedan fuera del MVP (no están en la fundamentación).

**Migración `V4__catalogo_habilidades.sql`:**

1. Cambia `uq_habilidades_nombre` por un índice único sobre `lower(nombre)`, como se hizo con el correo en V3.
2. Inserta el catálogo inicial (catálogo de referencia, no datos personales):

| Categoría | Habilidades |
| --- | --- |
| LENGUAJE | Java, JavaScript, TypeScript, Python, C#, Kotlin, PHP, Go, SQL |
| FRAMEWORK | Spring Boot, React, Angular, Vue.js, Node.js, Express, .NET, Django |
| BASE_DATOS | PostgreSQL, MySQL, SQL Server, MongoDB, Redis |
| HERRAMIENTA | Git, Docker, Linux, AWS, Azure, Postman, Jira, Scrum |
| BLANDA | Trabajo en equipo, Comunicación, Resolución de problemas, Aprendizaje autónomo, Inglés |

### 4.2 Perfil de empresa (HU-06)

`EmpresaPerfilDto`:

```json
{
  "id": "…",
  "razonSocial": "Software Medellín S.A.S.",
  "nit": "9001234567",
  "email": "talento@softwaremedellin.co",
  "sector": "Desarrollo de software",
  "ciudad": "Medellín",
  "sitioWeb": "https://softwaremedellin.co",
  "verificada": false
}
```

| Endpoint | Rol | Respuesta |
| --- | --- | --- |
| `GET /api/empresas/me` | EMPRESA | 200 `EmpresaPerfilDto` |
| `PUT /api/empresas/me` | EMPRESA | 200 `EmpresaPerfilDto`; 400 validación |

- **Cuerpo del PUT** (`ActualizarEmpresaPeticion`): `razonSocial`, `sector`, `ciudad` y `sitioWeb`, con las mismas validaciones que el registro (2.4).
- El **NIT y el correo no se editan**: el NIT identifica a la empresa que el administrador verificó,
  y el correo es la cuenta de inicio de sesión.
- La empresa siempre trabaja sobre su propio perfil (el id sale del token), así que no hay 403 por propiedad.

### 4.3 Verificación de empresas (HU-10, solo backend en esta fase)

| Endpoint | Rol | Respuesta |
| --- | --- | --- |
| `GET /api/empresas?verificada=false&page=0&size=10` | ADMIN | 200 `PaginaDto<EmpresaResumen>` (definido en 3.1) |
| `PATCH /api/empresas/{id}/verificar` | ADMIN | 200 `EmpresaResumen`; 404 `"La empresa no existe"` |

- Sin cuerpo. Es **idempotente**: verificar una empresa ya verificada responde 200 sin cambios.
- Quitar la verificación queda fuera del MVP.

### 4.4 Vacantes (HU-07)

**Entrada** (`VacantePeticion`, la misma para crear y editar):

```json
{
  "titulo": "Desarrollador Java junior",
  "descripcion": "Apoyarás el desarrollo de APIs REST para clientes del sector financiero en Medellín…",
  "tipo": "JUNIOR",
  "modalidad": "HIBRIDA",
  "ciudad": "Medellín",
  "salarioMin": 2800000,
  "salarioMax": 3500000,
  "fechaCierre": "2026-11-30",
  "habilidades": [
    { "habilidadId": "…", "nivelRequerido": "INTERMEDIO", "obligatoria": true },
    { "habilidadId": "…", "nivelRequerido": "BASICO", "obligatoria": false }
  ]
}
```

| Campo | Validación |
| --- | --- |
| `titulo` | `@NotBlank @Size(max = 150)` |
| `descripcion` | `@NotBlank @Size(max = 5000)` |
| `tipo` | `@NotNull`, `PRACTICA` o `JUNIOR` |
| `modalidad` | `@NotNull`, `PRESENCIAL`, `HIBRIDA` o `REMOTA` |
| `ciudad` | `@NotBlank @Size(max = 100)` |
| `salarioMin`, `salarioMax` | Opcionales. Pesos colombianos sin decimales, `>= 0`. Si vienen los dos, `salarioMin <= salarioMax` (dominio, 400) |
| `fechaCierre` | `@NotNull`. Hoy o después, en hora de Colombia (dominio, 400) |
| `habilidades` | `@NotEmpty @Size(max = 15) @Valid`. Sin habilidades repetidas y **al menos una obligatoria** (dominio, 400). Cada `habilidadId` debe existir en el catálogo (400 `"habilidades: la habilidad … no existe"`) |
| `habilidades[].nivelRequerido` | `@NotNull`, `BASICO`, `INTERMEDIO` o `AVANZADO` |
| `habilidades[].obligatoria` | `@NotNull` |

Al menos una obligatoria, porque la compatibilidad (`CLAUDE.md`) pesa doble esas habilidades y una vacante
sin requisitos obligatorios no explica "lo que te falta".

**Salida** (`VacanteDetalle`):

```json
{
  "id": "…",
  "titulo": "Desarrollador Java junior",
  "descripcion": "…",
  "tipo": "JUNIOR",
  "modalidad": "HIBRIDA",
  "ciudad": "Medellín",
  "salarioMin": 2800000,
  "salarioMax": 3500000,
  "estado": "ABIERTA",
  "fechaPublicacion": "2026-10-28T15:00:00Z",
  "fechaCierre": "2026-11-30",
  "empresa": { "id": "…", "razonSocial": "Software Medellín S.A.S." },
  "habilidades": [
    { "habilidadId": "…", "nombre": "Java", "categoria": "LENGUAJE", "nivelRequerido": "INTERMEDIO", "obligatoria": true }
  ]
}
```

Las habilidades salen ordenadas: primero las obligatorias y luego por nombre.

| Endpoint | Rol | Respuesta | Errores |
| --- | --- | --- | --- |
| `POST /api/vacantes` | EMPRESA | 201 `VacanteDetalle` | 400 validación; **403** `"Tu empresa debe estar verificada para publicar vacantes"` (regla 3) |
| `PUT /api/vacantes/{id}` | EMPRESA dueña | 200 `VacanteDetalle` | 400; 403 si es de otra empresa (regla 4); 404 `"La vacante no existe"`; 409 `"No se puede editar una vacante cerrada"` |
| `PATCH /api/vacantes/{id}/cerrar` | EMPRESA dueña | 200 `VacanteDetalle` con `estado: "CERRADA"` | 403 (regla 4); 404 |
| `GET /api/empresas/me/vacantes` | EMPRESA | 200 `PaginaDto<VacanteDeEmpresaResumen>` (definido en 3.2) | — |
| `GET /api/vacantes/{id}` | Autenticado | 200 `VacanteDetalle` | 403 si una EMPRESA pide la vacante de otra (regla 4); 404 |

- **Editar** reemplaza todos los campos, incluida la lista de habilidades. Las postulaciones existentes conservan
  la compatibilidad que se guardó al postularse (regla 6).
- **Cerrar** es idempotente (cerrar una cerrada responde 200). Reabrir queda fuera del MVP.
- **Vacantes vencidas:** una vacante `ABIERTA` con `fechaCierre` pasada no se cierra sola; el listado de candidatos
  (3.3) ya la excluye y el frontend la muestra como "vencida" comparando `fechaCierre` con la fecha de hoy.
- **Candidatos y administrador** pueden ver el detalle de cualquier vacante, también las cerradas
  (un candidato debe poder revisar la vacante de una postulación antigua).
- El listado para candidatos con compatibilidad (`GET /api/vacantes`, 3.3) es de la semana 4.

### 4.5 Postman y frontend

- **Postman** (skill `endpoint-postman`): carpetas Habilidades, Empresas y Vacantes. Como el administrador no se crea
  por la API, la colección inicia sesión con el administrador inicial de cada entorno (`ADMIN_EMAIL` y `ADMIN_PASSWORD`
  del `.env`). Sus credenciales se pasan al ejecutar, **nunca se guardan en el repo**:
  `npx newman run ... --env-var emailAdmin=... --env-var passwordAdmin=...`.
  Flujo: registrar empresa → publicar sin verificar (403) → el admin la verifica → publicar (201) → editar → cerrar → editar cerrada (409),
  y una segunda empresa que intenta editar la vacante ajena (403).
- **Frontend** (fase 2 de `docs/plan-frontend.md`): `/empresa/perfil`, `/empresa/vacantes`, `/empresa/vacantes/nueva`
  y `/empresa/vacantes/:id/editar`, primero con API simulada que siga esta sección.

---

## 5. Fase 3: candidato y postulaciones (semana 4: HU-02, HU-03 y HU-05)

Entregable: un candidato completa su perfil y sus habilidades, busca vacantes con filtros y se postula.
Para poder guardar la compatibilidad al postularse (regla 6) se **adelanta `CalculadoraCompatibilidad` a esta semana**;
en la semana 5 quedan el detalle con "lo que te falta" (`GET /api/vacantes/{id}/compatibilidad`, HU-04), más pruebas y su pantalla.

### 5.1 Perfil de candidato (HU-02)

`CandidatoPerfil`:

```json
{
  "id": "…",
  "email": "laura.gomez@udea.edu.co",
  "nombres": "Laura",
  "apellidos": "Gómez Restrepo",
  "estadoAcademico": "ESTUDIANTE",
  "semestre": 9,
  "universidad": "Universidad de Antioquia",
  "programa": "Ingeniería de Sistemas",
  "ciudad": "Medellín",
  "githubUrl": "https://github.com/lauragomez",
  "resumen": "Me interesa el desarrollo backend con Java…",
  "habilidades": [
    { "habilidadId": "…", "nombre": "Java", "categoria": "LENGUAJE", "nivel": "INTERMEDIO" }
  ]
}
```

| Endpoint | Rol | Respuesta |
| --- | --- | --- |
| `GET /api/candidatos/me` | CANDIDATO | 200 `CandidatoPerfil` |
| `PUT /api/candidatos/me` | CANDIDATO | 200 `CandidatoPerfil`; 400 validación |

- **Cuerpo del PUT** (`ActualizarCandidatoPeticion`): `nombres`, `apellidos`, `estadoAcademico`, `semestre`, `universidad`,
  `programa` y `ciudad` con las mismas validaciones del registro (2.3), más:

| Campo | Validación |
| --- | --- |
| `githubUrl` | Opcional. `@Size(max = 255)`, debe empezar por `https://github.com/` |
| `resumen` | Opcional. `@Size(max = 1000)` |

- El **correo no se edita** (es la cuenta de inicio de sesión) y las habilidades van por su propio endpoint (5.2).
- Las habilidades salen en el orden del catálogo (categoría y nombre, como en 4.1).
- Igual que la empresa en 4.2, el id sale del token: no hay 403 por propiedad.

### 5.2 Habilidades del candidato (HU-02)

`PUT /api/candidatos/me/habilidades` — CANDIDATO. Reemplaza la lista completa y responde 200 con la lista guardada
(`CandidatoHabilidad[]`, el mismo formato de `habilidades` en 5.1).

```json
[
  { "habilidadId": "…", "nivel": "INTERMEDIO" },
  { "habilidadId": "…", "nivel": "BASICO" }
]
```

| Regla | Validación |
| --- | --- |
| Tamaño | De 0 a 30 (`@Size(max = 30)`). Una lista vacía borra todas: la compatibilidad queda en 0 |
| `habilidadId` | `@NotNull`, debe existir en el catálogo (400 `"habilidades: la habilidad … no existe"`, como en 4.4) |
| `nivel` | `@NotNull`, `BASICO`, `INTERMEDIO` o `AVANZADO` |
| Repetidas | No se repite una habilidad (dominio, 400 `"habilidades: no se puede repetir una habilidad"`) |

El cambio de habilidades no recalcula postulaciones ya enviadas: conservan la compatibilidad guardada (regla 6).

### 5.3 Compatibilidad (`CalculadoraCompatibilidad`, dominio)

La fórmula es la de `CLAUDE.md`. Por cada habilidad de la vacante:

- puntaje = `min(1, nivelCandidato / nivelRequerido)`, con BASICO = 1, INTERMEDIO = 2 y AVANZADO = 3;
  si el candidato no la tiene, el puntaje es 0;
- peso = 2 si es obligatoria y 1 si es deseable.

Resultado = suma de (puntaje × peso) / suma de pesos × 100, **redondeado a un decimal** (mitad hacia arriba).
Las habilidades con puntaje menor que 1 forman la lista "lo que te falta" (se expone en la semana 5).

**Ejemplo** (vacante del 4.4 con Spring Boot añadida):

| Habilidad de la vacante | Peso | Candidato | Puntaje | Aporte |
| --- | --- | --- | --- | --- |
| Java, INTERMEDIO, obligatoria | 2 | BASICO | 1/2 = 0,5 | 1 |
| Spring Boot, BASICO, obligatoria | 2 | AVANZADO | min(1, 3/1) = 1 | 2 |
| Git, BASICO, deseable | 1 | No la tiene | 0 | 0 |
| **Total** | **5** | | | **3** |

Compatibilidad = 3 / 5 × 100 = **60.0**. Le faltan Java (tiene BASICO, piden INTERMEDIO) y Git.

Pruebas mínimas de la calculadora (prioridad de `CLAUDE.md`): todo cumplido = 100, sin habilidades = 0,
nivel superior no pasa de 1, peso doble de las obligatorias y redondeo.

### 5.4 Búsqueda de vacantes (HU-03)

Es el `GET /api/vacantes` de la sección 3.3, con estas precisiones:

- **Rol: solo CANDIDATO** (403 para los demás). La fundamentación decía "autenticado", pero una empresa no debe recorrer
  las vacantes de otras (regla 4) y el administrador no lo necesita en el MVP.
- Filtros: `modalidad`, `tipo` (el "nivel" de HU-03: `PRACTICA` o `JUNIOR`), `habilidadId`, `ciudad` (sin distinguir
  mayúsculas ni espacios a los lados), `page` y `size`. Un valor de enum desconocido responde 400.
- `orden=compatibilidad` (por defecto) o `orden=fechaPublicacion` (las más recientes primero). Con empate en compatibilidad,
  primero la más reciente.
- `VacanteResumen` agrega dos campos al ejemplo de 3.3:

| Campo | Contenido |
| --- | --- |
| `fechaPublicacion` | `Instant`, para mostrar "publicada hace…" |
| `postulado` | `true` si el candidato ya se postuló a esa vacante (el frontend muestra "Ya te postulaste" en vez del botón) |

Sin resultados responde 200 con `contenido` vacío.

### 5.5 Postularse (HU-05, reglas 1, 2 y 6)

`POST /api/vacantes/{id}/postulaciones` — CANDIDATO. Sin cuerpo.

| Código | Cuándo |
| --- | --- |
| 201 `PostulacionDeCandidato` | Postulación creada en `ENVIADA`, con la compatibilidad calculada en ese momento y guardada (regla 6) |
| 404 `"La vacante no existe"` | |
| 409 `"Ya te postulaste a esta vacante"` | Regla 1. También si dos peticiones llegan a la vez (`uq_postulaciones_candidato_vacante`) |
| 409 `"Esta vacante ya no recibe postulaciones"` | Regla 2: la vacante está `CERRADA` o su `fechaCierre` ya pasó (hora de Colombia) |

Un candidato sin habilidades puede postularse (queda con compatibilidad 0); el frontend le sugiere completarlas antes.
Retirar una postulación queda fuera del MVP (no está en la fundamentación).

### 5.6 Mis postulaciones (HU-05)

`GET /api/postulaciones/me?estado=ENVIADA&page=0&size=10` — CANDIDATO. `estado` es opcional.
Responde `PaginaDto<PostulacionDeCandidato>`, las más recientes primero:

```json
{
  "id": "…",
  "fecha": "2026-11-10T14:05:00Z",
  "estado": "ENVIADA",
  "compatibilidad": 60.0,
  "vacante": {
    "id": "…",
    "titulo": "Desarrollador Java junior",
    "empresa": { "id": "…", "razonSocial": "Software Medellín S.A.S." },
    "modalidad": "HIBRIDA",
    "ciudad": "Medellín",
    "estado": "ABIERTA",
    "fechaCierre": "2026-11-30"
  }
}
```

`estado` de la postulación: `ENVIADA`, `EN_REVISION`, `ENTREVISTA`, `RECHAZADA` o `CONTRATADO` (regla 5; la empresa los cambia
en la semana 6). Solo lista las del candidato del token.

### 5.7 Postman y frontend

- **Postman** (skill `endpoint-postman`): carpetas Candidatos y Postulaciones. Flujo: registrar candidato → editar perfil →
  definir habilidades (y una repetida, 400) → buscar con filtros → postularse a la vacante abierta de la carpeta Vacantes (201)
  → postularse otra vez (409) → postularse a la vacante cerrada (409) → mis postulaciones; y una empresa que intenta buscar (403).
- **Frontend** (fase 3 de `docs/plan-frontend.md`): `/candidato` (inicio), `/candidato/perfil` (datos y habilidades con nivel),
  `/candidato/vacantes` (filtros, orden, paginación y botón "Postularme" en cada tarjeta) y `/candidato/postulaciones`,
  primero con API simulada que siga esta sección. El detalle con compatibilidad (`/candidato/vacantes/:id`) es de la fase 4.
