import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { ErrorApi } from '../../api/cliente'
import type { CategoriaHabilidad, Habilidad, VacanteDetalle, VacantePeticion } from '../../api/tipos'
import { editarVacante, obtenerVacante, publicarVacante } from '../../api/vacantes'
import { idsDescripcion } from '../../components/accesibilidad'
import { AvisoDemora } from '../../components/AvisoDemora'
import { AvisoVerificacion } from '../../components/AvisoVerificacion'
import { Campo } from '../../components/Campo'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import { aplicarErroresDeValidacion } from '../../components/erroresServidor'
import formulario from '../../components/Formulario.module.css'
import { hoyEnColombia, TEXTO_CATEGORIA, TEXTO_MODALIDAD, TEXTO_NIVEL, TEXTO_TIPO } from '../../components/formato'
import { CLAVES, useCatalogoHabilidades, useMiPerfil } from './consultas'
import estilos from './PaginasEmpresa.module.css'

const MAXIMO_HABILIDADES = 15

const salario = z
  .string()
  .trim()
  .refine((valor) => valor === '' || /^[0-9]+$/.test(valor), 'Escribe solo números, sin puntos ni signos')
  .refine((valor) => valor === '' || Number(valor) <= 9_999_999_999, 'Es demasiado alto')

// Validaciones de VacantePeticion (contrato 4.4): Bean Validation del DTO y reglas del dominio
const esquema = z
  .object({
    titulo: z.string().trim().min(1, 'Escribe el título').max(150, 'Máximo 150 caracteres'),
    descripcion: z.string().trim().min(1, 'Describe la vacante').max(5000, 'Máximo 5000 caracteres'),
    tipo: z.enum(['PRACTICA', 'JUNIOR'], 'Elige el tipo de vacante'),
    modalidad: z.enum(['PRESENCIAL', 'HIBRIDA', 'REMOTA'], 'Elige la modalidad'),
    ciudad: z.string().trim().min(1, 'Escribe la ciudad').max(100, 'Máximo 100 caracteres'),
    salarioMin: salario,
    salarioMax: salario,
    fechaCierre: z
      .string()
      .min(1, 'Elige la fecha de cierre')
      .refine((fecha) => fecha >= hoyEnColombia(), 'Debe ser hoy o una fecha posterior'),
    habilidades: z
      .array(
        z.object({
          habilidadId: z.string().min(1, 'Elige una habilidad'),
          nivelRequerido: z.enum(['BASICO', 'INTERMEDIO', 'AVANZADO'], 'Elige el nivel'),
          obligatoria: z.boolean(),
        }),
      )
      .min(1, 'Agrega al menos una habilidad')
      .max(MAXIMO_HABILIDADES, `Máximo ${MAXIMO_HABILIDADES} habilidades`),
  })
  .superRefine((datos, ctx) => {
    if (datos.salarioMin !== '' && datos.salarioMax !== '' && Number(datos.salarioMin) > Number(datos.salarioMax)) {
      ctx.addIssue({ code: 'custom', path: ['salarioMin'], message: 'No puede ser mayor que el salario máximo' })
    }
    const vistas = new Set<string>()
    datos.habilidades.forEach((habilidad, indice) => {
      if (habilidad.habilidadId && vistas.has(habilidad.habilidadId)) {
        ctx.addIssue({ code: 'custom', path: ['habilidades', indice, 'habilidadId'], message: 'Ya la agregaste' })
      }
      vistas.add(habilidad.habilidadId)
    })
    // Sin obligatorias no hay "lo que te falta" en la compatibilidad
    if (datos.habilidades.length > 0 && !datos.habilidades.some((h) => h.obligatoria)) {
      ctx.addIssue({ code: 'custom', path: ['habilidades'], message: 'Marca al menos una habilidad como obligatoria' })
    }
  })

type DatosVacante = z.infer<typeof esquema>

const CAMPOS_SERVIDOR = [
  'titulo',
  'descripcion',
  'tipo',
  'modalidad',
  'ciudad',
  'salarioMin',
  'salarioMax',
  'fechaCierre',
  'habilidades',
] as const

