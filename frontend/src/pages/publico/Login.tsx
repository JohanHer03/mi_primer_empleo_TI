import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { login } from '../../api/auth'
import { usarMocks } from '../../api/cliente'
import { useAuth } from '../../auth/useAuth'
import { idsDescripcion } from '../../components/accesibilidad'
import { AvisoDemora } from '../../components/AvisoDemora'
import { Campo } from '../../components/Campo'
import formulario from '../../components/Formulario.module.css'

// Validaciones de docs/contrato-api.md, sección 2.5
const esquema = z.object({
  email: z.string().trim().pipe(z.email('Escribe un correo válido')),
  password: z.string().min(1, 'Escribe tu contraseña'),
})

type DatosLogin = z.infer<typeof esquema>

export default function Login() {
  const { iniciarSesion } = useAuth()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DatosLogin>({ resolver: zodResolver(esquema) })

  // Al guardar la sesión, SoloSinSesion redirige al inicio del rol o a la página que pidió el login
  const mutacion = useMutation({
    mutationFn: async (datos: DatosLogin) => {
      const { token } = await login(datos)
      if (!iniciarSesion(token)) throw new Error('El servidor devolvió un token inválido')
    },
  })

  return (
    <>
      <h1>Iniciar sesión</h1>
      <form className={formulario.formulario} onSubmit={handleSubmit((datos) => mutacion.mutate(datos))} noValidate>
        <Campo id="email" etiqueta="Correo" error={errors.email?.message}>
          <input
            id="email"
            type="email"
            autoComplete="email"
            aria-invalid={!!errors.email}
            aria-describedby={idsDescripcion('email', errors.email?.message)}
            {...register('email')}
          />
        </Campo>

        <Campo id="password" etiqueta="Contraseña" error={errors.password?.message}>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            aria-invalid={!!errors.password}
            aria-describedby={idsDescripcion('password', errors.password?.message)}
            {...register('password')}
          />
        </Campo>

        {mutacion.isError && (
          <p className={formulario.errorGeneral} role="alert">
            {mutacion.error.message}
          </p>
        )}

        <AvisoDemora activo={mutacion.isPending} />

        <button type="submit" className="boton" disabled={mutacion.isPending}>
          {mutacion.isPending ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>

      <p className={formulario.nota}>
        ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
      </p>
      {usarMocks && (
        <p className={formulario.nota}>
          Modo simulado: candidato@demo.co, empresa@demo.co o admin@demo.co (ver frontend/README.md).
        </p>
      )}
    </>
  )
}
