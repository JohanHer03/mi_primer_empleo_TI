# CONTEXT.md — Contexto vivo del proyecto

Este archivo responde *qué* estamos construyendo, *por qué* y *en qué punto vamos*.
Las reglas de *cómo* trabajar están en `CLAUDE.md`. Actualízalo al cerrar cada funcionalidad o decisión.

Última actualización: 2026-10-23 · Semana 1 (base del backend)

## Propósito

Plataforma web de primer empleo TI para Medellín. Conecta a estudiantes de últimos semestres y
recién graduados de software con vacantes de práctica y nivel junior. Calcula la compatibilidad
por habilidades y le muestra al candidato qué le falta para cada vacante.

- Problema: en Colombia 2,33 millones de jóvenes de 15 a 28 años no estudian ni trabajan (DANE, jun–ago 2026)
  y el desempleo juvenil es 14,7 % (12,9 % en Medellín A.M.), mientras el sector TI reporta déficit de desarrolladores.
- Alineación: ODS 8, metas 8.6 y 8.b.
- Diferencial frente a Torre, Get on Board y Handshake: solo vacantes alcanzables para un junior
  y explicación de la brecha de habilidades ("esto te falta para encajar").

## Usuarios

| Rol | Quién es | Objetivo |
| --- | --- | --- |
| Candidato | Estudiante de últimos semestres o egresado de hasta 2 años | Encontrar vacantes a su alcance y saber qué le falta |
| Empresa | Empresa con área TI que contrata practicantes o juniors | Publicar vacantes y ver postulantes ordenados por compatibilidad |
| Administrador | Equipo de la plataforma | Gestionar el catálogo de habilidades y verificar empresas |

## Glosario del dominio

| Término | Significado en este proyecto |
| --- | --- |
| Vacante | Oferta de tipo `PRACTICA` o `JUNIOR` publicada por una empresa verificada |
| Postulación | Aplicación de un candidato a una vacante; guarda la compatibilidad calculada en ese momento |
| Habilidad | Elemento del catálogo (lenguaje, framework, base de datos, herramienta o habilidad blanda) |
| Nivel | `BASICO`=1, `INTERMEDIO`=2, `AVANZADO`=3 |
| Habilidad obligatoria | Requisito de la vacante con peso 2 en la compatibilidad (las deseables pesan 1) |
| Compatibilidad | Porcentaje de 0 a 100 que mide qué tanto cubre el candidato lo que pide la vacante |
| Brecha / "lo que te falta" | Habilidades donde el nivel del candidato es menor que el requerido |
| Empresa verificada | Empresa aprobada por un administrador; solo ellas pueden publicar |

## Alcance del MVP

Historias HU-01 a HU-10 de `docs/fundamentacion.md`.
Fuera del MVP: notificaciones por correo, carga de hoja de vida en PDF, estadísticas.

## Registro de decisiones

Las más recientes arriba. Una línea por decisión: fecha, decisión y motivo.

