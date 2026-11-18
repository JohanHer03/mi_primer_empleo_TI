import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { ErrorApi } from '../../api/cliente'
import { cambiarEstadoPostulacion, listarPostulantes } from '../../api/postulaciones'
import type { EstadoPostulacion, HabilidadRequerida, NivelHabilidad, Postulante, VacanteDetalle } from '../../api/tipos'
import { obtenerVacante } from '../../api/vacantes'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import {
  estadoVisible,
  formatearFecha,
  INSIGNIA_ESTADO_POSTULACION,
  SIGUIENTES_ESTADOS,
  TEXTO_ACCION_ESTADO,
  TEXTO_ESTADO_ACADEMICO,
  TEXTO_ESTADO_POSTULACION,
  TEXTO_NIVEL,
} from '../../components/formato'
import { IndicadorCompatibilidad } from '../../components/IndicadorCompatibilidad'
import { Paginacion } from '../../components/Paginacion'
import comunes from '../../components/Paginas.module.css'
import { CLAVES } from './consultas'
import estilos from './Postulantes.module.css'

const ESTADOS = Object.keys(TEXTO_ESTADO_POSTULACION) as EstadoPostulacion[]
const VALOR_NIVEL: Record<NivelHabilidad, number> = { BASICO: 1, INTERMEDIO: 2, AVANZADO: 3 }
const TEXTO_ESTADO_VACANTE = { ABIERTA: 'Abierta', CERRADA: 'Cerrada', VENCIDA: 'Vencida' }

/** HU-08 y HU-09: postulantes de una vacante propia, ordenados por compatibilidad, y cambio de estado (contrato 7). */
export default function Postulantes() {
  const { id = '' } = useParams()
  const [parametros, setParametros] = useSearchParams()
  const estado = ESTADOS.find((e) => e === parametros.get('estado'))
  const pagina = Math.max(0, Number(parametros.get('pagina')) || 0)

  const vacante = useQuery({ queryKey: CLAVES.vacante(id), queryFn: () => obtenerVacante(id) })
  const postulantes = useQuery({
    queryKey: [...CLAVES.postulantes(id), estado ?? 'TODOS', pagina],
    queryFn: () => listarPostulantes(id, estado, pagina),
    placeholderData: keepPreviousData,
  })

  const cambiar = (nuevoEstado: EstadoPostulacion | undefined, nuevaPagina = 0) => {
    const siguientes = new URLSearchParams()
    if (nuevoEstado) siguientes.set('estado', nuevoEstado)
    if (nuevaPagina > 0) siguientes.set('pagina', String(nuevaPagina))
    setParametros(siguientes)
  }

  // 403 (vacante de otra empresa) o 404: no tiene sentido mostrar filtros
  const sinAcceso = [vacante.error, postulantes.error].find(
    (e) => e instanceof ErrorApi && (e.status === 403 || e.status === 404),
  )

  return (
    <section className={comunes.pagina}>
      <Link to="/empresa/vacantes" className={estilos.volver}>
        ← Volver a mis vacantes
      </Link>

      {sinAcceso ? (
        <EstadoVacio
          titulo="No puedes ver los postulantes de esta vacante"
          descripcion={sinAcceso.message}
          accion={
            <Link to="/empresa/vacantes" className="boton">
              Ver mis vacantes
            </Link>
          }
        />
      ) : (
        <>
          {vacante.isSuccess && <Encabezado vacante={vacante.data} />}
          {vacante.isError && <MensajeError error={vacante.error} alReintentar={() => vacante.refetch()} />}

          <div className={comunes.filtros} role="group" aria-label="Filtrar por estado">
            {[undefined, ...ESTADOS].map((opcion) => (
              <button
                key={opcion ?? 'TODOS'}
                type="button"
                className={comunes.filtro}
                aria-pressed={estado === opcion}
                onClick={() => cambiar(opcion)}
              >
                {opcion ? TEXTO_ESTADO_POSTULACION[opcion] : 'Todos'}
              </button>
            ))}
          </div>

          {postulantes.isPending && <Cargando texto="Cargando postulantes…" />}
          {postulantes.isError && <MensajeError error={postulantes.error} alReintentar={() => postulantes.refetch()} />}
          {postulantes.isSuccess && postulantes.data.contenido.length === 0 && (
            <EstadoVacio
              titulo={estado ? 'Ningún postulante en este estado' : 'Aún no hay postulantes'}
              descripcion={
                estado ? 'Prueba con otro filtro.' : 'Cuando un candidato se postule, aparecerá aquí con su compatibilidad.'
              }
            />
          )}
          {postulantes.isSuccess && postulantes.data.contenido.length > 0 && (
            <>
              <p className={comunes.detalles} role="status">
                {postulantes.data.totalElementos === 1
                  ? '1 postulante'
                  : `${postulantes.data.totalElementos} postulantes`}
                , de mayor a menor compatibilidad al postularse.
              </p>
              <ul className={comunes.lista}>
                {postulantes.data.contenido.map((postulante) => (
                  <TarjetaPostulante
                    key={postulante.id}
                    vacanteId={id}
                    postulante={postulante}
                    requeridas={vacante.data?.habilidades ?? []}
                  />
                ))}
              </ul>
              <Paginacion
                pagina={pagina}
                totalPaginas={postulantes.data.totalPaginas}
                alCambiar={(nueva) => cambiar(estado, nueva)}
              />
            </>
          )}
        </>
      )}
    </section>
  )
}

