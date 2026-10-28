# Plan del frontend

Ruta de acción aprobada el 2026-10-23. Sigue la columna de la Persona B del plan de 7 semanas
de `docs/fundamentacion.md`. Cada fase termina con `npm run lint` y `npm run build` sin errores y un pull request.

## Decisiones

| Tema | Decisión |
| --- | --- |
| Rutas | React Router (createBrowserRouter) |
| Peticiones y caché | TanStack Query |
| Formularios | react-hook-form + zod (validación que replica la de Bean Validation) |
| Estilos | CSS Modules + variables en `index.css`; pulido final con el plugin `frontend-design` |
| Token | `localStorage`, con copia en memoria para el cliente HTTP |
| API simulada | Funciones propias en `src/api/mocks/`, activadas con `VITE_USAR_MOCKS=true` |
| Lint | oxlint (el que trae la plantilla actual de Vite) |

## Fases

| Fase | Semana | Alcance | Historias | Estado |
| --- | --- | --- | --- | --- |
| 0. Base | 1 | Proyecto, cliente de la API, sesión, rutas y layouts por rol, pantallas marcador, login simulado, skill `pantalla-react` | — | Hecho |
| 1. Autenticación | 2 | Registro de candidato y de empresa, login real, redirección por rol | HU-01, HU-06 | Hecho; probado contra el backend local (registro, login y 401). Falta probarlo desplegado en Vercel |
| 2. Empresa | 3 | Perfil de empresa (aviso si no está verificada), mis vacantes, publicar, editar y cerrar | HU-06, HU-07 | Pendiente |
| 3. Candidato | 4 | Perfil y habilidades con nivel, búsqueda con filtros y paginación, postularse, mis postulaciones | HU-02, HU-03, HU-05 | Pendiente |
| 4. Compatibilidad | 5 | Detalle de vacante con porcentaje y "lo que te falta"; despliegue en Vercel | HU-04 | Pendiente |
| 5. Gestión y admin | 6 | Postulantes ordenados y cambio de estado; catálogo de habilidades y verificación de empresas | HU-08, HU-09, HU-10 | Pendiente |
| 6. Pulido | 6–7 | Estados de carga y error, diseño visual, versión móvil, pruebas de uso | — | Pendiente |

## Pantallas y rutas

| Ruta | Pantalla | Rol | Fase |
| --- | --- | --- | --- |
| `/` | Landing (sin sesión); con sesión redirige al inicio del rol | público | 1 |
| `/login` | Iniciar sesión | público | 1 |
| `/registro`, `/registro?tipo=empresa` | Crear cuenta (candidato o empresa) | público | 1 |
| `/candidato` | Inicio | CANDIDATO | 3 |
| `/candidato/perfil` | Mi perfil y habilidades | CANDIDATO | 3 |
| `/candidato/vacantes` | Buscar vacantes | CANDIDATO | 3 |
| `/candidato/vacantes/:id` | Detalle con compatibilidad | CANDIDATO | 4 |
| `/candidato/postulaciones` | Mis postulaciones | CANDIDATO | 3 |
| `/empresa` | Inicio | EMPRESA | 2 |
| `/empresa/perfil` | Perfil de empresa | EMPRESA | 2 |
| `/empresa/vacantes` | Mis vacantes | EMPRESA | 2 |
| `/empresa/vacantes/nueva`, `/empresa/vacantes/:id/editar` | Formulario de vacante | EMPRESA | 2 |
| `/empresa/vacantes/:id/postulantes` | Postulantes | EMPRESA | 5 |
| `/admin/habilidades` | Catálogo de habilidades | ADMIN | 5 |
| `/admin/empresas` | Empresas por verificar | ADMIN | 5 |

## Contrato con el backend

Antes de cada fase se acuerdan las rutas y los DTOs en [`docs/contrato-api.md`](contrato-api.md).
Hoy tiene, como propuesta pendiente de aprobación del backend:

- convenciones generales (errores, paginación con `PaginaDto`, fechas);
- autenticación de la fase 1: `POST /api/auth/registro/candidato`, `POST /api/auth/registro/empresa` y `POST /api/auth/login`, que responden `{ "token" }` con un JWT que trae `sub`, `email`, `rol` y `exp`;
- los tres endpoints que faltaban: `GET /api/empresas`, `GET /api/empresas/me/vacantes` y la compatibilidad en `GET /api/vacantes`.
