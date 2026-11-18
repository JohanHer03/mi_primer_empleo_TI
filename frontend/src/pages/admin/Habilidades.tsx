import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { ErrorApi } from '../../api/cliente'
import { crearHabilidad } from '../../api/habilidades'
import type { CategoriaHabilidad, Habilidad } from '../../api/tipos'
import { idsDescripcion } from '../../components/accesibilidad'
import { AvisoDemora } from '../../components/AvisoDemora'
import { Campo } from '../../components/Campo'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import { aplicarErroresDeValidacion } from '../../components/erroresServidor'
import formulario from '../../components/Formulario.module.css'
import { TEXTO_CATEGORIA } from '../../components/formato'
import comunes from '../../components/Paginas.module.css'
import { CLAVE_CATALOGO, useCatalogoHabilidades } from '../../hooks/useCatalogoHabilidades'
import estilos from './PaginasAdmin.module.css'

const CATEGORIAS = Object.keys(TEXTO_CATEGORIA) as CategoriaHabilidad[]

// Validaciones de CrearHabilidadPeticion (contrato 4.1)
const esquema = z.object({
  nombre: z.string().trim().min(1, 'Escribe el nombre').max(100, 'Máximo 100 caracteres'),
  categoria: z.enum(['LENGUAJE', 'FRAMEWORK', 'BASE_DATOS', 'HERRAMIENTA', 'BLANDA'], 'Elige la categoría'),
})

type DatosHabilidad = z.infer<typeof esquema>

/** HU-10: el administrador revisa el catálogo de habilidades y agrega nuevas (GET y POST /api/habilidades). */
export default function Habilidades() {
  const catalogo = useCatalogoHabilidades()

  return (
    <section className={comunes.pagina}>
      <h1>Catálogo de habilidades</h1>
      <FormularioHabilidad />
      {catalogo.isPending && <Cargando texto="Cargando el catálogo…" />}
      {catalogo.isError && <MensajeError error={catalogo.error} alReintentar={() => catalogo.refetch()} />}
      {catalogo.isSuccess && catalogo.data.length === 0 && <EstadoVacio titulo="El catálogo está vacío" />}
      {catalogo.isSuccess && catalogo.data.length > 0 && <Catalogo habilidades={catalogo.data} />}
    </section>
  )
}

function Catalogo({ habilidades }: { habilidades: Habilidad[] }) {
  return (
    <div className={estilos.categorias}>
      <p className={comunes.detalles}>{habilidades.length} habilidades. Candidatos y empresas eligen de esta lista.</p>
      {CATEGORIAS.map((categoria) => {
        const deCategoria = habilidades.filter((h) => h.categoria === categoria)
        return (
          <section key={categoria} className={estilos.categoria} aria-labelledby={`cat-${categoria}`}>
            <h2 id={`cat-${categoria}`}>
              {TEXTO_CATEGORIA[categoria]} <span className={comunes.detalles}>({deCategoria.length})</span>
            </h2>
            {deCategoria.length === 0 ? (
              <p className={comunes.detalles}>Sin habilidades en esta categoría.</p>
            ) : (
              <ul className={estilos.chips}>
                {deCategoria.map((habilidad) => (
                  <li key={habilidad.id} className={estilos.chip}>
                    {habilidad.nombre}
                  </li>
                ))}
              </ul>
            )}
          </section>
        )
      })}
    </div>
  )
}

function FormularioHabilidad() {
  const queryClient = useQueryClient()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [creada, setCreada] = useState<Habilidad | null>(null)
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors },
  } = useForm<DatosHabilidad>({ resolver: zodResolver(esquema), defaultValues: { nombre: '', categoria: 'LENGUAJE' } })

  const mutacion = useMutation({
    mutationFn: (datos: DatosHabilidad) => crearHabilidad({ nombre: datos.nombre.trim(), categoria: datos.categoria }),
    onMutate: () => {
      setErrorGeneral(null)
      setCreada(null)
    },
    onSuccess: (habilidad, datos) => {
      setCreada(habilidad)
      // Conserva la categoría: suele agregarse más de una del mismo tipo
      reset({ nombre: '', categoria: datos.categoria })
      return queryClient.invalidateQueries({ queryKey: CLAVE_CATALOGO })
    },
    onError: (error) => {
      if (error instanceof ErrorApi && error.status === 409) {
        setError('nombre', { type: 'server', message: error.message })
      } else if (!aplicarErroresDeValidacion(error, setError, ['nombre', 'categoria'])) {
        setErrorGeneral(error.message)
      }
    },
  })

  return (
    <form
      className={`${formulario.formulario} ${estilos.formularioCorto}`}
      onSubmit={handleSubmit((datos) => mutacion.mutate(datos))}
      aria-labelledby="titulo-nueva"
      noValidate
    >
      <h2 id="titulo-nueva">Agregar habilidad</h2>
      <div className={comunes.fila}>
        <Campo id="h-nombre" etiqueta="Nombre" error={errors.nombre?.message} ayuda="No distingue mayúsculas: Kotlin y kotlin son la misma.">
          <input
            id="h-nombre"
            placeholder="Kotlin"
            aria-invalid={!!errors.nombre}
            aria-describedby={idsDescripcion('h-nombre', errors.nombre?.message, 'ayuda')}
            {...register('nombre')}
          />
        </Campo>
        <Campo id="h-categoria" etiqueta="Categoría" error={errors.categoria?.message}>
          <select
            id="h-categoria"
            aria-invalid={!!errors.categoria}
            aria-describedby={idsDescripcion('h-categoria', errors.categoria?.message)}
            {...register('categoria')}
          >
            {CATEGORIAS.map((categoria) => (
              <option key={categoria} value={categoria}>
                {TEXTO_CATEGORIA[categoria]}
              </option>
            ))}
          </select>
        </Campo>
      </div>

      {errorGeneral && (
        <p className={formulario.errorGeneral} role="alert">
          {errorGeneral}
        </p>
      )}
      {creada && (
        <p className={comunes.exito} role="status">
          Agregaste "{creada.nombre}" a {TEXTO_CATEGORIA[creada.categoria].toLowerCase()}.
        </p>
      )}

      <AvisoDemora activo={mutacion.isPending} />

      <div className={comunes.acciones}>
        <button type="submit" className="boton" disabled={mutacion.isPending}>
          {mutacion.isPending ? 'Agregando…' : 'Agregar al catálogo'}
        </button>
      </div>
    </form>
  )
}