function Encabezado({ vacante }: { vacante: VacanteDetalle }) {
  const visible = estadoVisible(vacante.estado, vacante.fechaCierre)
  return (
    <div className={comunes.encabezado}>
      <div>
        <h1>Postulantes</h1>
        <p className={comunes.detalles}>
          {vacante.titulo} ·{' '}
          <span className={`${comunes.insignia} ${comunes[`insignia${visible}`]}`}>{TEXTO_ESTADO_VACANTE[visible]}</span> ·
          Cierra el {formatearFecha(vacante.fechaCierre)}
        </p>
      </div>
    </div>
  )
}

interface PropsTarjeta {
  vacanteId: string
  postulante: Postulante
  requeridas: HabilidadRequerida[]
}

function TarjetaPostulante({ vacanteId, postulante, requeridas }: PropsTarjeta) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const { candidato } = postulante
  const nombre = `${candidato.nombres} ${candidato.apellidos}`

  const cambio = useMutation({
    mutationFn: (nuevo: EstadoPostulacion) => cambiarEstadoPostulacion(postulante.id, nuevo),
    onMutate: () => setError(null),
    // El estado nuevo puede sacar la tarjeta del filtro actual: se recarga la lista
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CLAVES.postulantes(vacanteId) }),
    onError: (e) => {
      setError(e.message)
      // 409: otra persona de la empresa ya lo cambió; se recarga para ver el estado real
      if (e instanceof ErrorApi && e.status === 409) {
        return queryClient.invalidateQueries({ queryKey: CLAVES.postulantes(vacanteId) })
      }
    },
  })

  const pasarA = (nuevo: EstadoPostulacion) => {
    // Rechazar y contratar son estados finales (regla 5): no se pueden deshacer
    if (nuevo === 'RECHAZADA' || nuevo === 'CONTRATADO') {
      const pregunta =
        nuevo === 'CONTRATADO'
          ? `¿Contratar a ${nombre}? La postulación queda cerrada y no se puede deshacer.`
          : `¿Rechazar a ${nombre}? La postulación queda cerrada y no se puede deshacer.`
      if (!window.confirm(pregunta)) return
    }
    cambio.mutate(nuevo)
  }

  const siguientes = SIGUIENTES_ESTADOS[postulante.estado]
  const academico =
    candidato.estadoAcademico === 'ESTUDIANTE' && candidato.semestre
      ? `Estudiante de ${candidato.semestre}.º semestre`
      : TEXTO_ESTADO_ACADEMICO[candidato.estadoAcademico]

  return (
    <li className={estilos.tarjeta}>
      <IndicadorCompatibilidad valor={postulante.compatibilidad} texto="al postularse" />
      <div className={estilos.datos}>
        <h2 className={comunes.tarjetaTitulo}>
          {nombre}
          <span className={`${comunes.insignia} ${comunes[INSIGNIA_ESTADO_POSTULACION[postulante.estado]]}`}>
            {TEXTO_ESTADO_POSTULACION[postulante.estado]}
          </span>
        </h2>
        <p className={comunes.detalles}>
          {[academico, candidato.programa, candidato.universidad, candidato.ciudad].filter(Boolean).join(' · ')}
        </p>
        <p className={comunes.detalles}>Se postuló el {formatearFecha(postulante.fecha)}</p>
      </div>

      <details className={estilos.perfil}>
        <summary>Ver perfil y habilidades</summary>
        <PerfilPostulante postulante={postulante} requeridas={requeridas} />
      </details>

      <div className={estilos.acciones}>
        {siguientes.length === 0 ? (
          <span className={estilos.final}>Proceso terminado: ya no cambia de estado.</span>
        ) : (
          siguientes.map((nuevo) => (
            <button
              key={nuevo}
              type="button"
              className={nuevo === 'RECHAZADA' ? 'boton boton-secundario' : 'boton'}
              onClick={() => pasarA(nuevo)}
              disabled={cambio.isPending}
            >
              {cambio.isPending && cambio.variables === nuevo ? 'Guardando…' : TEXTO_ACCION_ESTADO[nuevo]}
            </button>
          ))
        )}
        {error && (
          <p className={formulario.errorCampo} role="alert">
            {error}
          </p>
        )}
      </div>
    </li>
  )
}

