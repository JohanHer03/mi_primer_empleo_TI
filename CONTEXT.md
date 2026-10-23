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
| Proyecto Spring Boot | Hecho | Paquetes hexagonales, errores uniformes, `SecurityConfig` sin estado (sin JWT aún), CORS, Swagger. Imagen Docker sin probar |
| Proyecto React | Pendiente | Semana 1 |
| Esquema y migraciones | Hecho | V1 (8 tablas) y V2 aplicadas en Supabase (PostgreSQL 17). Entidades JPA se crean con cada funcionalidad |
| Despliegue de prueba en Vercel | Pendiente | Semana 1: configurar variables en Vercel y desplegar el backend |
| Autenticación (HU-01, HU-06) | Pendiente | Semana 2 |

## Preguntas abiertas

- [ ] Librería de estado y peticiones en React (React Query o fetch con Context).
- [x] ¿Usar Lombok y MapStruct? → MapStruct sí, Lombok no (2026-10-23).
- [ ] ¿El tutor exige pruebas de integración además de las unitarias? Si sí, agregar Testcontainers (requiere Docker).
- [ ] Fecha exacta de entrega y sustentación.

## Equipo

| Persona | Rol principal |
| --- | --- |
| Persona A | Backend (dominio, casos de uso, seguridad, despliegue del backend) |
| Persona B | Frontend (pantallas, cliente de la API, despliegue del frontend) |
