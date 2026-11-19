import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { cambiarContrasena } from '../api/usuarios'
import { contrasena } from '../pages/publico/registro/esquemas'
import { idsDescripcion } from './accesibilidad'
import { AvisoDemora } from './AvisoDemora'
import { Campo } from './Campo'
import estilos from './CambiarContrasena.module.css'
import { aplicarErroresDeValidacion } from './erroresServidor'
import formulario from './Formulario.module.css'

// Validaciones de CambiarContrasenaPeticion (contrato 8.1); la confirmación solo existe en el frontend
const esquema = z
  .object({
    contrasenaActual: z.string().min(1, 'Escribe tu contraseña actual').max(72, 'Máximo 72 caracteres'),
    contrasenaNueva: contrasena,
    confirmarContrasena: z.string(),
  })
  .superRefine((datos, ctx) => {
    if (datos.contrasenaNueva !== datos.confirmarContrasena) {
      ctx.addIssue({ code: 'custom', path: ['confirmarContrasena'], message: 'Las contraseñas no coinciden' })
    }
  })

type DatosContrasena = z.infer<typeof esquema>

const CAMPOS_SERVIDOR = ['contrasenaActual', 'contrasenaNueva'] as const
const VACIO: DatosContrasena = { contrasenaActual: '', contrasenaNueva: '', confirmarContrasena: '' }

/** Cambio de contraseña del usuario de la sesión (PUT /api/usuarios/me/contrasena), en el perfil de cada rol. */
export function CambiarContrasena() {
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [cambiada, setCambiada] = useState(false)
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isDirty },
  } = useForm<DatosContrasena>({ resolver: zodResolver(esquema), defaultValues: VACIO })

  const mutacion = useMutation({
    mutationFn: ({ contrasenaActual, contrasenaNueva }: DatosContrasena) =>
      cambiarContrasena({ contrasenaActual, contrasenaNueva }),
    onMutate: () => {
      setErrorGeneral(null)
      setCambiada(false)
    },
    onSuccess: () => {
      reset(VACIO)
      setCambiada(true)
    },
    onError: (error) => {
      if (!aplicarErroresDeValidacion(error, setError, CAMPOS_SERVIDOR)) setErrorGeneral(error.message)
    },
  })

  return (
    <form
      className={`${formulario.formulario} ${estilos.seccion}`}
      onSubmit={handleSubmit((datos) => mutacion.mutate(datos))}
      aria-labelledby="titulo-contrasena"
      noValidate
    >
      <h2 id="titulo-contrasena">Cambiar contraseña</h2>

      <Campo id="c-actual" etiqueta="Contraseña actual" error={errors.contrasenaActual?.message}>
        <input
          id="c-actual"
          type="password"
          autoComplete="current-password"
          aria-invalid={!!errors.contrasenaActual}
          aria-describedby={idsDescripcion('c-actual', errors.contrasenaActual?.message)}
          {...register('contrasenaActual')}
        />
      </Campo>

      <Campo
        id="c-nueva"
        etiqueta="Contraseña nueva"
        ayuda="Entre 8 y 72 caracteres."
        error={errors.contrasenaNueva?.message}
      >
        <input
          id="c-nueva"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.contrasenaNueva}
          aria-describedby={idsDescripcion('c-nueva', errors.contrasenaNueva?.message, 'ayuda')}
          {...register('contrasenaNueva')}
        />
      </Campo>

      <Campo id="c-confirmar" etiqueta="Confirma la contraseña nueva" error={errors.confirmarContrasena?.message}>
        <input
          id="c-confirmar"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.confirmarContrasena}
          aria-describedby={idsDescripcion('c-confirmar', errors.confirmarContrasena?.message)}
          {...register('confirmarContrasena')}
        />
      </Campo>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}
      {cambiada && !isDirty && (
        <p className={estilos.exito} role="status">
          Contraseña cambiada. Úsala la próxima vez que inicies sesión. Si tenías la sesión abierta en otro dispositivo,
          seguirá activa hasta 2 horas.
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <div className={estilos.acciones}>
        <button type="submit" className="boton" disabled={mutacion.isPending || !isDirty}>
          {mutacion.isPending ? 'Cambiando…' : 'Cambiar contraseña'}
        </button>
      </div>
    </form>
  )
}