function aPeticion(datos: DatosVacante): VacantePeticion {
  return {
    titulo: datos.titulo.trim(),
    descripcion: datos.descripcion.trim(),
    tipo: datos.tipo,
    modalidad: datos.modalidad,
    ciudad: datos.ciudad.trim(),
    salarioMin: datos.salarioMin === '' ? null : Number(datos.salarioMin),
    salarioMax: datos.salarioMax === '' ? null : Number(datos.salarioMax),
    fechaCierre: datos.fechaCierre,
    habilidades: datos.habilidades,
  }
}

function desdeDetalle(vacante: VacanteDetalle): DatosVacante {
  return {
    titulo: vacante.titulo,
    descripcion: vacante.descripcion,
    tipo: vacante.tipo,
    modalidad: vacante.modalidad,
    ciudad: vacante.ciudad,
    salarioMin: vacante.salarioMin === null ? '' : String(vacante.salarioMin),
    salarioMax: vacante.salarioMax === null ? '' : String(vacante.salarioMax),
    fechaCierre: vacante.fechaCierre,
    habilidades: vacante.habilidades.map(({ habilidadId, nivelRequerido, obligatoria }) => ({
      habilidadId,
      nivelRequerido,
      obligatoria,
    })),
  }
}

/** HU-07: publicar (/empresa/vacantes/nueva) o editar (/empresa/vacantes/:id/editar) una vacante. */
export default function FormularioVacante() {
  const { id } = useParams()
  return id ? <EditarVacante id={id} /> : <NuevaVacante />
}

function NuevaVacante() {
  const perfil = useMiPerfil()
  const catalogo = useCatalogoHabilidades()

  return (
    <section className={estilos.pagina}>
      <h1>Publicar vacante</h1>
      {(perfil.isPending || catalogo.isPending) && <Cargando />}
      {perfil.isError && <MensajeError error={perfil.error} alReintentar={() => perfil.refetch()} />}
      {catalogo.isError && <MensajeError error={catalogo.error} alReintentar={() => catalogo.refetch()} />}
      {perfil.isSuccess && !perfil.data.verificada && (
        <div className={estilos.contenidoFormulario}>
          <AvisoVerificacion />
          <p>
            <Link to="/empresa/perfil">Revisa el perfil de tu empresa</Link>
          </p>
        </div>
      )}
      {perfil.isSuccess && perfil.data.verificada && catalogo.isSuccess && (
        <DatosDeVacante
          catalogo={catalogo.data}
          inicial={{
            titulo: '',
            descripcion: '',
            tipo: 'JUNIOR',
            modalidad: 'PRESENCIAL',
            ciudad: perfil.data.ciudad ?? 'Medellín',
            salarioMin: '',
            salarioMax: '',
            fechaCierre: '',
            habilidades: [{ habilidadId: '', nivelRequerido: 'BASICO', obligatoria: true }],
          }}
        />
      )}
    </section>
  )
}

function EditarVacante({ id }: { id: string }) {
  const vacante = useQuery({ queryKey: CLAVES.vacante(id), queryFn: () => obtenerVacante(id) })
  const catalogo = useCatalogoHabilidades()

  return (
    <section className={estilos.pagina}>
      <h1>Editar vacante</h1>
      {(vacante.isPending || catalogo.isPending) && <Cargando />}
      {vacante.isError && <MensajeError error={vacante.error} alReintentar={() => vacante.refetch()} />}
      {catalogo.isError && <MensajeError error={catalogo.error} alReintentar={() => catalogo.refetch()} />}
      {vacante.isSuccess && vacante.data.estado === 'CERRADA' && (
        <EstadoVacio
          titulo="Esta vacante está cerrada"
          descripcion="Las vacantes cerradas no se pueden editar ni reabrir."
          accion={
            <Link to="/empresa/vacantes" className="boton boton-secundario">
              Volver a mis vacantes
            </Link>
          }
        />
      )}
      {vacante.isSuccess && vacante.data.estado === 'ABIERTA' && catalogo.isSuccess && (
        <DatosDeVacante vacanteId={id} catalogo={catalogo.data} inicial={desdeDetalle(vacante.data)} />
      )}
    </section>
  )
}

interface PropsDatos {
  vacanteId?: string
  catalogo: Habilidad[]
  inicial: DatosVacante
}

