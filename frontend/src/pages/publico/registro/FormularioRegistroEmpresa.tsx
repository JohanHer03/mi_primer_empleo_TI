import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { registrarEmpresa } from '../../../api/auth'
import { ErrorApi } from '../../../api/cliente'
import type { RegistroEmpresaPeticion } from '../../../api/tipos'
import { useAuth } from '../../../auth/useAuth'
import { idsDescripcion } from '../../../components/accesibilidad'
import { AvisoDemora } from '../../../components/AvisoDemora'
import { Campo } from '../../../components/Campo'
import { aplicarErroresDeValidacion } from '../../../components/erroresServidor'
import formulario from '../../../components/Formulario.module.css'
import { esquemaEmpresa, limpiarNit, vacioANulo, type DatosEmpresa } from './esquemas'

const CAMPOS_SERVIDOR = ['email', 'password', 'razonSocial', 'nit', 'sector', 'ciudad', 'sitioWeb'] as const

function aPeticion(datos: DatosEmpresa): RegistroEmpresaPeticion {
  return {
    email: datos.email,
    password: datos.password,
    razonSocial: datos.razonSocial,
    nit: limpiarNit(datos.nit),
    sector: vacioANulo(datos.sector),
    ciudad: vacioANulo(datos.ciudad),
    sitioWeb: vacioANulo(datos.sitioWeb),
  }
}

/** HU-06: registro de empresa (POST /api/auth/registro/empresa). */
export function FormularioRegistroEmpresa() {
  const { iniciarSesion } = useAuth()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<DatosEmpresa>({
    resolver: zodResolver(esquemaEmpresa),
    defaultValues: { sector: '', ciudad: 'Medellín', sitioWeb: '' },
  })

  const mutacion = useMutation({
    mutationFn: async (datos: DatosEmpresa) => {
      const { token } = await registrarEmpresa(aPeticion(datos))
      if (!iniciarSesion(token)) throw new Error('El servidor devolvió un token inválido')
    },
    onMutate: () => setErrorGeneral(null),
    onError: (error) => {
      // 409 por correo o por NIT repetido (contrato 2.4)
      if (error instanceof ErrorApi && error.status === 409) {
        setError(/nit/i.test(error.message) ? 'nit' : 'email', { type: 'server', message: error.message })
      } else if (!aplicarErroresDeValidacion(error, setError, CAMPOS_SERVIDOR)) {
        setErrorGeneral(error.message)
      }
    },
  })

  return (
    <form className={formulario.formulario} onSubmit={handleSubmit((datos) => mutacion.mutate(datos))} noValidate>
      <Campo id="e-razon" etiqueta="Razón social" error={errors.razonSocial?.message}>
        <input
          id="e-razon"
          autoComplete="organization"
          aria-invalid={!!errors.razonSocial}
          aria-describedby={idsDescripcion('e-razon', errors.razonSocial?.message)}
          {...register('razonSocial')}
        />
      </Campo>

      <Campo
        id="e-nit"
        etiqueta="NIT"
        error={errors.nit?.message}
        ayuda="Con el dígito de verificación. Puedes escribirlo con puntos y guion."
      >
        <input
          id="e-nit"
          inputMode="numeric"
          placeholder="900.123.456-7"
          aria-invalid={!!errors.nit}
          aria-describedby={idsDescripcion('e-nit', errors.nit?.message, 'ayuda')}
          {...register('nit')}
        />
      </Campo>

      <Campo id="e-email" etiqueta="Correo de la empresa" error={errors.email?.message}>
        <input
          id="e-email"
          type="email"
          autoComplete="email"
          aria-invalid={!!errors.email}
          aria-describedby={idsDescripcion('e-email', errors.email?.message)}
          {...register('email')}
        />
      </Campo>

      <Campo id="e-password" etiqueta="Contraseña" error={errors.password?.message} ayuda="Entre 8 y 72 caracteres.">
        <input
          id="e-password"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.password}
          aria-describedby={idsDescripcion('e-password', errors.password?.message, 'ayuda')}
          {...register('password')}
        />
      </Campo>

      <Campo id="e-confirmar" etiqueta="Confirma la contraseña" error={errors.confirmarPassword?.message}>
        <input
          id="e-confirmar"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.confirmarPassword}
          aria-describedby={idsDescripcion('e-confirmar', errors.confirmarPassword?.message)}
          {...register('confirmarPassword')}
        />
      </Campo>

      <Campo id="e-sector" etiqueta="Sector" opcional error={errors.sector?.message}>
        <input
          id="e-sector"
          placeholder="Desarrollo de software"
          aria-invalid={!!errors.sector}
          aria-describedby={idsDescripcion('e-sector', errors.sector?.message)}
          {...register('sector')}
        />
      </Campo>

      <Campo id="e-ciudad" etiqueta="Ciudad" opcional error={errors.ciudad?.message}>
        <input
          id="e-ciudad"
          autoComplete="address-level2"
          aria-invalid={!!errors.ciudad}
          aria-describedby={idsDescripcion('e-ciudad', errors.ciudad?.message)}
          {...register('ciudad')}
        />
      </Campo>

      <Campo id="e-web" etiqueta="Sitio web" opcional error={errors.sitioWeb?.message}>
        <input
          id="e-web"
          type="url"
          autoComplete="url"
          placeholder="https://"
          aria-invalid={!!errors.sitioWeb}
          aria-describedby={idsDescripcion('e-web', errors.sitioWeb?.message)}
          {...register('sitioWeb')}
        />
      </Campo>

      <p className={formulario.aviso}>
        Un administrador verificará tu empresa antes de que puedas publicar vacantes. Mientras tanto puedes completar tu
        perfil.
      </p>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <button type="submit" className="boton" disabled={mutacion.isPending}>
        {mutacion.isPending ? 'Creando cuenta…' : 'Crear cuenta de empresa'}
      </button>
    </form>
  )
}
