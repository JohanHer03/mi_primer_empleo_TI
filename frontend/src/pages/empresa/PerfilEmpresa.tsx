import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { actualizarMiPerfil } from '../../api/empresas'
import type { EmpresaPerfil } from '../../api/tipos'
import { idsDescripcion } from '../../components/accesibilidad'
import { AvisoDemora } from '../../components/AvisoDemora'
import { AvisoVerificacion } from '../../components/AvisoVerificacion'
import { Campo } from '../../components/Campo'
import { Cargando, MensajeError } from '../../components/Estados'
import { aplicarErroresDeValidacion } from '../../components/erroresServidor'
import formulario from '../../components/Formulario.module.css'
import { vacioANulo } from '../publico/registro/esquemas'
import { CLAVES, useMiPerfil } from './consultas'
import estilos from './PaginasEmpresa.module.css'

// Validaciones de ActualizarEmpresaPeticion (contrato 4.2), las mismas del registro (2.4)
const esquema = z.object({
  razonSocial: z.string().trim().min(1, 'Escribe la razón social').max(200, 'Máximo 200 caracteres'),
  sector: z.string().trim().max(100, 'Máximo 100 caracteres'),
  ciudad: z.string().trim().max(100, 'Máximo 100 caracteres'),
  sitioWeb: z
    .string()
    .trim()
    .max(255, 'Máximo 255 caracteres')
    .refine((url) => url === '' || /^https?:\/\/[^\s.]+\.[^\s]+$/.test(url), 'Escribe una dirección que empiece por http:// o https://'),
})

type DatosPerfil = z.infer<typeof esquema>

const CAMPOS_SERVIDOR = ['razonSocial', 'sector', 'ciudad', 'sitioWeb'] as const

/** HU-06: la empresa revisa y completa su perfil (GET y PUT /api/empresas/me). */
export default function PerfilEmpresa() {
  const perfil = useMiPerfil()

  return (
    <section className={estilos.pagina}>
      <h1>Perfil de empresa</h1>
      {perfil.isPending && <Cargando texto="Cargando tu perfil…" />}
      {perfil.isError && <MensajeError error={perfil.error} alReintentar={() => perfil.refetch()} />}
      {perfil.isSuccess && <FormularioPerfil perfil={perfil.data} />}
    </section>
  )
}

function FormularioPerfil({ perfil }: { perfil: EmpresaPerfil }) {
  const queryClient = useQueryClient()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardado, setGuardado] = useState(false)
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isDirty },
  } = useForm<DatosPerfil>({
    resolver: zodResolver(esquema),
    defaultValues: {
      razonSocial: perfil.razonSocial,
      sector: perfil.sector ?? '',
      ciudad: perfil.ciudad ?? '',
      sitioWeb: perfil.sitioWeb ?? '',
    },
  })

  const mutacion = useMutation({
    mutationFn: (datos: DatosPerfil) =>
      actualizarMiPerfil({
        razonSocial: datos.razonSocial.trim(),
        sector: vacioANulo(datos.sector),
        ciudad: vacioANulo(datos.ciudad),
        sitioWeb: vacioANulo(datos.sitioWeb),
      }),
    onMutate: () => {
      setErrorGeneral(null)
      setGuardado(false)
    },
    onSuccess: (actualizado) => {
      queryClient.setQueryData(CLAVES.perfil, actualizado)
      reset({
        razonSocial: actualizado.razonSocial,
        sector: actualizado.sector ?? '',
        ciudad: actualizado.ciudad ?? '',
        sitioWeb: actualizado.sitioWeb ?? '',
      })
      setGuardado(true)
    },
    onError: (error) => {
      if (!aplicarErroresDeValidacion(error, setError, CAMPOS_SERVIDOR)) setErrorGeneral(error.message)
    },
  })

  return (
    <div className={estilos.contenidoFormulario}>
      {!perfil.verificada && <AvisoVerificacion />}

      <dl className={estilos.datosFijos}>
        <div>
          <dt>NIT</dt>
          <dd>{perfil.nit}</dd>
        </div>
        <div>
          <dt>Correo de la cuenta</dt>
          <dd>{perfil.email}</dd>
        </div>
        <div>
          <dt>Estado</dt>
          <dd>{perfil.verificada ? 'Verificada' : 'Pendiente de verificación'}</dd>
        </div>
      </dl>
      <p className={formulario.nota}>El NIT y el correo no se pueden cambiar.</p>

      <form className={formulario.formulario} onSubmit={handleSubmit((datos) => mutacion.mutate(datos))} noValidate>
        <Campo id="p-razon" etiqueta="Razón social" error={errors.razonSocial?.message}>
          <input
            id="p-razon"
            autoComplete="organization"
            aria-invalid={!!errors.razonSocial}
            aria-describedby={idsDescripcion('p-razon', errors.razonSocial?.message)}
            {...register('razonSocial')}
          />
        </Campo>

        <Campo id="p-sector" etiqueta="Sector" opcional error={errors.sector?.message}>
          <input
            id="p-sector"
            placeholder="Desarrollo de software"
            aria-invalid={!!errors.sector}
            aria-describedby={idsDescripcion('p-sector', errors.sector?.message)}
            {...register('sector')}
          />
        </Campo>

        <Campo id="p-ciudad" etiqueta="Ciudad" opcional error={errors.ciudad?.message}>
          <input
            id="p-ciudad"
            autoComplete="address-level2"
            aria-invalid={!!errors.ciudad}
            aria-describedby={idsDescripcion('p-ciudad', errors.ciudad?.message)}
            {...register('ciudad')}
          />
        </Campo>

        <Campo id="p-web" etiqueta="Sitio web" opcional error={errors.sitioWeb?.message}>
          <input
            id="p-web"
            type="url"
            autoComplete="url"
            placeholder="https://"
            aria-invalid={!!errors.sitioWeb}
            aria-describedby={idsDescripcion('p-web', errors.sitioWeb?.message)}
            {...register('sitioWeb')}
          />
        </Campo>

        {errorGeneral && (
          <p className={formulario.errorGeneral} role="alert">
            {errorGeneral}
          </p>
        )}
        {guardado && !isDirty && (
          <p className={estilos.exito} role="status">
            Cambios guardados.
          </p>
        )}

        <AvisoDemora activo={mutacion.isPending} />

        <div className={estilos.acciones}>
          <button type="submit" className="boton" disabled={mutacion.isPending || !isDirty}>
            {mutacion.isPending ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </div>
      </form>
    </div>
  )
}