| Fecha | Decisión | Motivo |
| --- | --- | --- |
| 2026-10-23 | Vercel en la región `pdx1` (Portland) | Supabase está en us-west-2 (Oregón): las consultas no cruzan el país |
| 2026-10-23 | nginx del frontend escucha en `${PORT}` (plantilla de la imagen oficial, 80 por defecto) | La documentación de Vercel exige escuchar en el puerto que indica `PORT` |
| 2026-10-23 | El dominio de producción `*.vercel.app` es público; las URL de cada deploy piden login de Vercel | Protección estándar de Vercel. Antes de tener datos reales, revisar si se protege también producción |
| 2026-10-23 | Frontend: React Router, TanStack Query, react-hook-form + zod (`@hookform/resolvers` 5.9: la 6.0 tiene un conflicto de dependencias), CSS Modules, oxlint | Ruta aprobada en `docs/plan-frontend.md` |
| 2026-10-23 | Token JWT en `localStorage` con copia en memoria; el frontend lee `sub`, `email`, `rol` y `exp` del token | Simple y sobrevive a recargas; el backend debe incluir esos campos en el JWT |
| 2026-10-23 | API simulada con funciones propias en `src/api/mocks/` (`VITE_USAR_MOCKS=true`) | El frontend avanza sin esperar cada endpoint; sin dependencias extra |
| 2026-10-23 | Email único sin distinguir mayúsculas (índice sobre `lower(email)`, V3) | Evita cuentas duplicadas; el adaptador debe buscar con `findByEmailIgnoreCase` |
| 2026-10-23 | NIT guardado solo con dígitos (incluye dígito de verificación) | Que `UNIQUE (nit)` detecte el mismo NIT escrito con puntos o guion; el caso de uso lo normaliza |
| 2026-10-23 | `semestre` obligatorio para `ESTUDIANTE` y vacío para `EGRESADO`; `fecha_cierre` ≥ día de publicación (hora de Colombia) | Coherencia de datos garantizada también en la base |
| 2026-10-23 | `ManejadorGlobalErrores` hereda de `ResponseEntityExceptionHandler` y hay un `ControladorErrores` propio | Todos los errores de Spring MVC y los de filtros salen con su código correcto y el formato uniforme |
| 2026-10-23 | En las pruebas también se inyecta por constructor (`@Autowired` en el constructor) | Cumple la regla de `CLAUDE.md` sin excepciones |
| 2026-10-23 | Spring Boot 3.5.16, MapStruct 1.6.3 y springdoc 2.9.1; sin Lombok | MapStruct reduce los mappers de la hexagonal; los DTOs ya son `record` y el dominio explícito se defiende mejor |
| 2026-10-23 | Swagger en `/api/docs/swagger-ui.html` y OpenAPI en `/api/docs/openapi` | En Vercel solo `/api/*` llega al backend |
| 2026-10-23 | Conexión a Supabase por el Session pooler `aws-1-us-west-2` (puerto 5432) | La conexión directa solo tiene IPv6; `aws-0` no reconoce el proyecto |
| 2026-10-23 | Migración V2 retira permisos de `anon` y `authenticated` en `public` | Cierra la Data API de Supabase sin usar RLS; solo el backend accede por JDBC |
| 2026-10-23 | Tablas de relación (`candidato_habilidades`, `vacante_habilidades`) con clave primaria compuesta | No necesitan id propio; en JPA se mapean con `@EmbeddedId` |
| 2026-10-23 | Empresas → vacantes y vacantes → postulaciones con `ON DELETE RESTRICT` | Las vacantes se cierran, no se borran: se conserva el historial |
| 2026-10-23 | Sin prueba `contextLoads`; pruebas web con `@WebMvcTest` | `./mvnw test` no debe depender de Supabase ni del `.env` |
| 2026-10-23 | El agente `revisor-arquitectura` es solo lectura (Read, Grep, Glob) y recibe la lista de archivos cambiados de quien lo invoca | No puede ejecutar `git diff`; así no edita ni corre comandos |
| 2026-10-21 | Supabase solo como PostgreSQL; autenticación con Spring Security + JWT | Requisito académico de usar Spring Security |
| 2026-10-21 | Despliegue de front y back en Vercel con Docker, en un solo proyecto | Indicación del tutor; mismo dominio evita CORS. Plan B: backend en Render |
| 2026-10-21 | Frontend en React + Vite + TypeScript | Indicación del tutor |
| 2026-10-21 | Todo el backend en Spring Boot; el dominio sin anotaciones JPA | Arquitectura hexagonal exigida |
| 2026-10-21 | IDs UUID | Encajan con PostgreSQL de Supabase |
| 2026-10-21 | Alcance geográfico: Medellín, con datos nacionales de contexto | Datos locales disponibles y caso más defendible |

## Estado actual

