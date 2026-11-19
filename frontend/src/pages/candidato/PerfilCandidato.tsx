import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useFieldArray, useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { actualizarMiPerfilCandidato, actualizarMisHabilidades } from '../../api/candidatos'
import type { CandidatoHabilidad, CandidatoPerfil, Habilidad } from '../../api/tipos'
import { idsDescripcion } from '../../components/accesibilidad'
import { AvisoDemora } from '../../components/AvisoDemora'
import { CambiarContrasena } from '../../components/CambiarContrasena'
import { Campo } from '../../components/Campo'
import { Cargando, MensajeError } from '../../components/Estados'
import { aplicarErroresDeValidacion } from '../../components/erroresServidor'
import formulario from '../../components/Formulario.module.css'
import { TEXTO_ESTADO_ACADEMICO, TEXTO_NIVEL } from '../../components/formato'
import { OpcionesHabilidades } from '../../components/OpcionesHabilidades'
import comunes from '../../components/Paginas.module.css'
import { useCatalogoHabilidades } from '../../hooks/useCatalogoHabilidades'
import { SEMESTRES, vacioANulo } from '../publico/registro/esquemas'
import { CLAVES, useMiPerfilCandidato } from './consultas'
import estilos from './PaginasCandidato.module.css'

const MAXIMO_HABILIDADES = 30

const textoOpcional = (maximo: number) => z.string().trim().max(maximo, `Máximo ${maximo} caracteres`)

// Validaciones de ActualizarCandidatoPeticion (contrato 5.1): las del registro (2.3) más GitHub y resumen
const esquemaDatos = z
  .object({
    nombres: z.string().trim().min(1, 'Escribe tus nombres').max(100, 'Máximo 100 caracteres'),
    apellidos: z.string().trim().min(1, 'Escribe tus apellidos').max(100, 'Máximo 100 caracteres'),
    estadoAcademico: z.enum(['ESTUDIANTE', 'EGRESADO'], 'Elige si eres estudiante o egresado'),
    semestre: z.string(),
    universidad: textoOpcional(150),
    programa: textoOpcional(150),
    ciudad: textoOpcional(100),
    githubUrl: textoOpcional(255).refine(
      (url) => url === '' || /^https:\/\/github\.com\/\S+$/.test(url),
      'Debe empezar por https://github.com/',
    ),
    resumen: textoOpcional(1000),
  })
  .superRefine((datos, ctx) => {
    if (datos.estadoAcademico === 'ESTUDIANTE' && datos.semestre === '') {
      ctx.addIssue({ code: 'custom', path: ['semestre'], message: 'Elige el semestre que cursas' })
    }
  })

type DatosPersonales = z.infer<typeof esquemaDatos>

const CAMPOS_DATOS = [
  'nombres',
  'apellidos',
  'estadoAcademico',
  'semestre',
  'universidad',
  'programa',
  'ciudad',
  'githubUrl',
  'resumen',
] as const

// Validaciones de PUT /api/candidatos/me/habilidades (contrato 5.2)
const esquemaHabilidades = z
  .object({
    habilidades: z
      .array(
        z.object({
          habilidadId: z.string().min(1, 'Elige una habilidad'),
          nivel: z.enum(['BASICO', 'INTERMEDIO', 'AVANZADO'], 'Elige el nivel'),
        }),
      )
      .max(MAXIMO_HABILIDADES, `Máximo ${MAXIMO_HABILIDADES} habilidades`),
  })
  .superRefine((datos, ctx) => {
    const vistas = new Set<string>()
    datos.habilidades.forEach((habilidad, indice) => {
      if (habilidad.habilidadId && vistas.has(habilidad.habilidadId)) {
        ctx.addIssue({ code: 'custom', path: ['habilidades', indice, 'habilidadId'], message: 'Ya la agregaste' })
      }
      vistas.add(habilidad.habilidadId)
    })
  })

type DatosHabilidades = z.infer<typeof esquemaHabilidades>