function DatosDeVacante({ vacanteId, catalogo, inicial }: PropsDatos) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<DatosVacante>({ resolver: zodResolver(esquema), defaultValues: inicial })
  const habilidades = useFieldArray({ control, name: 'habilidades' })

  const mutacion = useMutation({
    mutationFn: (datos: DatosVacante) =>
      vacanteId ? editarVacante(vacanteId, aPeticion(datos)) : publicarVacante(aPeticion(datos)),
    onMutate: () => setErrorGeneral(null),
    onSuccess: async (vacante) => {
      queryClient.setQueryData(CLAVES.vacante(vacante.id), vacante)
      await queryClient.invalidateQueries({ queryKey: CLAVES.misVacantes })
      navigate('/empresa/vacantes', {
        state: { aviso: vacanteId ? `Guardaste los cambios de "${vacante.titulo}".` : `Publicaste "${vacante.titulo}".` },
      })
      // El formulario es largo: sin esto el listado se abre abajo y no se ve el aviso
      window.scrollTo({ top: 0 })
    },
    onError: (error) => {
      // 403 de la regla 3 o 4 y 409 de vacante cerrada: no son de un campo
      if (error instanceof ErrorApi && error.status !== 400) setErrorGeneral(error.message)
      else if (!aplicarErroresDeValidacion(error, setError, CAMPOS_SERVIDOR)) setErrorGeneral(error.message)
    },
  })

  const porCategoria = Object.entries(TEXTO_CATEGORIA).map(([categoria, texto]) => ({
    texto,
    habilidades: catalogo.filter((h) => h.categoria === (categoria as CategoriaHabilidad)),
  }))
  const errorHabilidades = errors.habilidades?.message ?? errors.habilidades?.root?.message

  return (
    <form
      className={`${formulario.formulario} ${estilos.contenidoFormulario}`}
      onSubmit={handleSubmit((datos) => mutacion.mutate(datos))}
      noValidate
    >
      <Campo id="v-titulo" etiqueta="Título" error={errors.titulo?.message}>
        <input
          id="v-titulo"
          placeholder="Desarrollador Java junior"
          aria-invalid={!!errors.titulo}
          aria-describedby={idsDescripcion('v-titulo', errors.titulo?.message)}
          {...register('titulo')}
        />
      </Campo>

      <Campo
        id="v-descripcion"
        etiqueta="Descripción"
        error={errors.descripcion?.message}
        ayuda="Qué hará la persona, con qué equipo y qué ofrece la empresa."
      >
        <textarea
          id="v-descripcion"
          rows={6}
          aria-invalid={!!errors.descripcion}
          aria-describedby={idsDescripcion('v-descripcion', errors.descripcion?.message, 'ayuda')}
          {...register('descripcion')}
        />
      </Campo>

      <div className={estilos.fila}>
        <Campo id="v-tipo" etiqueta="Tipo" error={errors.tipo?.message}>
          <select
            id="v-tipo"
            aria-invalid={!!errors.tipo}
            aria-describedby={idsDescripcion('v-tipo', errors.tipo?.message)}
            {...register('tipo')}
          >
            {Object.entries(TEXTO_TIPO).map(([valor, texto]) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))}
          </select>
        </Campo>

        <Campo id="v-modalidad" etiqueta="Modalidad" error={errors.modalidad?.message}>
          <select
            id="v-modalidad"
            aria-invalid={!!errors.modalidad}
            aria-describedby={idsDescripcion('v-modalidad', errors.modalidad?.message)}
            {...register('modalidad')}
          >
            {Object.entries(TEXTO_MODALIDAD).map(([valor, texto]) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))}
          </select>
        </Campo>

        <Campo id="v-ciudad" etiqueta="Ciudad" error={errors.ciudad?.message}>
          <input
            id="v-ciudad"
            autoComplete="address-level2"
            aria-invalid={!!errors.ciudad}
            aria-describedby={idsDescripcion('v-ciudad', errors.ciudad?.message)}
            {...register('ciudad')}
          />
        </Campo>
      </div>

      <div className={estilos.fila}>
        <Campo id="v-salario-min" etiqueta="Salario mínimo" opcional error={errors.salarioMin?.message} ayuda="En pesos, sin puntos.">
          <input
            id="v-salario-min"
            inputMode="numeric"
            placeholder="2800000"
            aria-invalid={!!errors.salarioMin}
            aria-describedby={idsDescripcion('v-salario-min', errors.salarioMin?.message, 'ayuda')}
            {...register('salarioMin')}
          />
        </Campo>

        <Campo id="v-salario-max" etiqueta="Salario máximo" opcional error={errors.salarioMax?.message} ayuda="En pesos, sin puntos.">
          <input
            id="v-salario-max"
            inputMode="numeric"
            placeholder="3500000"
            aria-invalid={!!errors.salarioMax}
            aria-describedby={idsDescripcion('v-salario-max', errors.salarioMax?.message, 'ayuda')}
            {...register('salarioMax')}
          />
        </Campo>

        <Campo id="v-cierre" etiqueta="Fecha de cierre" error={errors.fechaCierre?.message}>
          <input
            id="v-cierre"
            type="date"
            min={hoyEnColombia()}
            aria-invalid={!!errors.fechaCierre}
            aria-describedby={idsDescripcion('v-cierre', errors.fechaCierre?.message)}
            {...register('fechaCierre')}
          />
        </Campo>
      </div>

      <fieldset className={estilos.habilidades} aria-describedby={errorHabilidades ? 'v-habilidades-error' : 'v-habilidades-ayuda'}>
        <legend>Habilidades requeridas</legend>
        <p id="v-habilidades-ayuda" className={formulario.ayuda}>
          De 1 a {MAXIMO_HABILIDADES}. Las obligatorias pesan el doble en la compatibilidad de los candidatos y al menos una
          debe serlo.
        </p>

        {habilidades.fields.map((campo, indice) => {
          const errorFila = errors.habilidades?.[indice]
          return (
            <div key={campo.id} className={estilos.habilidad}>
              <div>
                <label htmlFor={`v-hab-${indice}`} className={estilos.oculto}>
                  Habilidad {indice + 1}
                </label>
                <select
                  id={`v-hab-${indice}`}
                  aria-invalid={!!errorFila?.habilidadId}
                  aria-describedby={errorFila?.habilidadId ? `v-hab-${indice}-error` : undefined}
                  {...register(`habilidades.${indice}.habilidadId`)}
                >
                  <option value="">Elige una habilidad</option>
                  {porCategoria.map((grupo) => (
                    <optgroup key={grupo.texto} label={grupo.texto}>
                      {grupo.habilidades.map((habilidad) => (
                        <option key={habilidad.id} value={habilidad.id}>
                          {habilidad.nombre}
                        </option>
                      ))}
                    </optgroup>
                  ))}
                </select>
                {errorFila?.habilidadId && (
                  <p id={`v-hab-${indice}-error`} className={formulario.errorCampo}>
                    {errorFila.habilidadId.message}
                  </p>
                )}
              </div>

              <div>
                <label htmlFor={`v-nivel-${indice}`} className={estilos.oculto}>
                  Nivel requerido de la habilidad {indice + 1}
                </label>
                <select id={`v-nivel-${indice}`} {...register(`habilidades.${indice}.nivelRequerido`)}>
                  {Object.entries(TEXTO_NIVEL).map(([valor, texto]) => (
                    <option key={valor} value={valor}>
                      {texto}
                    </option>
                  ))}
                </select>
              </div>

              <label className={estilos.casilla}>
                <input type="checkbox" {...register(`habilidades.${indice}.obligatoria`)} />
                Obligatoria
              </label>

              <button
                type="button"
                className="boton boton-secundario"
                onClick={() => habilidades.remove(indice)}
                disabled={habilidades.fields.length === 1}
                aria-label={`Quitar la habilidad ${indice + 1}`}
              >
                Quitar
              </button>
            </div>
          )
        })}

        {errorHabilidades && (
          <p id="v-habilidades-error" className={formulario.errorCampo} role="alert">
            {errorHabilidades}
          </p>
        )}

        <div>
          <button
            type="button"
            className="boton boton-secundario"
            onClick={() => habilidades.append({ habilidadId: '', nivelRequerido: 'BASICO', obligatoria: false })}
            disabled={habilidades.fields.length >= MAXIMO_HABILIDADES}
          >
            Agregar habilidad
          </button>
        </div>
      </fieldset>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <div className={estilos.acciones}>
        <button type="submit" className="boton" disabled={mutacion.isPending}>
          {mutacion.isPending ? 'Guardando…' : vacanteId ? 'Guardar cambios' : 'Publicar vacante'}
        </button>
        <Link to="/empresa/vacantes" className="boton boton-secundario">
          Cancelar
        </Link>
      </div>
    </form>
  )
}
