import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { registrarCandidato } from '../../../api/auth'
import { ErrorApi } from '../../../api/cliente'
import type { RegistroCandidatoPeticion } from '../../../api/tipos'
import { useAuth } from '../../../auth/useAuth'
import { idsDescripcion } from '../../../components/accesibilidad'
import { AvisoDemora } from '../../../components/AvisoDemora'
import { Campo } from '../../../components/Campo'
import { aplicarErroresDeValidacion } from '../../../components/erroresServidor'
import formulario from '../../../components/Formulario.module.css'
import { esquemaCandidato, SEMESTRES, vacioANulo, type DatosCandidato } from './esquemas'

const CAMPOS_SERVIDOR = ['email', 'password', 'nombres', 'apellidos', 'estadoAcademico', 'semestre', 'universidad', 'programa', 'ciudad'] as const

function aPeticion(datos: DatosCandidato): RegistroCandidatoPeticion {
  return {
    email: datos.email,
    password: datos.password,
    nombres: datos.nombres,
    apellidos: datos.apellidos,
    estadoAcademico: datos.estadoAcademico,
    semestre: datos.estadoAcademico === 'ESTUDIANTE' ? Number(datos.semestre) : null,
    universidad: vacioANulo(datos.universidad),
    programa: vacioANulo(datos.programa),
    ciudad: vacioANulo(datos.ciudad),
  }
}

/** HU-01: registro de candidato (POST /api/auth/registro/candidato). */
export function FormularioRegistroCandidato() {
  const { iniciarSesion } = useAuth()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    control,
    setValue,
    setError,
    formState: { errors },
  } = useForm<DatosCandidato>({
    resolver: zodResolver(esquemaCandidato),
    defaultValues: { semestre: '', universidad: '', programa: '', ciudad: 'Medellín' },
  })
  const esEstudiante = useWatch({ control, name: 'estadoAcademico' }) === 'ESTUDIANTE'

  // Al registrarse queda con la sesión iniciada; SoloSinSesion redirige al inicio del candidato
  const mutacion = useMutation({
    mutationFn: async (datos: DatosCandidato) => {
      const { token } = await registrarCandidato(aPeticion(datos))
      if (!iniciarSesion(token)) throw new Error('El servidor devolvió un token inválido')
    },
    onMutate: () => setErrorGeneral(null),
    onError: (error) => {
      if (error instanceof ErrorApi && error.status === 409) {
        setError('email', { type: 'server', message: error.message })
      } else if (!aplicarErroresDeValidacion(error, setError, CAMPOS_SERVIDOR)) {
        setErrorGeneral(error.message)
      }
    },
  })

  return (
    <form className={formulario.formulario} onSubmit={handleSubmit((datos) => mutacion.mutate(datos))} noValidate>
      <Campo id="c-nombres" etiqueta="Nombres" error={errors.nombres?.message}>
        <input
          id="c-nombres"
          autoComplete="given-name"
          aria-invalid={!!errors.nombres}
          aria-describedby={idsDescripcion('c-nombres', errors.nombres?.message)}
          {...register('nombres')}
        />
      </Campo>

      <Campo id="c-apellidos" etiqueta="Apellidos" error={errors.apellidos?.message}>
        <input
          id="c-apellidos"
          autoComplete="family-name"
          aria-invalid={!!errors.apellidos}
          aria-describedby={idsDescripcion('c-apellidos', errors.apellidos?.message)}
          {...register('apellidos')}
        />
      </Campo>

      <Campo id="c-email" etiqueta="Correo" error={errors.email?.message}>
        <input
          id="c-email"
          type="email"
          autoComplete="email"
          aria-invalid={!!errors.email}
          aria-describedby={idsDescripcion('c-email', errors.email?.message)}
          {...register('email')}
        />
      </Campo>

      <Campo id="c-password" etiqueta="Contraseña" error={errors.password?.message} ayuda="Entre 8 y 72 caracteres.">
        <input
          id="c-password"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.password}
          aria-describedby={idsDescripcion('c-password', errors.password?.message, 'ayuda')}
          {...register('password')}
        />
      </Campo>

      <Campo id="c-confirmar" etiqueta="Confirma la contraseña" error={errors.confirmarPassword?.message}>
        <input
          id="c-confirmar"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.confirmarPassword}
          aria-describedby={idsDescripcion('c-confirmar', errors.confirmarPassword?.message)}
          {...register('confirmarPassword')}
        />
      </Campo>

      <fieldset
        className={formulario.grupoOpciones}
        aria-invalid={!!errors.estadoAcademico}
        aria-describedby={idsDescripcion('c-estado', errors.estadoAcademico?.message)}
      >
        <legend>Estado académico</legend>
        <label className={formulario.opcion}>
          <input type="radio" value="ESTUDIANTE" {...register('estadoAcademico')} />
          Estudiante
        </label>
        <label className={formulario.opcion}>
          <input
            type="radio"
            value="EGRESADO"
            {...register('estadoAcademico', { onChange: () => setValue('semestre', '') })}
          />
          Egresado
        </label>
        {errors.estadoAcademico && (
          <p id="c-estado-error" className={formulario.errorCampo}>
            {errors.estadoAcademico.message}
          </p>
        )}
      </fieldset>

      {esEstudiante && (
        <Campo id="c-semestre" etiqueta="Semestre que cursas" error={errors.semestre?.message}>
          <select
            id="c-semestre"
            aria-invalid={!!errors.semestre}
            aria-describedby={idsDescripcion('c-semestre', errors.semestre?.message)}
            {...register('semestre')}
          >
            <option value="">Elige un semestre</option>
            {SEMESTRES.map((semestre) => (
              <option key={semestre} value={semestre}>
                {semestre}
              </option>
            ))}
          </select>
        </Campo>
      )}

      <Campo id="c-universidad" etiqueta="Universidad" opcional error={errors.universidad?.message}>
        <input
          id="c-universidad"
          autoComplete="organization"
          aria-invalid={!!errors.universidad}
          aria-describedby={idsDescripcion('c-universidad', errors.universidad?.message)}
          {...register('universidad')}
        />
      </Campo>

      <Campo id="c-programa" etiqueta="Programa" opcional error={errors.programa?.message}>
        <input
          id="c-programa"
          aria-invalid={!!errors.programa}
          aria-describedby={idsDescripcion('c-programa', errors.programa?.message)}
          {...register('programa')}
        />
      </Campo>

      <Campo id="c-ciudad" etiqueta="Ciudad" opcional error={errors.ciudad?.message}>
        <input
          id="c-ciudad"
          autoComplete="address-level2"
          aria-invalid={!!errors.ciudad}
          aria-describedby={idsDescripcion('c-ciudad', errors.ciudad?.message)}
          {...register('ciudad')}
        />
      </Campo>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <button type="submit" className="boton" disabled={mutacion.isPending}>
        {mutacion.isPending ? 'Creando cuenta…' : 'Crear cuenta de candidato'}
      </button>
    </form>
  )
}
