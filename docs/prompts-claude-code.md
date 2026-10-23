# Prompts para crear skills y agentes en Claude Code

Cada prompt se pega en Claude Code desde la raíz del repositorio, con el `CLAUDE.md` ya en su lugar.
Claude crea el archivo; ustedes lo revisan, lo ajustan y lo suben al repo para que la pareja trabaje igual.

Orden recomendado:

| Cuándo | Qué crear |
| --- | --- |
| Semana 1 | Skill `nueva-funcionalidad-hexagonal` y agente `revisor-arquitectura` |
| Semana 1–2 | Skill `migracion-flyway` |
| Semana 2 | Skill `pantalla-react` |
| Semana 3 | Skill `endpoint-postman` |
| Semana 4–5 | Agentes `escritor-pruebas` y `revisor-seguridad` |

Recuerden: skill = receta que Claude sigue en la conversación principal (se invoca con `/nombre` o sola cuando aplica).
Agente = otro Claude con su propio contexto y herramientas limitadas, que hace una tarea aparte y devuelve un resultado.

---

## Skills

### 1. nueva-funcionalidad-hexagonal

```text
Crea una skill de proyecto en .claude/skills/nueva-funcionalidad-hexagonal/SKILL.md.

Frontmatter:
- name: nueva-funcionalidad-hexagonal
- description: Implementa una funcionalidad completa del backend siguiendo la arquitectura hexagonal del proyecto. Usar cuando se pida un caso de uso, endpoint o funcionalidad nueva del backend.

Antes de escribirla, lee CLAUDE.md y respeta sus reglas de arquitectura, nombres y convenciones.

La skill debe indicar este procedimiento, en orden, con la ruta de paquete de cada paso:
1. Confirmar con el usuario la historia de usuario (HU-xx), la ruta, el rol y las reglas de negocio involucradas. Si algo no está claro, preguntar antes de escribir código.
2. Modelo de dominio en domain/model (sin anotaciones de persistencia) y reglas o excepciones en domain.
3. Puerto de entrada en application/port/in (interfaz XxxUseCase con un método y su comando o record de entrada).
4. Caso de uso en application/service como @Service, con @Transactional si escribe, inyección por constructor, y las reglas de negocio validadas aquí.
5. Puerto de salida en application/port/out si hace falta persistencia nueva.
6. Adaptador de persistencia en infrastructure/adapter/out/persistence: entidad JPA, repositorio Spring Data, mapper y clase que implementa el puerto.
7. Si cambia el esquema, crear la migración Flyway siguiendo la skill migracion-flyway.
8. Controlador y DTOs (records con Bean Validation) en infrastructure/adapter/in/web; el controlador solo llama al puerto de entrada.
9. Autorización por rol en SecurityConfig o @PreAuthorize.
10. Pruebas unitarias del caso de uso con JUnit 5 y Mockito (camino feliz y cada regla de negocio).
11. Ejecutar ./mvnw test y corregir hasta que pase.
12. Agregar la petición a Postman siguiendo la skill endpoint-postman.
13. Terminar con un resumen: archivos creados, endpoint, códigos de respuesta y qué falta.

Incluye una lista de verificación final (dominio sin JPA, controlador sin repositorios, DTOs en las respuestas, pruebas en verde).
Mantén la skill por debajo de 120 líneas. No crees código de ejemplo de una funcionalidad real.
```

### 2. migracion-flyway

```text
Crea una skill de proyecto en .claude/skills/migracion-flyway/SKILL.md.

Frontmatter:
- name: migracion-flyway
- description: Crea migraciones Flyway para el esquema en Supabase (PostgreSQL). Usar cuando se agregue o modifique una tabla, columna, índice o restricción.

Lee CLAUDE.md antes de escribirla. La skill debe indicar:
- Ubicación backend/src/main/resources/db/migration y nombre V{n}__descripcion_en_snake_case.sql, con n = el siguiente número disponible (revisar los existentes).
- Nunca editar una migración ya aplicada; corregir con una nueva.
- Convenciones: tablas en snake_case y plural, id UUID con DEFAULT gen_random_uuid(), claves foráneas con ON DELETE explícito, enums como VARCHAR con CHECK (no tipos ENUM de Postgres, para que JPA los mapee como String), columnas de auditoría fecha_creacion TIMESTAMPTZ DEFAULT now().
- Restricciones de negocio en la base cuando aplique (por ejemplo UNIQUE (candidato_id, vacante_id) en postulaciones).
- Índices para columnas de búsqueda y claves foráneas.
- Después de crearla: revisar que la entidad JPA coincida (ddl-auto=validate) y ejecutar ./mvnw test.
- Cómo verificar en local con ./mvnw flyway:info.
Mantenla corta (menos de 60 líneas).
```

### 3. pantalla-react

```text
Crea una skill de proyecto en .claude/skills/pantalla-react/SKILL.md.

Frontmatter:
- name: pantalla-react
- description: Crea una pantalla o flujo del frontend React conectado a la API. Usar cuando se pida una página, formulario o vista nueva del frontend.

Lee CLAUDE.md y la estructura actual de frontend/src antes de escribirla, y adapta las rutas a lo que exista.
La skill debe indicar:
1. Confirmar la historia de usuario, el rol que ve la pantalla y los endpoints que usa.
2. Funciones de la API en src/api/ usando el cliente único (agrega Bearer, base ${VITE_API_URL}/api, maneja 401 cerrando sesión).
3. Tipos TypeScript que reflejan los DTOs del backend.
4. La página en src/pages y componentes reutilizables en src/components.
5. Estados obligatorios: carga, vacío, error y éxito.
6. Formularios con validación en el cliente que coincida con la del backend, y mensajes de error del formato { status, error, mensaje, timestamp }.
7. Registrar la ruta y protegerla por rol.
8. Ejecutar npm run lint y npm run build y corregir errores.
9. Resumen final de archivos y cómo probar la pantalla.
Mantenla por debajo de 80 líneas.
```

