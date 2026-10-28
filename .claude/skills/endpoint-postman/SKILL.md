---
name: endpoint-postman
description: Agrega o actualiza peticiones en la colección de Postman del proyecto. Usar después de crear o cambiar un endpoint.
---

# Endpoint en Postman

`CLAUDE.md` manda sobre esta skill. Contrato de rutas y DTOs: `docs/contrato-api.md`.

## Dónde está

- Colección: `postman/primer-empleo.postman_collection.json` (formato v2.1).
- Entornos: `postman/local.postman_environment.json` y `postman/produccion.postman_environment.json`; solo definen `baseUrl`.
- Variables de colección: `token` (lo guardan login y registro), `passwordPrueba` y los datos que se generan en cada ejecución.

## Reglas

1. **Una carpeta por recurso:** Autenticación, Habilidades, Candidatos, Empresas, Vacantes, Postulaciones, Seguridad.
   Dentro, el orden de ejecución importa: primero lo que crea datos y después lo que los usa.
2. **URL** siempre con `{{baseUrl}}/api/...`, separada en `host` y `path` como las peticiones existentes.
3. **Autorización:** se hereda de la colección (`Bearer {{token}}`). Las de `/api/auth/**` y los casos "sin token" llevan `"auth": {"type": "noauth"}`.
4. **Token:** login y registro lo guardan con `pm.collectionVariables.set("token", pm.response.json().token)`.
   Si una prueba necesita otro rol, crea la cuenta en la misma ejecución y guarda su token en otra variable (`tokenEmpresa`, `tokenAdmin`).
5. **Pruebas:** cada petición tiene al menos `pm.response.to.have.status(...)`. Cuando aplique, agregar casos negativos:
   400 validación, 401 sin token, 403 rol o propiedad (regla 4), 404 inexistente, 409 duplicado.
   Si el contrato fija el `mensaje`, verificarlo también.
6. **Datos únicos:** correos, NIT y nombres que deben ser únicos se generan en el *pre-request* con `Date.now()`
   y dominio `@example.com`, para que la colección se pueda correr muchas veces sin limpiar la base.
7. **Cuerpos de ejemplo** realistas de Medellín (universidades, empresas, barrios), sin datos personales reales ni secretos.
8. El nombre de cada petición dice qué hace y qué código espera: `Publicar vacante (201)`, `Publicar vacante sin verificar (403)`.

## Cómo editar

- Editar el JSON con un script (Python o Node) que lo cargue, agregue las peticiones y lo vuelva a escribir con `indent=2`
  y `ensure_ascii=False`. No editar a mano partes grandes.
- Validar que siga siendo JSON válido: `python -m json.tool postman/primer-empleo.postman_collection.json > /dev/null`.

## Probar

Con el backend en local (`./mvnw spring-boot:run` desde `/backend`):

```bash
npx newman run postman/primer-empleo.postman_collection.json -e postman/local.postman_environment.json
```

Todas las aserciones deben pasar. Cada ejecución crea cuentas `@example.com` en Supabase.
No correr la colección contra `produccion` sin acordarlo, porque crea datos en la base real.
