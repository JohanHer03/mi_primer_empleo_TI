# Frontend — Primer Empleo TI

React 19 + Vite + TypeScript. Rutas con React Router, peticiones con TanStack Query,
formularios con react-hook-form + zod y estilos con CSS Modules.
Plan por fases: [`docs/plan-frontend.md`](../docs/plan-frontend.md). Reglas: [`CLAUDE.md`](../CLAUDE.md).

## Arrancar en local

Requiere Node 22 o superior.

```bash
cp .env.example .env.local
npm install
npm run dev        # http://localhost:5173
npm run lint
npm run build
```

## Modo simulado (`VITE_USAR_MOCKS=true`)

Mientras el backend no tenga un endpoint, cada función de `src/api/` responde con datos de `src/api/mocks/`.
Usuarios de demostración (contraseña `demo1234` para los tres):

| Correo | Rol |
| --- | --- |
| candidato@demo.co | CANDIDATO |
| empresa@demo.co | EMPRESA |
| admin@demo.co | ADMIN |
| inactivo@demo.co | CANDIDATO desactivado (el login responde 403) |

Las cuentas creadas en `/registro` se guardan en `localStorage` de ese navegador (clave `primer-empleo.mock.cuentas`),
contraseña incluida en texto plano: en modo simulado usa solo datos de prueba.
El NIT `9001234567` ya pertenece a la empresa de demostración, sirve para probar el error de NIT repetido.

Para simular una red lenta o el arranque en frío del backend (y ver los estados de carga y el aviso
"Estamos despertando el servidor"), en la consola del navegador:

```js
localStorage.setItem('primer-empleo.mock.latencia', '9000')  // milisegundos; removeItem para volver a 400
```

Con `VITE_USAR_MOCKS=false` (o sin definir, como en Docker y Vercel) todas las llamadas van al backend.

## Estructura

```
src/
  api/          cliente.ts (único cliente HTTP), tipos.ts (DTOs), una función por endpoint y mocks/
  auth/         sesión (token en localStorage), AuthProvider, useAuth, rutas protegidas por rol
  components/   estados de carga, vacío y error; estilos de formulario
  layouts/      LayoutPublico (login, registro) y LayoutApp (menú según el rol)
  pages/        publico/, candidato/, empresa/, admin/
  router.tsx    todas las rutas
```
