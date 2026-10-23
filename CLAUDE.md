# CLAUDE.md — Plataforma de primer empleo TI (Medellín)

Proyecto de fin de semestre de Ingeniería Informática (en pareja, 7 semanas).
Plataforma web que conecta estudiantes de últimos semestres y recién graduados de software
en Medellín con vacantes de práctica y nivel junior, calculando su compatibilidad por
habilidades y mostrando qué les falta para cada vacante. Alineado con ODS 8 (metas 8.6 y 8.b).

Antes de empezar una tarea, lee `CONTEXT.md`: tiene el glosario, las decisiones tomadas y el estado actual del proyecto.
Fundamentación completa (datos, historias de usuario, endpoints, plan): ver `docs/fundamentacion.md`.

## Estructura del repositorio

```
/backend    Spring Boot 3 + Java 21 (Maven), arquitectura hexagonal
/frontend   React + Vite + TypeScript
/postman    Colección y entornos exportados
/docs       Fundamentación y diagramas
vercel.json         Un proyecto de Vercel con dos servicios en contenedor (backend y frontend)
docker-compose.yml  Las mismas imágenes, para probar en local
```

## Comandos

```bash
# Backend (desde /backend)
./mvnw spring-boot:run          # levantar API en http://localhost:8080
./mvnw test                     # pruebas unitarias e integración
./mvnw flyway:info              # estado de migraciones

# Frontend (desde /frontend)
npm run dev                     # http://localhost:5173
npm run build
npm run lint

# Contenedores (desde la raíz, requiere Docker y un archivo .env basado en .env.example)
docker compose up --build       # front en http://localhost:5173, API en http://localhost:8080
vercel dev                      # simula Vercel en local (requiere Docker)
vercel deploy                   # despliegue de vista previa
```

Variables de entorno del backend: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`.
Variable del frontend: `VITE_API_URL` = origen del backend sin `/api` (vacío en Vercel, `http://localhost:8080` en local); el cliente llama a `${VITE_API_URL}/api/...`.
Nunca escribir valores reales en el código ni en este archivo.

## Despliegue (Vercel + Docker)

- Un solo proyecto de Vercel. Cada servicio tiene su `Dockerfile.vercel` y corre como Vercel Function.
- `vercel.json` envía `/api/*` al backend y todo lo demás al frontend: comparten dominio, así que en producción no hay CORS.
- El contenedor debe escuchar en `PORT` (80 por defecto en Vercel) y en `0.0.0.0`. El backend usa `server.port=${PORT:8080}`.
- Los contenedores no guardan estado: nada en disco local, todo en Supabase.
- Las instancias se apagan tras 5 minutos sin tráfico: la primera petición tarda (arranque en frío de la JVM). No subir el pool de Hikari por encima de 3 conexiones por instancia.
- Los secretos se configuran en Project Settings > Environment Variables de Vercel, nunca en los Dockerfile.

## Arquitectura hexagonal (reglas obligatorias)

Paquete base: `com.primerempleo`

Todo el backend es Java con Spring Boot. La arquitectura hexagonal define qué parte de Spring usa cada capa:

- `domain/` — modelos, enums, reglas de negocio y `CalculadoraCompatibilidad`.
  Sin anotaciones de persistencia (`@Entity`, `@Table`, `jakarta.persistence`): esas solo van en el adaptador de salida.
- `application/port/in/` — interfaces de casos de uso (`PostularseUseCase`, ...).
- `application/port/out/` — interfaces de persistencia (`VacanteRepositoryPort`, ...).
- `application/service/` — implementaciones de casos de uso como `@Service` de Spring, con `@Transactional` donde aplique; dependen de puertos y dominio, nunca de repositorios JPA directamente.
- `infrastructure/adapter/in/web/` — controladores REST, DTOs (records) y mappers.
- `infrastructure/adapter/out/persistence/` — entidades JPA, repositorios Spring Data y adaptadores que implementan los puertos de salida.
- `infrastructure/security/` — `SecurityConfig`, `JwtService`, `JwtAuthFilter`.
- `infrastructure/config/` — CORS, manejo global de errores (`@RestControllerAdvice`) y configuración general.

Reglas:
- Los controladores solo llaman puertos de entrada. Nunca repositorios.
- Las entidades JPA son distintas de los modelos de dominio; un mapper traduce entre ambas.
- Los controladores nunca devuelven entidades JPA ni modelos de dominio: siempre DTOs.
- La inyección de dependencias es de Spring, siempre por constructor (sin `@Autowired` en atributos).

## Dominio

Entidades: Usuario, Candidato, Empresa, Habilidad, Vacante, Postulacion, CandidatoHabilidad, VacanteHabilidad.
Roles: `CANDIDATO`, `EMPRESA`, `ADMIN`. IDs: UUID.

