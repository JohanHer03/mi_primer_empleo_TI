# CONTEXT.md — Contexto vivo del proyecto

Este archivo responde *qué* estamos construyendo, *por qué* y *en qué punto vamos*.
Las reglas de *cómo* trabajar están en `CLAUDE.md`. Actualízalo al cerrar cada funcionalidad o decisión.

Última actualización: 2026-11-17 · Semanas 4 y 5 completas: MVP del candidato desplegado y probado en producción; semana 6 en curso: HU-01 a HU-10 implementadas en backend y frontend (versión candidata); pulido en curso

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
| 2026-11-18 | Los datos de demostración se cargan con un script de Node por la API (no con una migración); las contraseñas van en variables de entorno que pone el usuario | No deja hashes de contraseñas conocidas en el repo público ni mezcla datos de prueba con el esquema; el script se puede volver a correr sin duplicar |
| 2026-11-18 | El backend local y producción usan la misma base de Supabase | Hallazgo: lo que se crea al probar en local (cuentas `@example.com`) se ve en producción. Para probar algo que escribe muchos datos se usa un PostgreSQL desechable en Docker |
| 2026-11-18 | Las páginas de cada rol se cargan con `lazy` de React Router; las públicas (landing, login, registro) van en el bundle inicial | Quien entra a la landing no descarga las pantallas de empresa ni de administrador; la primera visita sigue siendo inmediata |
| 2026-11-18 | La pantalla de postulantes cruza en el frontend las habilidades de hoy del candidato con las de la vacante; las transiciones de la regla 5 están también en `formato.ts` (`SIGUIENTES_ESTADOS`) para mostrar solo los botones permitidos | El backend sigue siendo quien decide (409); el frontend evita ofrecer acciones que fallarían |
| 2026-11-18 | `IndicadorCompatibilidad` pasa a `components/` | Lo usan el candidato y la empresa |
| 2026-11-17 | Los postulantes se ordenan en la base (`compatibilidad desc, fecha asc, id`) y sus perfiles se cargan con una consulta por página (`buscarConCuentaPorIds`) y otra al catálogo | La compatibilidad guardada sí es columna (a diferencia de la búsqueda), así que se pagina en la base sin cargar todas las postulaciones |
| 2026-11-17 | Los estados de la postulación avanzan de uno en uno (`ENVIADA → EN_REVISION → ENTREVISTA → CONTRATADO`, `RECHAZADA` desde los tres primeros) y pedir el mismo estado responde 409 | Es la lectura estricta de la regla 5; un doble clic no pasa como éxito silencioso y el historial de la postulación es coherente |
| 2026-11-17 | La empresa ve el correo y el perfil completo del candidato solo en los postulantes de sus vacantes | Es el medio de contacto del MVP (sin notificaciones) y el candidato decidió compartirlo al postularse |
| 2026-11-16 | El detalle de la vacante pide en paralelo `GET /api/vacantes/{id}` y la compatibilidad, y esta última va bajo la clave de la búsqueda en TanStack Query; "Postularme" es un hook compartido (`usePostularse`) | Al cambiar habilidades o postularse se recalcula también el detalle, y la búsqueda y el detalle confirman y manejan el 409 igual |
| 2026-11-16 | El detalle de compatibilidad lo arma `CalculadoraCompatibilidad.detallar` (en `domain/service`) y el modelo `CompatibilidadDetalle` es un record sin lógica; la unión con el catálogo pasa a `HabilidadDeVacante.unirConCatalogo` (la usan también `VacanteDetalle`) | `domain/model` no depende de `domain/service`: evita un ciclo entre paquetes del dominio |
| 2026-11-16 | La compatibilidad del detalle (HU-04) devuelve todas las habilidades de la vacante con `cumple` y el nivel del candidato, no solo las faltantes, y sirve también para vacantes cerradas | La pantalla muestra lo que cumple y lo que le falta; desde "Mis postulaciones" se puede abrir una vacante ya cerrada y comparar la compatibilidad de hoy con la guardada |
| 2026-11-16 | Postularse pide confirmación y, ante un 409, la tarjeta muestra el mensaje y quita el botón sin recargar la búsqueda | La postulación no se puede retirar (contrato 5.5); si se recargara, una vacante que cerró desaparecería junto con el mensaje |
| 2026-11-16 | Estilos de páginas (`components/Paginas.module.css`), `Paginacion`, `OpcionesHabilidades` y `useCatalogoHabilidades` pasan a ser compartidos entre empresa y candidato | Las pantallas del candidato repiten tarjetas, filtros, paginación y el selector del catálogo |
| 2026-11-16 | El modo simulado copia `CalculadoraCompatibilidad` en `mocks/compatibilidad.ts` | La búsqueda simulada ordena y muestra porcentajes reales; con el backend la compatibilidad siempre la calcula la API |
| 2026-11-16 | Al postularse, primero se comprueba la regla 1 (409 "Ya te postulaste") y luego la 2 (409 "ya no recibe postulaciones"); una vacante abierta pero vencida tampoco recibe postulaciones | Si ya se postuló a una vacante que luego cerró, el mensaje más útil es que ya se postuló. `uq_postulaciones_candidato_vacante` respalda la regla 1 con el mismo 409 |
| 2026-11-16 | "Mis postulaciones" sale de una consulta JPQL que une postulaciones, vacantes y empresas en una sola fila (`PostulacionConVacanteFila`) | Evita cargar cada vacante con sus habilidades solo para mostrar el título |
| 2026-11-16 | Supabase pausó el proyecto gratuito tras unos 11 días sin actividad: el backend no arrancaba (`tenant/user ... not found`) y la API de producción respondía 500; se restauró desde el panel de Supabase | Antes de la entrega y la sustentación, hacer una petición a la API unos días antes y el mismo día |
| 2026-11-16 | La búsqueda filtra en la base con una `Specification` (Criteria) que trae las habilidades con fetch join; el filtro por habilidad es una subconsulta | Los filtros son opcionales (un parámetro null en `is null` falla en PostgreSQL) y filtrar sobre el fetch join recortaría las habilidades con las que se calcula la compatibilidad |
| 2026-11-16 | `PostulacionEntity` llega con la búsqueda, solo para saber a qué vacantes se postuló el candidato | `postulado` del contrato 5.4; el PR de postulaciones le suma crear y listar |
| 2026-11-04 | `CalculadoraCompatibilidad` es una clase estática del dominio y suma los puntajes multiplicados por 6 (mcm de los niveles) en enteros; el porcentaje es `BigDecimal` con un decimal (`HALF_UP`) | Sin estado ni dependencias; la suma es exacta y el redondeo no depende de errores de coma flotante. Se guarda en `postulaciones.compatibilidad` (`NUMERIC(5,2)`) |
| 2026-11-04 | Las habilidades del candidato son parte del modelo `Candidato` (lista validada en el dominio) y un `@ElementCollection` sobre `candidato_habilidades` | Igual que las de la vacante; `CalculadoraCompatibilidad` las recibirá junto con el candidato |
| 2026-11-04 | `ManejadorGlobalErrores` formatea también la validación de parámetros (`HandlerMethodValidationException`) como `"campo[i].subcampo: mensaje"` | El cuerpo de `PUT /api/candidatos/me/habilidades` es un arreglo: Spring lo valida con su validación de métodos, no con `MethodArgumentNotValidException` |
| 2026-11-04 | El cliente HTTP lee el token de `obtenerTokenActual()` desde el inicio; `AuthProvider` solo registra el cierre de sesión ante un 401 | Al recargar una página protegida, sus consultas salían sin token (401): React ejecuta los efectos de los hijos antes que el de `AuthProvider` |
| 2026-11-04 | Filtro y página de "Mis vacantes" en la URL (`?estado=&pagina=`); una vacante abierta con la fecha de cierre pasada se muestra "Vencida" | Se conservan al recargar o volver del formulario; el contrato 4.4 deja al frontend marcar las vencidas |
| 2026-11-04 | Las habilidades de la vacante son un `@ElementCollection` de `VacanteEntity` sobre `vacante_habilidades` (sin entidad propia) | Se guardan, cargan y reemplazan con la vacante: editar borra y vuelve a insertar la lista, como pide el contrato 4.4 |
| 2026-11-04 | Un `Clock` (bean de `RelojConfig`, UTC) se inyecta en los casos de uso; el dominio convierte a `America/Bogota` para las fechas "vigentes" | Las pruebas fijan el día; la fecha de cierre se compara con el día en Colombia, igual que `ck_vacantes_fecha_cierre` |
| 2026-11-04 | Nuevas excepciones de dominio: `AccesoDenegadoException` (403 con mensaje, reglas 3 y 4) y `OperacionNoPermitidaException` (409, editar una vacante cerrada) | `RecursoDuplicadoException` es solo para duplicados; el 403 de Spring Security no lleva mensaje propio |
| 2026-11-04 | `totalPostulaciones` sale de una consulta nativa agrupada por página (`contarPostulaciones`) | Las postulaciones aún no tienen entidad JPA (semana 4); con ella puede pasar a JPQL |
| 2026-10-27 | Paginación con `SolicitudPagina` y `Pagina<T>` en el dominio y `PaginaDto<T>` en la web; `size` fuera de 1 a 50 responde 400 | El dominio no depende de la `Page` de Spring Data y el error sale con el formato uniforme |
| 2026-10-27 | Para actualizar, el puerto tiene `actualizar(...)`: el adaptador carga la entidad con `findById` y MapStruct le copia los cambios (`@MappingTarget`) | Evita la clave duplicada de una entidad nueva (`Persistable`), como anotó el revisor |
| 2026-10-27 | El correo de la empresa sale de una consulta JPQL que une `empresas` y `usuarios` por `usuario_id` (sin relación JPA) | Las entidades siguen sin relaciones; el listado con y sin filtro son dos consultas porque un parámetro null en `is null` falla en PostgreSQL |
| 2026-10-27 | El catálogo de habilidades se lista completo, sin paginar (excepción a `CLAUDE.md`) y ordenado por categoría (orden del enum) y nombre en español | Alimenta los selectores de los formularios y tiene decenas de elementos (contrato 4.1) |
| 2026-10-27 | La colección de Postman no crea habilidades (no hay caso 201) | Cada ejecución agregaría una habilidad de prueba al catálogo real que ven los usuarios |
| 2026-10-27 | Contrato de la API acordado: secciones 1 a 3 de `docs/contrato-api.md`; el registro va por rol (`/api/auth/registro/candidato` y `/empresa`) | Lo implementado en la semana 2 ya lo sigue; cambios posteriores, con un PR que toque el contrato |
| 2026-10-27 | `server.forward-headers-strategy: framework`: Spring toma el host y el protocolo de `X-Forwarded-Host` y `X-Forwarded-Proto` | Detrás del proxy de Vercel, Spring veía un host interno y rechazaba con 403 "Invalid CORS request" el `Origin` del propio dominio (el login desde el navegador fallaba aunque curl funcionaba) |
| 2026-10-27 | Contraseña nueva: mínimo 8 caracteres y máximo 72 **bytes** (`PoliticaContrasena`, en el dominio) | BCrypt 6.5 lanza excepción con más de 72 bytes: 40 letras con tilde pasaban el `@Size` del DTO y daban 500 |
| 2026-10-27 | Los puertos de salida tienen `crear(...)`, no `guardar(...)`; para editar habrá que cargar la entidad con `findById` | Una entidad que sale del mapper se marca como nueva (`Persistable`) y al actualizar daría clave duplicada |
| 2026-10-27 | Los UUID los genera el dominio (`Usuario.nuevo`, ...); las entidades heredan de `EntidadConIdAsignado` (`Persistable`) | El caso de uso arma usuario y perfil completos antes de guardar, y Spring Data hace un solo INSERT sin el SELECT previo |
| 2026-10-27 | El correo se guarda en minúsculas y se busca con `lower(email) = lower(:email)` en `@Query` | Usa la misma expresión del índice único de V3 (`findByEmailIgnoreCase` generaría `upper()` y no usaría el índice) |
| 2026-10-27 | Registro: primero reglas del dominio (400), luego duplicados (409); un `DataIntegrityViolationException` con `uq_usuarios_email` o `uq_empresas_nit` también responde 409 | Respuestas coherentes, incluso si dos registros iguales llegan a la vez |
| 2026-10-27 | Login: correo inexistente y contraseña errónea dan el mismo 401, y con correo inexistente igual se cifra la contraseña; el 403 de cuenta desactivada solo sale con la contraseña correcta | No revelar qué correos están registrados, ni por el mensaje ni por el tiempo de respuesta |
| 2026-10-27 | Colección de Postman en `/postman` con entornos `local` y `produccion`; cada ejecución crea cuentas nuevas `@example.com` | Se puede correr completa con el Collection Runner o `npx newman run` sin limpiar la base |
| 2026-10-27 | JWT con jjwt 0.13.0 (`jjwt-impl` y `jjwt-jackson` solo en ejecución), firmado con HS256 y `JWT_SECRET` de al menos 32 bytes | Librería más documentada; encaja con `JwtService` y `JwtAuthFilter` de `CLAUDE.md`. El servicio falla al crearse si el secreto es corto |
| 2026-10-27 | `JwtAuthFilter` se crea dentro de `SecurityConfig` (no es `@Component`) y deja como principal un `UsuarioAutenticado` (id, email, rol) con la autoridad `ROLE_<rol>` | Evita que Spring Boot lo registre dos veces; los controladores leen el usuario con `@AuthenticationPrincipal` y el rol se exige con `@PreAuthorize("hasRole('...')")` |
| 2026-10-27 | El administrador inicial lo crea un `ApplicationRunner` desde `ADMIN_EMAIL` y `ADMIN_PASSWORD` si no existe | El hash se calcula al arrancar: ningún secreto queda en las migraciones del repo público |
| 2026-10-27 | Una cuenta desactivada conserva su token hasta que vence (120 min) | El JWT no guarda estado; aceptable para el MVP. El login sí rechaza cuentas inactivas (403) |
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
| Skills y agentes de Claude Code | En curso | Hechos: skills `nueva-funcionalidad-hexagonal`, `migracion-flyway`, `pantalla-react` y `endpoint-postman`, agente `revisor-arquitectura`. Faltan los agentes `escritor-pruebas` y `revisor-seguridad` (semanas 4 y 5) |
| Proyecto Spring Boot | Hecho | Paquetes hexagonales, errores uniformes, `SecurityConfig` sin estado (sin JWT aún), CORS, Swagger. Imagen `Dockerfile.vercel` probada con `docker compose up --build backend` (responde 401 en JSON) |
| Proyecto React (fase 0 de `docs/plan-frontend.md`) | Hecho | Vite + React 19 + TS, cliente único de la API, sesión con JWT, rutas y layouts por rol, 14 pantallas marcador, login simulado. Probado con `docker compose` junto al backend |
| Landing pública en `/` | Hecho | Problema con cifras del DANE, diferencial, cómo funciona, empresas y ODS 8, según `docs/fundamentacion.md`. Con sesión, `/` sigue llevando al inicio del rol |
| Frontend fase 1: registro y login (HU-01, HU-06) | Hecho | Formularios de candidato y empresa según `docs/contrato-api.md`, conectados al backend real; probado en producción (login de ADMIN → `/admin/habilidades`) |
| Catálogo de habilidades (HU-10, contrato 4.1) | Hecho | PR #17: `GET /api/habilidades` (autenticado, filtro por categoría) y `POST /api/habilidades` (ADMIN, 409 si se repite) |
| Perfil y verificación de empresas (HU-06, HU-10; contrato 4.2 y 4.3) | Hecho | PR #18: `GET` y `PUT /api/empresas/me` (EMPRESA), `GET /api/empresas?verificada=` paginado y `PATCH /api/empresas/{id}/verificar` (ADMIN). Primera paginación (`PaginaDto`). Probado contra Supabase y con la carpeta Empresas de Postman |
| CRUD de vacantes (HU-07; contrato 3.2 y 4.4) | Hecho (PR #19) | `POST`, `PUT` y `PATCH /api/vacantes/{id}/cerrar` (EMPRESA dueña), `GET /api/vacantes/{id}` (autenticado) y `GET /api/empresas/me/vacantes` paginado con `totalPostulaciones`. Reglas 3 y 4 en `VacanteService`. 160 pruebas; probado contra Supabase con la carpeta Vacantes de Postman (44 peticiones, 85 aserciones) |
| Frontend fase 2: empresa (HU-06, HU-07) | Hecho (PR #20) | Inicio, perfil (aviso si no está verificada), mis vacantes (filtro, paginación, vencidas, cerrar) y formulario de publicar y editar con habilidades del catálogo. Mocks de empresas, habilidades y vacantes. Probado en modo simulado y contra el backend local |
| Contrato de la fase 3: candidato y postulaciones (semana 4) | Acordado (PR #21) | Sección 5 de `docs/contrato-api.md`: perfil y habilidades del candidato, `CalculadoraCompatibilidad` adelantada, búsqueda solo para CANDIDATO, postularse y mis postulaciones |
| Perfil y habilidades del candidato (HU-02; contrato 5.1 y 5.2) | Hecho (PR #22) | `GET` y `PUT /api/candidatos/me` y `PUT /api/candidatos/me/habilidades` (reemplaza la lista, 0 a 30). 182 pruebas; probado contra Supabase con la carpeta Candidatos de Postman |
| `CalculadoraCompatibilidad` (contrato 5.3) | Hecho (PR #23) | En `domain/service`, Java puro: porcentaje con un decimal y lista de faltantes. 8 pruebas, incluido el ejemplo del contrato (60.0) |
| Búsqueda de vacantes (HU-03; contrato 5.4) | Hecho (PR #24) | `GET /api/vacantes` solo para CANDIDATO: filtros opcionales con Criteria, compatibilidad calculada al consultar, orden y paginación en memoria, `postulado`. 204 pruebas; probado contra Supabase con la carpeta Búsqueda de Postman (61 peticiones, 122 aserciones) |
| Postulaciones del candidato (HU-05; contrato 5.5 y 5.6) | Hecho (PR #25) | `POST /api/vacantes/{id}/postulaciones` con las reglas 1, 2 y 6 y `GET /api/postulaciones/me` paginado. `totalPostulaciones` de "Mis vacantes" pasa a JPQL. 218 pruebas; probado contra Supabase con la carpeta Postulaciones de Postman (71 peticiones, 144 aserciones) |
| Frontend fase 3: candidato (HU-02, HU-03, HU-05) | Hecho (PR #26) | Inicio con las vacantes más compatibles, perfil (datos y habilidades con nivel), búsqueda con filtros, orden y paginación en la URL y botón "Postularme", y mis postulaciones por estado. Mocks de candidatos, búsqueda y postulaciones. Probado en modo simulado y contra el backend local |
| Contrato de la fase 4: compatibilidad y "lo que te falta" (semana 5) | Acordado (PR #27) | Sección 6 de `docs/contrato-api.md`: `GET /api/vacantes/{id}/compatibilidad` (CANDIDATO) con el porcentaje actual, todas las habilidades de la vacante con `cumple` y la postulación si existe; la pantalla usa también `GET /api/vacantes/{id}` |
| Compatibilidad y "lo que te falta" (HU-04; contrato 6.1) | Hecho (PR #28) | `GET /api/vacantes/{id}/compatibilidad` (CANDIDATO): porcentaje actual, cada habilidad con el nivel del candidato y `cumple`, y la postulación si existe; también para vacantes cerradas. 232 pruebas; probado contra Supabase con la carpeta Compatibilidad de Postman (4 peticiones, 11 aserciones) |
| Frontend fase 4: detalle con compatibilidad (HU-04) | Hecho (PR #29) | `/candidato/vacantes/:id`: datos de la vacante, porcentaje de hoy, "lo que te falta" y lo que ya cumple, postularse o el estado de la postulación (con la compatibilidad de hoy y la guardada si difieren). Enlazado desde la búsqueda, el inicio y mis postulaciones. Probado en modo simulado, contra el backend local y en producción |
| Prueba de punta a punta en producción (semana 5) | Hecho (2026-11-17) | Deploy del #29 en READY. Con una cuenta de candidato `@example.com` (la contraseña la puso el usuario): habilidades, búsqueda (100 % y 25 %), detalle con "lo que te falta", postularse desde el detalle y mis postulaciones. Registros de Vercel sin advertencias ni errores. Pendiente de pulido: con unos 800 px de ancho el menú del encabezado parte "Mis postulaciones" en dos líneas |
| Contrato de la fase 5: gestión de postulantes y administración (semana 6) | Acordado (PR #31) | Sección 7 de `docs/contrato-api.md`: `GET /api/vacantes/{id}/postulaciones` (EMPRESA dueña, ordenados por la compatibilidad guardada) y `PATCH /api/postulaciones/{id}/estado` con las transiciones de la regla 5; las pantallas del administrador usan los endpoints de 4.1 y 4.3 |
| Gestión de postulantes (HU-08 y HU-09; contrato 7.1 y 7.2) | Hecho (PR #32) | `GET /api/vacantes/{id}/postulaciones` (EMPRESA dueña, por compatibilidad guardada y fecha, con el perfil del candidato) y `PATCH /api/postulaciones/{id}/estado` con la regla 5 en `EstadoPostulacion.siguientes()` y `Postulacion.cambiarEstado`. 270 pruebas; probado contra Supabase con la carpeta Gestión de postulaciones de Postman (13 peticiones, 27 aserciones) |
| Frontend fase 5: postulantes y administración (HU-08, HU-09, HU-10) | Hecho (PR #33) | `/empresa/vacantes/:id/postulantes` (orden por compatibilidad, filtro por estado, perfil con las habilidades frente a la vacante, solo las transiciones permitidas y confirmación al rechazar o contratar), `/admin/habilidades` (catálogo por categoría y crear) y `/admin/empresas` (por verificar, verificadas o todas, paginado). Probado en modo simulado y contra el backend local |
| Frontend fase 6: pulido | En curso (PR #34) | Menú del encabezado sin partir enlaces (fila propia por debajo de 960 px), páginas de cada rol cargadas por ruta (bundle inicial de 587 a 479 kB, sin el aviso de Vite) y las 17 pantallas revisadas a 375 y 800 px sin desbordes. Faltan las pruebas de uso con datos de demostración |
| Datos de demostración | Script listo (PR #35); lo corre el usuario contra producción, después de la limpieza de `scripts/limpieza-pruebas` | `scripts/datos-demo/`: 4 empresas (una sin verificar), 7 vacantes (una cerrada), 6 candidatos y 9 postulaciones en todos los estados, cargados por la API. `--revisar` valida los datos sin conexión. Probado contra un backend local con un PostgreSQL 17 desechable en Docker: carga completa, segunda corrida sin duplicar (4, 7, 6 y 9 filas) y errores claros (variable faltante, otra contraseña, API caída). Ver su README |
| Esquema y migraciones | Hecho | V1 (8 tablas), V2 (cierre de la Data API), V3 (ajustes de revisión) y V4 (nombre de habilidad único sin distinguir mayúsculas y catálogo inicial de 35 habilidades) aplicadas en Supabase (PostgreSQL 17). Las entidades JPA se crean con cada funcionalidad |
| Despliegue de prueba en Vercel | Hecho | Proyecto `mi-primer-empleo-ti` (framework Services, región `pdx1`), conectado al repo: cada push despliega (`main` → producción). Front, API y Supabase funcionando en https://mi-primer-empleo-ti.vercel.app. Arranque en frío ~9 s, luego < 0,5 s |
| Autenticación en el backend (HU-01, HU-06) | Hecho | PR #11 (JWT, filtro y BCrypt), #12 (registro, login y administrador inicial) y #13 (CORS detrás del proxy de Vercel); 80 pruebas en verde. Probado en producción: el administrador se crea desde `ADMIN_EMAIL`/`ADMIN_PASSWORD` y el login lleva a su panel. Colección de Postman en `/postman` |

## Preguntas abiertas

- [x] Librería de estado y peticiones en React → TanStack Query (2026-10-23).
- [x] Aprobar `docs/contrato-api.md`: convenciones, autenticación con registro separado por rol y los tres endpoints que faltaban → Acordado (2026-10-27).
- [x] ¿Usar Lombok y MapStruct? → MapStruct sí, Lombok no (2026-10-23).
- [ ] ¿El tutor exige pruebas de integración además de las unitarias? Si sí, agregar Testcontainers (requiere Docker).
- [ ] Fecha exacta de entrega y sustentación.
- [ ] ¿Proteger el dominio de producción? Hoy es público (la protección estándar de Vercel solo cubre las URL de cada deploy). Recomendación: dejarlo público para que el tutor lo abra sin cuenta de Vercel, sin datos personales reales y con contraseña fuerte del ADMIN.

## Plugins externos

Se instalan desde el directorio de plugins de Anthropic **a medida que se necesitan**, no todos al inicio.
`CLAUDE.md` manda sobre cualquier plugin: si uno sugiere algo contrario (otra arquitectura, Supabase Auth, otro formato de errores), se sigue `CLAUDE.md`.

| Plugin | Cuándo | Para qué | Estado |
| --- | --- | --- | --- |
| Java Backend Skills | Semana 1 | Spring Boot, Data, Security, Web y pruebas | Instalado |
| supabase | Semana 1 | PostgreSQL y consultar el esquema (sin su autenticación) | Instalado; su MCP necesita autorizarse con `/mcp` antes de usarlo |
| code-review | Semana 3 | Revisión de pull requests | Ofrecido para instalar (2026-10-27); confirmar en el panel de plugins |
| postman | Semana 3 | Colección y pruebas de la API | Ofrecido para instalar (2026-10-27); su MCP necesita una API key de Postman solo si se sincroniza con la nube. La colección del repo se prueba con `npx newman` sin cuenta |
| frontend-design | Semana 6 | Pulir la interfaz | Pendiente de instalar |

Notas de uso:
- **supabase**: trae skills y herramientas de Auth, Storage, Edge Functions y RLS que este proyecto tiene prohibidas. Usarlo solo para consultar el esquema y buenas prácticas de PostgreSQL; si se conecta su MCP, en modo **Read-only**: el esquema solo cambia con migraciones de Flyway.
- **code-review**: Claude Code ya incluye `/code-review` (se usó para revisar el PR #1); el plugin agrega la revisión de PR con varios agentes.

## Equipo

| Persona | Rol principal |
| --- | --- |
| Persona A | Backend (dominio, casos de uso, seguridad, despliegue del backend) |
| Persona B | Frontend (pantallas, cliente de la API, despliegue del frontend) |