### 4. endpoint-postman

```text
Crea una skill de proyecto en .claude/skills/endpoint-postman/SKILL.md.

Frontmatter:
- name: endpoint-postman
- description: Agrega o actualiza peticiones en la colección de Postman del proyecto. Usar después de crear o cambiar un endpoint.

Lee CLAUDE.md y los archivos de /postman antes de escribirla.
La skill debe indicar:
- La colección vive en postman/primer-empleo.postman_collection.json (formato v2.1) con entornos local y produccion que definen baseUrl y token.
- Una carpeta por recurso (Auth, Habilidades, Candidatos, Empresas, Vacantes, Postulaciones).
- Cada petición usa {{baseUrl}} y hereda la autorización Bearer {{token}} de la colección; las de /api/auth no llevan token.
- La petición de login guarda el token con pm.environment.set("token", pm.response.json().token).
- Cada petición lleva al menos una prueba de código de estado, y se agregan casos negativos cuando aplique: 400 validación, 401 sin token, 403 rol o propiedad, 404, 409 duplicado.
- Cuerpos de ejemplo con datos realistas de Medellín.
- Editar el JSON sin romper el formato y validar que siga siendo JSON válido.
Mantenla por debajo de 60 líneas.
```

---

## Agentes

Se pueden crear con estos prompts o con el comando `/agents` de Claude Code. Revisen siempre las herramientas que quedan habilitadas.

### 1. revisor-arquitectura

```text
Crea un subagente de proyecto en .claude/agents/revisor-arquitectura.md.

Frontmatter:
- name: revisor-arquitectura
- description: Revisa que el backend respete la arquitectura hexagonal del proyecto. Usar antes de abrir un pull request o cuando se pida revisar la arquitectura.
- tools: Read, Grep, Glob (solo lectura; no debe editar archivos)

Instrucciones del agente (léelas de CLAUDE.md y resúmelas):
- Revisar solo los archivos que cambiaron (git diff contra main) salvo que se pida todo el proyecto.
- Verificar: domain sin imports de jakarta.persistence ni anotaciones JPA; controladores que solo usan puertos de entrada; casos de uso que dependen de puertos y no de repositorios Spring Data; respuestas HTTP con DTOs, nunca entidades ni modelos de dominio; inyección por constructor; reglas de negocio en el caso de uso y no en el controlador.
- Formato de respuesta: lista de hallazgos con archivo:línea, la regla incumplida y la corrección sugerida, ordenados de más a menos grave. Si no hay hallazgos, decirlo en una línea.
- No inventar problemas: cada hallazgo debe citar el código.
```

### 2. escritor-pruebas

```text
Crea un subagente de proyecto en .claude/agents/escritor-pruebas.md.

Frontmatter:
- name: escritor-pruebas
- description: Escribe y ejecuta pruebas unitarias JUnit 5 y Mockito para el dominio y los casos de uso. Usar cuando se termine un caso de uso o se pidan pruebas.
- tools: Read, Grep, Glob, Write, Edit, Bash

Instrucciones del agente (basadas en CLAUDE.md):
- Escribir pruebas solo en backend/src/test, sin modificar código de producción. Si una prueba revela un error, reportarlo en vez de cambiar el código.
- Prioridad: CalculadoraCompatibilidad (casos con habilidades faltantes, niveles iguales, obligatorias vs deseables, vacante sin habilidades) y las 6 reglas de negocio de CLAUDE.md.
- Casos de uso con Mockito sobre los puertos de salida; nada de base de datos real en pruebas unitarias.
- Nombres de prueba descriptivos en español (por ejemplo: deberiaRechazarPostulacionDuplicada).
- Ejecutar ./mvnw test al final.
- Responder con: pruebas creadas, resultado de la ejecución y errores encontrados en el código de producción.
```

### 3. revisor-seguridad

```text
Crea un subagente de proyecto en .claude/agents/revisor-seguridad.md.

Frontmatter:
- name: revisor-seguridad
- description: Revisa autenticación, autorización y manejo de secretos del proyecto. Usar antes de un despliegue o cuando se cambie seguridad, endpoints o configuración.
- tools: Read, Grep, Glob (solo lectura)

Instrucciones del agente (basadas en CLAUDE.md y la tabla de endpoints de docs/fundamentacion.md):
- Comparar cada endpoint con el rol que debe tener; señalar rutas abiertas que no deberían serlo.
- Verificar reglas de propiedad en los casos de uso (empresa solo sus vacantes y postulaciones; candidato solo sus postulaciones).
- Revisar JwtService y JwtAuthFilter: expiración, validación de firma, clave desde variable de entorno.
- Buscar secretos escritos en código, application.yml, Dockerfile.vercel, docker-compose.yml o .env versionados.
- Revisar CORS, sesión STATELESS, contraseñas con BCrypt y que las respuestas no expongan passwordHash.
- Formato: hallazgos con archivo:línea, riesgo (alto, medio, bajo) y corrección. Sin hallazgos inventados.
```