Reglas de negocio (se validan en el caso de uso, no solo en el controlador):
1. Un candidato no puede postularse dos veces a la misma vacante (409).
2. Solo se postula a vacantes `ABIERTA` con fecha de cierre vigente.
3. Solo empresas verificadas publican vacantes.
4. Una empresa solo ve y modifica sus propias vacantes y postulaciones (403 si no).
5. Estados de postulación: `ENVIADA → EN_REVISION → ENTREVISTA → CONTRATADO`; `RECHAZADA` desde cualquiera antes de `CONTRATADO`.
6. La compatibilidad se calcula al postularse y se guarda.

Compatibilidad: niveles BASICO=1, INTERMEDIO=2, AVANZADO=3. Por cada habilidad de la vacante,
`min(1, nivelCandidato / nivelRequerido)`, con peso 2 si es obligatoria y 1 si es deseable.
Resultado = suma ponderada / suma de pesos × 100. Las habilidades con nivel insuficiente
forman la lista "lo que te falta".

## Base de datos

- Supabase se usa **solo como base de datos PostgreSQL**, conectada desde Spring Boot por JDBC.
- Prohibido usar: Supabase Auth (registro, login, sesiones, JWT de Supabase), `supabase-js` o cualquier cliente de Supabase en el frontend, Row Level Security como mecanismo de autorización, Storage, Edge Functions y Realtime.
- El frontend nunca habla con Supabase: solo con la API del backend.
- La autenticación y la autorización son responsabilidad exclusiva de Spring Security (requisito académico del proyecto).
- Esquema versionado con Flyway en `backend/src/main/resources/db/migration`, nombres `V{n}__descripcion.sql`.
- Nunca modificar una migración ya aplicada: crear una nueva.
- `spring.jpa.hibernate.ddl-auto=validate` (Flyway manda sobre el esquema).

## Seguridad

- JWT sin estado, contraseñas con BCrypt, sesión `STATELESS`.
- Rutas públicas: solo `/api/auth/**` y la documentación de la API.
- Autorización por rol en `SecurityConfig` y `@PreAuthorize`; propiedad del recurso en el caso de uso.
- CORS limitado a `CORS_ORIGINS` (solo se necesita en local; en Vercel front y back comparten dominio).

## API

- Prefijo `/api`, recursos en plural y español (`/api/vacantes`, `/api/postulaciones`).
- Validación con Bean Validation en los DTOs de entrada.
- Errores con formato uniforme: `{ "status", "error", "mensaje", "timestamp" }`.
- Listados paginados (`page`, `size`).
- Cada endpoint nuevo se agrega a la colección de Postman con al menos una prueba de código de estado.

## Frontend

- React + TypeScript, componentes funcionales y hooks.
- Toda llamada HTTP pasa por un cliente único en `src/api/` que agrega el token `Bearer`.
- Rutas protegidas por rol; si el backend responde 401, cerrar sesión y redirigir a login.
- Cada pantalla maneja estados de carga, vacío y error.

## Pruebas

- JUnit 5 + Mockito.
- Dominio y casos de uso con pruebas unitarias (prioridad: `CalculadoraCompatibilidad` y reglas de postulación).
- Antes de dar por terminada una funcionalidad: `./mvnw test` debe pasar.

## Convenciones de código

- Código, clases y variables en español, salvo términos técnicos (`Controller`, `Repository`, `UseCase`, `Port`, `Dto`, `Mapper`).
- DTOs como `record`.
- Commits en español con prefijo: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`.
- Una rama por funcionalidad y pull request revisado por la otra persona.

## Plugins y skills

Este archivo tiene prioridad sobre cualquier skill o plugin instalado. Si una skill sugiere algo que contradice estas reglas (otra arquitectura, Supabase Auth, otro formato de errores), sigue este archivo y avisa del conflicto.

- Plugins externos: `Java Backend Skills`, `supabase` (solo para PostgreSQL y consultar el esquema), `code-review`, `postman`, `frontend-design`.
- Skills del proyecto en `.claude/skills/`: `nueva-funcionalidad-hexagonal`, `pantalla-react`, `migracion-flyway`, `endpoint-postman`.
- Agentes del proyecto en `.claude/agents/`: `revisor-arquitectura`, `escritor-pruebas`, `revisor-seguridad`.

## Forma de trabajar

- Para una funcionalidad nueva seguir este orden: modelo de dominio → puerto de entrada → caso de uso → puerto de salida → adaptador de persistencia → controlador y DTO → pruebas → Postman.
- Antes de cambios grandes, proponer un plan corto y esperar aprobación.
- No agregar dependencias nuevas sin consultarlo.
- Fuera del alcance del MVP (no implementar salvo que se pida): notificaciones por correo, carga de PDF, estadísticas.
- Al terminar una funcionalidad o tomar una decisión técnica, actualiza `CONTEXT.md` (estado y registro de decisiones).
