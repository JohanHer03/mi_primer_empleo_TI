---
name: nueva-funcionalidad-hexagonal
description: Implementa una funcionalidad completa del backend siguiendo la arquitectura hexagonal del proyecto. Usar cuando se pida un caso de uso, endpoint o funcionalidad nueva del backend.
---

# Nueva funcionalidad hexagonal

Receta para implementar una funcionalidad del backend de punta a punta.
`CLAUDE.md` manda sobre esta skill: si algo choca, sigue `CLAUDE.md` y avisa del conflicto.
Lee también `CONTEXT.md` (glosario y decisiones) antes de empezar.

Paquete base: `com.primerempleo`. Las rutas de abajo son relativas a `backend/src/main/java/com/primerempleo/`.

## Procedimiento

Sigue los pasos en orden. No saltes al controlador antes de tener dominio y caso de uso.

1. **Confirmar el alcance.** Con el usuario: historia de usuario (HU-xx de `docs/fundamentacion.md`),
   método y ruta, rol que la usa y reglas de negocio involucradas (las 6 de `CLAUDE.md`).
   Si algo no está claro, pregunta antes de escribir código. Si el cambio es grande, propone un plan corto y espera aprobación.

2. **Dominio** — `domain/model/` y `domain/`.
   - Modelos y enums como clases Java sin anotaciones de persistencia (nada de `jakarta.persistence`).
   - Reglas que dependen solo del modelo, como métodos del dominio.
   - Excepciones de negocio en `domain/exception/` (por ejemplo, para 409 o 403), sin dependencias de Spring Web.

3. **Puerto de entrada** — `application/port/in/`.
   - Interfaz `XxxUseCase` con un solo método.
   - Su comando o consulta de entrada como `record`.

4. **Caso de uso** — `application/service/`.
   - Clase `@Service` que implementa el puerto de entrada.
   - `@Transactional` si escribe en base de datos (`readOnly = true` si solo lee).
   - Inyección por constructor, sin `@Autowired` en atributos.
   - Aquí se validan las reglas de negocio y la propiedad del recurso (regla 4), no en el controlador.
   - Depende solo de puertos de salida y del dominio, nunca de repositorios Spring Data.

5. **Puerto de salida** — `application/port/out/`, solo si hace falta persistencia nueva.
   - Interfaz `XxxRepositoryPort` que recibe y devuelve modelos de dominio.

6. **Adaptador de persistencia** — `infrastructure/adapter/out/persistence/`.
   - Entidad JPA (`XxxEntity`) distinta del modelo de dominio.
   - Repositorio Spring Data (`XxxJpaRepository`).
   - Mapper entidad ↔ dominio.
   - Clase adaptadora `@Component` que implementa el puerto de salida.
   - Recuerda `ddl-auto=validate`: la entidad debe coincidir exactamente con el esquema.

7. **Migración** — si cambia el esquema, crea una migración Flyway siguiendo la skill `migracion-flyway`
   (si todavía no existe, sigue la sección "Base de datos" de `CLAUDE.md`). Nunca edites una migración aplicada.

8. **Controlador y DTOs** — `infrastructure/adapter/in/web/`.
   - DTOs de entrada y salida como `record`; los de entrada con Bean Validation (`@NotBlank`, `@Size`, ...).
   - Mapper DTO ↔ comando/modelo.
   - El controlador solo llama al puerto de entrada y siempre responde DTOs.
   - Prefijo `/api`, recursos en plural y español; listados paginados con `page` y `size`.
   - Los errores salen por el `@RestControllerAdvice` de `infrastructure/config/` con el formato
     `{ "status", "error", "mensaje", "timestamp" }`; si hay una excepción de dominio nueva, mapéala ahí.

9. **Autorización** — por rol en `infrastructure/security/SecurityConfig` o con `@PreAuthorize`.
   Solo `/api/auth/**` y la documentación son públicas.

10. **Pruebas unitarias** — `backend/src/test/java/com/primerempleo/...`.
    - JUnit 5 + Mockito sobre los puertos de salida; sin base de datos real.
    - Un caso para el camino feliz y uno por cada regla de negocio involucrada.
    - Nombres descriptivos en español (por ejemplo, `deberiaRechazarPostulacionDuplicada`).

11. **Ejecutar** `./mvnw test` desde `/backend` y corregir hasta que pase.

12. **Postman** — agrega la petición siguiendo la skill `endpoint-postman`, con al menos una prueba
    de código de estado (si la skill aún no existe, sigue la sección "API" de `CLAUDE.md`).

13. **Cerrar.**
    - Actualiza `CONTEXT.md` (estado y registro de decisiones si tomaste alguna).
    - Responde con un resumen: archivos creados o modificados, endpoint, códigos de respuesta posibles y qué falta.

## Lista de verificación final

- [ ] `domain/` no importa `jakarta.persistence` ni tiene `@Entity`, `@Table`, `@Column`.
- [ ] El controlador no inyecta repositorios ni adaptadores, solo puertos de entrada.
- [ ] El caso de uso no depende de repositorios Spring Data, solo de puertos de salida.
- [ ] Ninguna respuesta HTTP devuelve una entidad JPA ni un modelo de dominio: siempre DTOs.
- [ ] Toda la inyección es por constructor.
- [ ] Las reglas de negocio y la propiedad del recurso se validan en el caso de uso.
- [ ] Los DTOs de entrada tienen Bean Validation.
- [ ] Rol configurado en `SecurityConfig` o `@PreAuthorize`.
- [ ] Migración nueva (no editada) si cambió el esquema.
- [ ] `./mvnw test` en verde.
- [ ] Petición agregada a Postman con prueba de código de estado.
- [ ] `CONTEXT.md` actualizado.

Antes de abrir el pull request, sugiere ejecutar el agente `revisor-arquitectura`.