function desdePerfil(perfil: CandidatoPerfil): DatosPersonales {
  return {
    nombres: perfil.nombres,
    apellidos: perfil.apellidos,
    estadoAcademico: perfil.estadoAcademico,
    semestre: perfil.semestre === null ? '' : String(perfil.semestre),
    universidad: perfil.universidad ?? '',
    programa: perfil.programa ?? '',
    ciudad: perfil.ciudad ?? '',
    githubUrl: perfil.githubUrl ?? '',
    resumen: perfil.resumen ?? '',
  }
}

const aFilas = (habilidades: CandidatoHabilidad[]): DatosHabilidades => ({
  habilidades: habilidades.map(({ habilidadId, nivel }) => ({ habilidadId, nivel })),
})

/** HU-02: el candidato completa sus datos y sus habilidades con nivel (contrato 5.1 y 5.2). */
export default function PerfilCandidato() {
  const perfil = useMiPerfilCandidato()
  const catalogo = useCatalogoHabilidades()

  return (
    <section className={comunes.pagina}>
      <h1>Mi perfil</h1>
      {(perfil.isPending || catalogo.isPending) && <Cargando texto="Cargando tu perfil…" />}
      {perfil.isError && <MensajeError error={perfil.error} alReintentar={() => perfil.refetch()} />}
      {catalogo.isError && <MensajeError error={catalogo.error} alReintentar={() => catalogo.refetch()} />}
      {perfil.isSuccess && catalogo.isSuccess && (
        <>
          <dl className={`${comunes.datosFijos} ${estilos.anchoSeccion}`}>
            <div>
              <dt>Correo de la cuenta</dt>
              <dd>{perfil.data.email}</dd>
            </div>
            <div>
              <dt>Habilidades registradas</dt>
              <dd>{perfil.data.habilidades.length}</dd>
            </div>
          </dl>
          <FormularioHabilidades habilidades={perfil.data.habilidades} catalogo={catalogo.data} />
          <FormularioDatos perfil={perfil.data} />
          <CambiarContrasena />
        </>
      )}
    </section>
  )
}

