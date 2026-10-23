# CONTEXT.md — Contexto vivo del proyecto

Este archivo responde *qué* estamos construyendo, *por qué* y *en qué punto vamos*.
Las reglas de *cómo* trabajar están en `CLAUDE.md`. Actualízalo al cerrar cada funcionalidad o decisión.

Última actualización: 2026-10-23 · Semana 0 (planeación)

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
| Skills y agentes de Claude Code | En curso | Hechos: skill `nueva-funcionalidad-hexagonal`, agente `revisor-arquitectura`. Faltan según `docs/prompts-claude-code.md` |
| Proyecto Spring Boot | Pendiente | Semana 1 |
| Proyecto React | Pendiente | Semana 1 |
| Esquema y migraciones | Pendiente | Semana 1 |
| Autenticación (HU-01, HU-06) | Pendiente | Semana 2 |

## Preguntas abiertas

- [ ] Librería de estado y peticiones en React (React Query o fetch con Context).
- [ ] ¿Usar Lombok y MapStruct?
- [ ] ¿El tutor exige pruebas de integración además de las unitarias?
- [ ] Fecha exacta de entrega y sustentación.

## Equipo

| Persona | Rol principal |
| --- | --- |
| Persona A | Backend (dominio, casos de uso, seguridad, despliegue del backend) |
| Persona B | Frontend (pantallas, cliente de la API, despliegue del frontend) |
