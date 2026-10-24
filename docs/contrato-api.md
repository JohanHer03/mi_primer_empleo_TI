# Contrato de la API

Acuerdo entre backend y frontend sobre rutas y DTOs, para construir en paralelo.
Se completa antes de cada fase del plan (`docs/plan-frontend.md`). Las rutas generales están en `docs/fundamentacion.md`.

| Sección | Estado |
| --- | --- |
| Convenciones generales | Propuesta, pendiente de aprobación del backend |
| Autenticación (fase 1) | Propuesta, pendiente de aprobación del backend |
| Endpoints que faltan en la fundamentación | Propuesta, pendiente de aprobación del backend |

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