function FormularioHabilidades({ habilidades, catalogo }: { habilidades: CandidatoHabilidad[]; catalogo: Habilidad[] }) {
  const queryClient = useQueryClient()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardado, setGuardado] = useState(false)
  const {
    register,
    control,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isDirty },
  } = useForm<DatosHabilidades>({ resolver: zodResolver(esquemaHabilidades), defaultValues: aFilas(habilidades) })
  const filas = useFieldArray({ control, name: 'habilidades' })

  const mutacion = useMutation({
    mutationFn: (datos: DatosHabilidades) => actualizarMisHabilidades(datos.habilidades),
    onMutate: () => {
      setErrorGeneral(null)
      setGuardado(false)
    },
    onSuccess: (guardadas) => {
      queryClient.setQueryData<CandidatoPerfil>(CLAVES.perfil, (actual) =>
        actual ? { ...actual, habilidades: guardadas } : actual,
      )
      reset(aFilas(guardadas))
      setGuardado(true)
      // La compatibilidad de la búsqueda cambia con las habilidades (las postulaciones enviadas conservan la suya)
      return queryClient.invalidateQueries({ queryKey: CLAVES.busqueda })
    },
    onError: (error) => {
      if (!aplicarErroresDeValidacion(error, setError, ['habilidades'])) setErrorGeneral(error.message)
    },
  })

  const errorLista = errors.habilidades?.message ?? errors.habilidades?.root?.message

  return (
    <form
      className={`${formulario.formulario} ${estilos.seccion}`}
      onSubmit={handleSubmit((datos) => mutacion.mutate(datos))}
      aria-labelledby="titulo-habilidades"
      noValidate
    >
      <h2 id="titulo-habilidades">Mis habilidades</h2>
      <p id="h-ayuda" className={formulario.ayuda}>
        Con ellas calculamos tu compatibilidad con cada vacante. Básico: lo has usado en clase o en un proyecto pequeño;
        intermedio: lo usas con soltura; avanzado: podrías enseñarlo. Hasta {MAXIMO_HABILIDADES}.
      </p>

      {filas.fields.length === 0 && (
        <p className={formulario.aviso}>
          Aún no tienes habilidades: tu compatibilidad con todas las vacantes será 0 %. Agrega las que manejas.
        </p>
      )}

      {filas.fields.map((campo, indice) => {
        const errorFila = errors.habilidades?.[indice]
        return (
          <div key={campo.id} className={estilos.habilidad}>
            <div>
              <label htmlFor={`h-hab-${indice}`} className={comunes.oculto}>
                Habilidad {indice + 1}
              </label>
              <select
                id={`h-hab-${indice}`}
                aria-invalid={!!errorFila?.habilidadId}
                aria-describedby={errorFila?.habilidadId ? `h-hab-${indice}-error` : undefined}
                {...register(`habilidades.${indice}.habilidadId`)}
              >
                <option value="">Elige una habilidad</option>
                <OpcionesHabilidades catalogo={catalogo} />
              </select>
              {errorFila?.habilidadId && (
                <p id={`h-hab-${indice}-error`} className={formulario.errorCampo}>
                  {errorFila.habilidadId.message}
                </p>
              )}
            </div>

            <div>
              <label htmlFor={`h-nivel-${indice}`} className={comunes.oculto}>
                Tu nivel en la habilidad {indice + 1}
              </label>
              <select id={`h-nivel-${indice}`} {...register(`habilidades.${indice}.nivel`)}>
                {Object.entries(TEXTO_NIVEL).map(([valor, texto]) => (
                  <option key={valor} value={valor}>
                    {texto}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="boton boton-secundario"
              onClick={() => filas.remove(indice)}
              aria-label={`Quitar la habilidad ${indice + 1}`}
            >
              Quitar
            </button>
          </div>
        )
      })}

      {errorLista && (
        <p className={formulario.errorCampo} role="alert">
          {errorLista}
        </p>
      )}

      <div>
        <button
          type="button"
          className="boton boton-secundario"
          onClick={() => filas.append({ habilidadId: '', nivel: 'BASICO' })}
          disabled={filas.fields.length >= MAXIMO_HABILIDADES}
        >
          Agregar habilidad
        </button>
      </div>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}
      {guardado && !isDirty && (
        <p className={comunes.exito} role="status">
          Habilidades guardadas. Tu compatibilidad con las vacantes ya está actualizada.
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <div className={comunes.acciones}>
        <button type="submit" className="boton" disabled={mutacion.isPending || !isDirty}>
          {mutacion.isPending ? 'Guardando…' : 'Guardar habilidades'}
        </button>
      </div>
    </form>
  )
}

function FormularioDatos({ perfil }: { perfil: CandidatoPerfil }) {
  const queryClient = useQueryClient()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardado, setGuardado] = useState(false)
  const {
    register,
    control,
    handleSubmit,
    setValue,
    setError,
    reset,
    formState: { errors, isDirty },
  } = useForm<DatosPersonales>({ resolver: zodResolver(esquemaDatos), defaultValues: desdePerfil(perfil) })
  const esEstudiante = useWatch({ control, name: 'estadoAcademico' }) === 'ESTUDIANTE'

  const mutacion = useMutation({
    mutationFn: (datos: DatosPersonales) =>
      actualizarMiPerfilCandidato({
        nombres: datos.nombres.trim(),
        apellidos: datos.apellidos.trim(),
        estadoAcademico: datos.estadoAcademico,
        semestre: datos.estadoAcademico === 'ESTUDIANTE' ? Number(datos.semestre) : null,
        universidad: vacioANulo(datos.universidad),
        programa: vacioANulo(datos.programa),
        ciudad: vacioANulo(datos.ciudad),
        githubUrl: vacioANulo(datos.githubUrl),
        resumen: vacioANulo(datos.resumen),
      }),
    onMutate: () => {
      setErrorGeneral(null)
      setGuardado(false)
    },
    onSuccess: (actualizado) => {
      queryClient.setQueryData(CLAVES.perfil, actualizado)
      reset(desdePerfil(actualizado))
      setGuardado(true)
    },
    onError: (error) => {
      if (!aplicarErroresDeValidacion(error, setError, CAMPOS_DATOS)) setErrorGeneral(error.message)
    },
  })

  return (
    <form
      className={`${formulario.formulario} ${estilos.seccion}`}
      onSubmit={handleSubmit((datos) => mutacion.mutate(datos))}
      aria-labelledby="titulo-datos"
      noValidate
    >
      <h2 id="titulo-datos">Datos personales</h2>

      <div className={comunes.fila}>
        <Campo id="p-nombres" etiqueta="Nombres" error={errors.nombres?.message}>
          <input
            id="p-nombres"
            autoComplete="given-name"
            aria-invalid={!!errors.nombres}
            aria-describedby={idsDescripcion('p-nombres', errors.nombres?.message)}
            {...register('nombres')}
          />
        </Campo>

        <Campo id="p-apellidos" etiqueta="Apellidos" error={errors.apellidos?.message}>
          <input
            id="p-apellidos"
            autoComplete="family-name"
            aria-invalid={!!errors.apellidos}
            aria-describedby={idsDescripcion('p-apellidos', errors.apellidos?.message)}
            {...register('apellidos')}
          />
        </Campo>
      </div>

      <fieldset
        className={formulario.grupoOpciones}
        aria-invalid={!!errors.estadoAcademico}
        aria-describedby={idsDescripcion('p-estado', errors.estadoAcademico?.message)}
      >
        <legend>Estado académico</legend>
        <label className={formulario.opcion}>
          <input type="radio" value="ESTUDIANTE" {...register('estadoAcademico')} />
          {TEXTO_ESTADO_ACADEMICO.ESTUDIANTE}
        </label>
        <label className={formulario.opcion}>
          <input
            type="radio"
            value="EGRESADO"
            {...register('estadoAcademico', { onChange: () => setValue('semestre', '') })}
          />
          {TEXTO_ESTADO_ACADEMICO.EGRESADO}
        </label>
        {errors.estadoAcademico && (
          <p id="p-estado-error" className={formulario.errorCampo}>
            {errors.estadoAcademico.message}
          </p>
        )}
      </fieldset>

      {esEstudiante && (
        <Campo id="p-semestre" etiqueta="Semestre que cursas" error={errors.semestre?.message}>
          <select
            id="p-semestre"
            aria-invalid={!!errors.semestre}
            aria-describedby={idsDescripcion('p-semestre', errors.semestre?.message)}
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

      <div className={comunes.fila}>
        <Campo id="p-universidad" etiqueta="Universidad" opcional error={errors.universidad?.message}>
          <input
            id="p-universidad"
            autoComplete="organization"
            aria-invalid={!!errors.universidad}
            aria-describedby={idsDescripcion('p-universidad', errors.universidad?.message)}
            {...register('universidad')}
          />
        </Campo>

        <Campo id="p-programa" etiqueta="Programa" opcional error={errors.programa?.message}>
          <input
            id="p-programa"
            aria-invalid={!!errors.programa}
            aria-describedby={idsDescripcion('p-programa', errors.programa?.message)}
            {...register('programa')}
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
      </div>

      <Campo id="p-github" etiqueta="Perfil de GitHub" opcional error={errors.githubUrl?.message}>
        <input
          id="p-github"
          type="url"
          placeholder="https://github.com/tu-usuario"
          aria-invalid={!!errors.githubUrl}
          aria-describedby={idsDescripcion('p-github', errors.githubUrl?.message)}
          {...register('githubUrl')}
        />
      </Campo>

      <Campo
        id="p-resumen"
        etiqueta="Sobre ti"
        opcional
        error={errors.resumen?.message}
        ayuda="Qué te interesa y en qué proyectos has trabajado. Máximo 1000 caracteres."
      >
        <textarea
          id="p-resumen"
          rows={5}
          aria-invalid={!!errors.resumen}
          aria-describedby={idsDescripcion('p-resumen', errors.resumen?.message, 'ayuda')}
          {...register('resumen')}
        />
      </Campo>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}
      {guardado && !isDirty && (
        <p className={comunes.exito} role="status">
          Cambios guardados.
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <div className={comunes.acciones}>
        <button type="submit" className="boton" disabled={mutacion.isPending || !isDirty}>
          {mutacion.isPending ? 'Guardando…' : 'Guardar datos'}
        </button>
      </div>
    </form>
  )
}