function PerfilPostulante({ postulante, requeridas }: { postulante: Postulante; requeridas: HabilidadRequerida[] }) {
  const { candidato } = postulante
  const nivelDe = new Map(candidato.habilidades.map((h) => [h.habilidadId, h.nivel]))
  const idsRequeridas = new Set(requeridas.map((h) => h.habilidadId))
  const otras = candidato.habilidades.filter((h) => !idsRequeridas.has(h.habilidadId))

  return (
    <div className={estilos.contenidoPerfil}>
      <p className={estilos.contacto}>
        <a href={`mailto:${candidato.email}`}>{candidato.email}</a>
        {candidato.githubUrl && (
          <a href={candidato.githubUrl} target="_blank" rel="noreferrer">
            GitHub
          </a>
        )}
      </p>
      {candidato.resumen && <p className={estilos.resumen}>{candidato.resumen}</p>}

      {requeridas.length > 0 && (
        <div>
          <h3 className={estilos.subtitulo}>Frente a lo que pide la vacante (habilidades de hoy)</h3>
          <ul className={estilos.chips}>
            {requeridas.map((requerida) => {
              const nivel = nivelDe.get(requerida.habilidadId)
              const cumple = nivel !== undefined && VALOR_NIVEL[nivel] >= VALOR_NIVEL[requerida.nivelRequerido]
              return (
                <li key={requerida.habilidadId} className={`${estilos.chip} ${cumple ? estilos.chipCumple : estilos.chipFalta}`}>
                  {cumple ? '✓' : '✗'} {requerida.nombre}: {nivel ? TEXTO_NIVEL[nivel].toLowerCase() : 'no la tiene'} (piden{' '}
                  {TEXTO_NIVEL[requerida.nivelRequerido].toLowerCase()}
                  {requerida.obligatoria ? ', obligatoria' : ''})
                </li>
              )
            })}
          </ul>
        </div>
      )}

      {otras.length > 0 && (
        <div>
          <h3 className={estilos.subtitulo}>Otras habilidades</h3>
          <ul className={estilos.chips}>
            {otras.map((habilidad) => (
              <li key={habilidad.habilidadId} className={`${estilos.chip} ${estilos.chipOtra}`}>
                {habilidad.nombre}: {TEXTO_NIVEL[habilidad.nivel].toLowerCase()}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
