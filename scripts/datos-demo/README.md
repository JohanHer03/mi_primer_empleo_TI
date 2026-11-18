# Datos de demostración

Carga por la API, como lo haría una persona desde la aplicación, los datos ficticios de `datos.mjs` para la sustentación:

| Qué | Cuántos | Para mostrar |
| --- | --- | --- |
| Empresas | 4: tres verificadas y **Cafetal Labs sin verificar** | La verificación del administrador en vivo (regla 3) |
| Vacantes | 7 en Medellín, Envigado y Rionegro; una ya cerrada con una persona contratada | Búsqueda con filtros, compatibilidad y "lo que te falta" |
| Candidatos | 6 con perfiles distintos (datos, backend, frontend, nube) | Postulantes ordenados por compatibilidad |
| Postulaciones | 9 en todos los estados: enviada, en revisión, entrevista, contratado y rechazada | "Mis postulaciones" y la gestión de la empresa (regla 5) |

Ninguna empresa ni persona es real. Los correos usan el dominio reservado `.example`: no llegan a ningún buzón.
Todas las cuentas de demostración tienen la misma contraseña, la que se pase en `DEMO_PASSWORD`.

## Requisitos

- Node.js 18 o superior (el mismo del frontend). El script no usa dependencias.
- La contraseña del administrador (`ADMIN_EMAIL` y `ADMIN_PASSWORD`, las mismas configuradas en Vercel): verifica las empresas.

## 1. Revisar los datos (sin conectarse a nada)

```bash
node scripts/datos-demo/cargar.mjs --revisar
```

Comprueba que las habilidades existan en el catálogo inicial (V4), los límites del contrato (títulos, salarios, de 1 a 15
habilidades por vacante, semestre solo para estudiantes) y que no haya correos, NIT ni postulaciones repetidos.

## 2. Cargar

Antes, conviene borrar de Supabase las cuentas de prueba `@example.com` para que solo se vean los datos de demostración.

**Importante:** el backend local (`.env`) y producción usan **la misma base de Supabase**. Cargar contra el backend local
o contra producción deja los datos en el mismo lugar: basta con correrlo una vez.

Desde la raíz del repositorio, en **PowerShell**:

```powershell
$env:DEMO_API = "https://mi-primer-empleo-ti.vercel.app"
$env:DEMO_PASSWORD = Read-Host "Contraseña para las cuentas de demostración"
$env:ADMIN_EMAIL = "<correo del administrador>"
$env:ADMIN_PASSWORD = Read-Host "Contraseña del administrador"
node scripts/datos-demo/cargar.mjs
Remove-Item Env:DEMO_PASSWORD, Env:ADMIN_PASSWORD
```

O en **Git Bash**:

```bash
export DEMO_API=https://mi-primer-empleo-ti.vercel.app ADMIN_EMAIL="<correo del administrador>"
read -rsp "Contraseña de demostración: " DEMO_PASSWORD && export DEMO_PASSWORD && echo
read -rsp "Contraseña del administrador: " ADMIN_PASSWORD && export ADMIN_PASSWORD && echo
node scripts/datos-demo/cargar.mjs
unset DEMO_PASSWORD ADMIN_PASSWORD
```

`Read-Host` y `read -s` piden la contraseña sin mostrarla ni dejarla en el historial de la terminal. El script tampoco
imprime contraseñas ni tokens. `DEMO_API` es el origen sin `/api` (por defecto `http://localhost:8080`).

La primera petición puede tardar unos 10 segundos: el contenedor de Vercel arranca en frío. El script reintenta.

## Volver a correrlo

Se puede correr varias veces sin duplicar nada: las cuentas que ya existen inician sesión (con la misma
`DEMO_PASSWORD`), las vacantes se buscan por título, una postulación repetida se ignora y los estados solo avanzan.
Si una cuenta ya existe con otra contraseña, el script se detiene y lo dice.

## Cuentas para la sustentación

| Rol | Correo |
| --- | --- |
| Candidata (Laura) | `laura.gomez@demo.example` |
| Empresa verificada | `talento@andinacodigo.example` |
| Empresa por verificar | `hola@cafetallabs.example` |
| Administrador | El de `ADMIN_EMAIL` |

Guion sugerido:

1. Laura busca vacantes y ve las más compatibles primero.
2. Abre "Analista de datos junior", revisa "lo que te falta" y se postula.
3. En "Mis postulaciones" ve su entrevista con Andina Código.
4. Andina Código revisa los postulantes de "Desarrollador backend Java junior" y avanza a Valeria.
5. El administrador verifica a Cafetal Labs.
