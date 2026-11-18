# Limpieza de las cuentas de prueba

Borra de la base las cuentas de prueba que dejan Postman y las pruebas en local: todas las que tienen un correo terminado
en **`@example.com`**, con sus perfiles, habilidades, vacantes y postulaciones. **Nunca** borra al administrador ni las
cuentas de demostración de `scripts/datos-demo` (terminan en `.example`, no en `.com`).

Se ejecuta en **Supabase → SQL Editor**. Como el backend local y producción usan la misma base, basta con hacerlo una vez.

1. Ejecuta `1-contar.sql` (solo lectura) y revisa los números. La fila "usuarios que se conservan" debe ser tu
   administrador más las cuentas reales o de demostración que tengas.
2. Si los números cuadran, ejecuta `2-borrar.sql`. Es un borrado **permanente**: va en una transacción y al final debe
   responder 0 en las tres filas de comprobación.

Orden del borrado (lo exigen las claves foráneas de V1): primero las postulaciones a vacantes de empresas de prueba, luego
esas vacantes y por último los usuarios, que arrastran en cascada su empresa o candidato, sus habilidades y las
postulaciones del candidato. Si un candidato que no es de prueba se postuló a una vacante de prueba, esa postulación también
se borra (el paso 1 la cuenta aparte).

Se puede volver a ejecutar: si ya no hay cuentas de prueba, no borra nada. Probado en un PostgreSQL 17 desechable con los
datos de demostración y cuentas de prueba cruzadas con ellos: solo desaparecieron las de prueba.
