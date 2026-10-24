---
name: pantalla-react
description: Crea una pantalla o flujo del frontend React conectado a la API. Usar cuando se pida una página, formulario o vista nueva del frontend.
---

# Pantalla React

`CLAUDE.md` manda sobre esta skill. Plan y rutas: `docs/plan-frontend.md`. Estructura: `frontend/README.md`.

## Procedimiento

1. **Confirmar el alcance:** historia de usuario (HU-xx), rol que ve la pantalla, endpoints que usa
   y su DTO (`docs/contrato-api.md`). Si el contrato no está acordado, preguntar antes de inventarlo.
2. **Tipos** en `src/api/tipos.ts`, reflejando los DTOs del backend (mismos nombres de campo).
3. **Funciones de la API** en `src/api/<recurso>.ts`, siempre con `peticion()` de `src/api/cliente.ts`
   (agrega Bearer, usa `${VITE_API_URL}/api`, cierra la sesión ante un 401). Nunca `fetch` directo.
   Si el backend aún no tiene el endpoint, agregar su versión en `src/api/mocks/` y elegirla con `usarMocks`.
4. **Página** en `src/pages/<rol>/` (reemplaza el `PantallaPendiente` si existe) y componentes
   reutilizables en `src/components/`. Estilos en un CSS Module junto al componente, con las variables de `index.css`.
5. **Datos con TanStack Query:** `useQuery` para leer (clave `['recurso', ...params]`) y `useMutation` para escribir,
   invalidando las consultas afectadas en `onSuccess`.
6. **Estados obligatorios:** carga (`<Cargando />`), vacío (`<EstadoVacio />`), error (`<MensajeError />` con reintentar)
   y éxito (mensaje o redirección). Botones deshabilitados mientras una mutación está pendiente.
7. **Formularios** con react-hook-form + zod: las reglas del esquema copian las de Bean Validation del DTO.
   Errores por campo bajo cada input y el `mensaje` del backend (`{ status, error, mensaje, timestamp }`) arriba del botón.
8. **Ruta** en `src/router.tsx`, dentro del `RutaProtegida` del rol; si va en el menú, agregarla en `src/auth/rutasPorRol.ts`.
9. **Accesibilidad:** `label` con `htmlFor`, `aria-invalid` en campos con error, `role="alert"` en errores, títulos `h1`.
10. Ejecutar `npm run lint` y `npm run build` desde `/frontend` y corregir todo.
11. Probar en el navegador con `npm run dev` (modo simulado si el backend no está listo): carga, vacío, error y éxito.
12. Actualizar el estado en `docs/plan-frontend.md` y `CONTEXT.md`.
13. **Resumen final:** archivos creados, ruta, endpoints usados (reales o simulados) y cómo probar la pantalla.
