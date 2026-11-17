import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router'
import { listarMisPostulaciones } from '../../api/postulaciones'
import type { EstadoPostulacion, PostulacionDeCandidato } from '../../api/tipos'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import { estadoVisible, formatearFecha, TEXTO_ESTADO_POSTULACION, TEXTO_MODALIDAD } from '../../components/formato'
import { Paginacion } from '../../components/Paginacion'
import comunes from '../../components/Paginas.module.css'
import { CLAVES } from './consultas'
import { IndicadorCompatibilidad } from './IndicadorCompatibilidad'
import estilos from './PaginasCandidato.module.css'

const ESTADOS = Object.keys(TEXTO_ESTADO_POSTULACION) as EstadoPostulacion[]

/** Color de la insignia: en curso (azul), contratado (verde), rechazada (gris). */
const INSIGNIA: Record<EstadoPostulacion, string> = {
  ENVIADA: 'insigniaENCURSO',
  EN_REVISION: 'insigniaENCURSO',
  ENTREVISTA: 'insigniaENCURSO',
  CONTRATADO: 'insigniaABIERTA',
  RECHAZADA: 'insigniaCERRADA',
}

/** HU-05: el candidato sigue el estado de sus postulaciones (GET /api/postulaciones/me). */
export default function MisPostulaciones() {
  const [parametros, setParametros] = useSearchParams()
  const estado = ESTADOS.find((e) => e === parametros.get('estado'))
  const pagina = Math.max(0, Number(parametros.get('pagina')) || 0)

  const postulaciones = useQuery({
    queryKey: [...CLAVES.misPostulaciones, estado ?? 'TODAS', pagina],
    queryFn: () => listarMisPostulaciones(estado, pagina),
    placeholderData: keepPreviousData,
  })

  const cambiar = (nuevoEstado: EstadoPostulacion | undefined, nuevaPagina = 0) => {
    const siguientes = new URLSearchParams()
    if (nuevoEstado) siguientes.set('estado', nuevoEstado)
    if (nuevaPagina > 0) siguientes.set('pagina', String(nuevaPagina))
    setParametros(siguientes)
  }

  return (
    <section className={comunes.pagina}>
      <h1>Mis postulaciones</h1>

      <div className={comunes.filtros} role="group" aria-label="Filtrar por estado">
        {[undefined, ...ESTADOS].map((opcion) => (
          <button
            key={opcion ?? 'TODAS'}
            type="button"
            className={comunes.filtro}
            aria-pressed={estado === opcion}
            onClick={() => cambiar(opcion)}
          >
            {opcion ? TEXTO_ESTADO_POSTULACION[opcion] : 'Todas'}
          </button>
        ))}
      </div>

      {postulaciones.isPending && <Cargando texto="Cargando tus postulaciones…" />}
      {postulaciones.isError && <MensajeError error={postulaciones.error} alReintentar={() => postulaciones.refetch()} />}
      {postulaciones.isSuccess && postulaciones.data.contenido.length === 0 && (
        <EstadoVacio
          titulo={estado ? 'No tienes postulaciones en este estado' : 'Aún no te has postulado a ninguna vacante'}
          descripcion={estado ? 'Prueba con otro filtro.' : 'Busca vacantes a tu alcance y postúlate con un clic.'}
          accion={
            estado ? undefined : (
              <Link to="/candidato/vacantes" className="boton">
                Buscar vacantes
              </Link>
            )
          }
        />
      )}
      {postulaciones.isSuccess && postulaciones.data.contenido.length > 0 && (
        <>
          <ul className={comunes.lista}>
            {postulaciones.data.contenido.map((postulacion) => (
              <TarjetaPostulacion key={postulacion.id} postulacion={postulacion} />
            ))}
          </ul>
          <Paginacion
            pagina={pagina}
            totalPaginas={postulaciones.data.totalPaginas}
            alCambiar={(nueva) => cambiar(estado, nueva)}
          />
        </>
      )}
    </section>
  )
}

function TarjetaPostulacion({ postulacion }: { postulacion: PostulacionDeCandidato }) {
  const { vacante } = postulacion
  const vacanteVisible = estadoVisible(vacante.estado, vacante.fechaCierre)

  return (
    <li className={estilos.tarjeta}>
      <IndicadorCompatibilidad valor={postulacion.compatibilidad} texto="al postularte" />
      <div className={comunes.tarjetaDatos}>
        <h2 className={comunes.tarjetaTitulo}>
          <Link to={`/candidato/vacantes/${vacante.id}`} className={estilos.enlaceTitulo}>
            {vacante.titulo}
          </Link>
          <span className={`${comunes.insignia} ${comunes[INSIGNIA[postulacion.estado]]}`}>
            {TEXTO_ESTADO_POSTULACION[postulacion.estado]}
          </span>
        </h2>
        <p className={estilos.empresa}>{vacante.empresa.razonSocial}</p>
        <p className={comunes.detalles}>
          {TEXTO_MODALIDAD[vacante.modalidad]} · {vacante.ciudad}
        </p>
        <p className={comunes.detalles}>
          Te postulaste el {formatearFecha(postulacion.fecha)}
          {vacanteVisible === 'ABIERTA' && ` · La vacante cierra el ${formatearFecha(vacante.fechaCierre)}`}
          {vacanteVisible === 'CERRADA' && ' · La empresa cerró la vacante'}
          {vacanteVisible === 'VENCIDA' && ' · La vacante ya no recibe postulaciones'}
        </p>
      </div>
    </li>
  )
}