| Área | Estado | Notas |
| --- | --- | --- |
| Fundamentación | Hecho | `docs/fundamentacion.md` |
| CLAUDE.md y CONTEXT.md | Hecho | |
| Archivos de despliegue | Hecho | `vercel.json`, `Dockerfile.vercel`, `docker-compose.yml` |
| Skills y agentes de Claude Code | En curso | Hechos: skills `nueva-funcionalidad-hexagonal` y `migracion-flyway`, agente `revisor-arquitectura`. Faltan según `docs/prompts-claude-code.md` |
| Proyecto Spring Boot | Hecho | Paquetes hexagonales, errores uniformes, `SecurityConfig` sin estado (sin JWT aún), CORS, Swagger. Imagen `Dockerfile.vercel` probada con `docker compose up --build backend` (responde 401 en JSON) |
| Proyecto React (fase 0 de `docs/plan-frontend.md`) | Hecho | Vite + React 19 + TS, cliente único de la API, sesión con JWT, rutas y layouts por rol, 14 pantallas marcador, login simulado. Probado con `docker compose` junto al backend |
| Frontend fases 1 a 6 | Pendiente | Ver `docs/plan-frontend.md` |
| Esquema y migraciones | Hecho | V1 (8 tablas) y V2 aplicadas en Supabase (PostgreSQL 17). V3 (ajustes de revisión) probada en PostgreSQL 17 local; se aplica en Supabase al arrancar la API tras fusionar. Entidades JPA se crean con cada funcionalidad |
| Despliegue de prueba en Vercel | Hecho | Proyecto `mi-primer-empleo-ti` (framework Services, región `pdx1`), conectado al repo: cada push despliega (`main` → producción). Front, API y Supabase funcionando en https://mi-primer-empleo-ti.vercel.app. Arranque en frío ~9 s, luego < 0,5 s |
| Autenticación (HU-01, HU-06) | Pendiente | Semana 2 |

## Preguntas abiertas

- [x] Librería de estado y peticiones en React → TanStack Query (2026-10-23).
- [ ] Aprobar la propuesta de `docs/contrato-api.md` (backend): autenticación de la fase 1, registro separado por rol (`/api/auth/registro/candidato` y `/empresa`, en vez del único `/api/auth/registro` de la fundamentación) y los tres endpoints que faltaban.
- [x] ¿Usar Lombok y MapStruct? → MapStruct sí, Lombok no (2026-10-23).
- [ ] ¿El tutor exige pruebas de integración además de las unitarias? Si sí, agregar Testcontainers (requiere Docker).
- [ ] Fecha exacta de entrega y sustentación.

## Plugins externos

Se instalan desde el directorio de plugins de Anthropic **a medida que se necesitan**, no todos al inicio.
`CLAUDE.md` manda sobre cualquier plugin: si uno sugiere algo contrario (otra arquitectura, Supabase Auth, otro formato de errores), se sigue `CLAUDE.md`.

| Plugin | Cuándo | Para qué | Estado |
| --- | --- | --- | --- |
| Java Backend Skills | Semana 1 | Spring Boot, Data, Security, Web y pruebas | Pendiente de instalar |
| supabase | Semana 1 | PostgreSQL y consultar el esquema (sin su autenticación) | Pendiente de instalar |
| code-review | Semana 3 | Revisión de pull requests | Pendiente de instalar |
| postman | Semana 3 | Colección y pruebas de la API | Pendiente de instalar |
| frontend-design | Semana 6 | Pulir la interfaz | Pendiente de instalar |

Notas de uso:
- **supabase**: trae skills y herramientas de Auth, Storage, Edge Functions y RLS que este proyecto tiene prohibidas. Usarlo solo para consultar el esquema y buenas prácticas de PostgreSQL; si se conecta su MCP, en modo **Read-only**: el esquema solo cambia con migraciones de Flyway.
- **code-review**: Claude Code ya incluye `/code-review` (se usó para revisar el PR #1); el plugin agrega la revisión de PR con varios agentes.

## Equipo

| Persona | Rol principal |
| --- | --- |
| Persona A | Backend (dominio, casos de uso, seguridad, despliegue del backend) |
| Persona B | Frontend (pantallas, cliente de la API, despliegue del frontend) |
