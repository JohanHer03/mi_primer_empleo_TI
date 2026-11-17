import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { ErrorApi } from '../../api/cliente'
import type { CompatibilidadDetalle, HabilidadEvaluada, VacanteDetalle } from '../../api/tipos'
import { obtenerCompatibilidad, obtenerVacante } from '../../api/vacantes'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import {
  estadoVisible,
  formatearCompatibilidad,
  formatearFecha,
  formatearSalario,
  TEXTO_ESTADO_POSTULACION,
  TEXTO_MODALIDAD,
  TEXTO_NIVEL,
  TEXTO_TIPO,
} from '../../components/formato'
import comunes from '../../components/Paginas.module.css'
import { CLAVES } from './consultas'
import { IndicadorCompatibilidad } from './IndicadorCompatibilidad'
import estilos from './PaginasCandidato.module.css'
import { usePostularse } from './usePostularse'

/**
 * HU-04: detalle de la vacante con la compatibilidad de hoy, "lo que te falta" y lo que ya cumple
 * (GET /api/vacantes/{id} y GET /api/vacantes/{id}/compatibilidad, contrato 6.1 y 6.2).
 */
export default function DetalleVacante() {
  const { id = '' } = useParams()
  // Dos peticiones en paralelo: los datos de la vacante no dependen del candidato
  const vacante = useQuery({ queryKey: ['vacantes', 'detalle', id], queryFn: () => obtenerVacante(id) })
  // Bajo la clave de la búsqueda: se recalcula al cambiar las habilidades o al postularse
  const compatibilidad = useQuery({
    queryKey: [...CLAVES.busqueda, 'compatibilidad', id],
    queryFn: () => obtenerCompatibilidad(id),
  })

  const noExiste = [vacante.error, compatibilidad.error].some((e) => e instanceof ErrorApi && e.status === 404)

  return (
    <section className={comunes.pagina}>
      <Link to="/candidato/vacantes" className={estilos.volver}>
        ← Volver a la búsqueda
      </Link>

      {noExiste ? (
        <EstadoVacio
          titulo="Esta vacante no existe"
          descripcion="Puede que el enlace esté incompleto."
          accion={
            <Link to="/candidato/vacantes" className="boton">
              Buscar vacantes
            </Link>
          }
        />
      ) : (
        <>
          {(vacante.isPending || compatibilidad.isPending) && <Cargando texto="Cargando la vacante…" />}
          {vacante.isError && <MensajeError error={vacante.error} alReintentar={() => vacante.refetch()} />}
          {compatibilidad.isError && (
            <MensajeError error={compatibilidad.error} alReintentar={() => compatibilidad.refetch()} />
          )}
          {vacante.isSuccess && compatibilidad.isSuccess && (
            <Detalle vacante={vacante.data} compatibilidad={compatibilidad.data} />
          )}
        </>
      )}
    </section>
  )
}

function Detalle({ vacante, compatibilidad }: { vacante: VacanteDetalle; compatibilidad: CompatibilidadDetalle }) {
  const visible = estadoVisible(vacante.estado, vacante.fechaCierre)
  const salario = formatearSalario(vacante.salarioMin, vacante.salarioMax)
  const faltan = compatibilidad.habilidades.filter((h) => !h.cumple)
  const cumple = compatibilidad.habilidades.filter((h) => h.cumple)

  return (
    <>
      <header className={estilos.cabeceraDetalle}>
        <div className={comunes.tarjetaDatos}>
          <h1>{vacante.titulo}</h1>
          <p className={estilos.empresa}>{vacante.empresa.razonSocial}</p>
          <p className={comunes.detalles}>
            {TEXTO_TIPO[vacante.tipo]} · {TEXTO_MODALIDAD[vacante.modalidad]} · {vacante.ciudad}
            {salario && ` · ${salario}`}
          </p>
          <p className={comunes.detalles}>
            Publicada el {formatearFecha(vacante.fechaPublicacion)} ·{' '}
            {visible === 'ABIERTA' && `Cierra el ${formatearFecha(vacante.fechaCierre)}`}
            {visible === 'VENCIDA' && `Cerró el ${formatearFecha(vacante.fechaCierre)}`}
            {visible === 'CERRADA' && 'La empresa la cerró'}
          </p>
        </div>
        <IndicadorCompatibilidad valor={compatibilidad.porcentaje} texto="compatible hoy" />
      </header>

      <Postulacion vacante={vacante} compatibilidad={compatibilidad} recibe={visible === 'ABIERTA'} />

      <section className={estilos.bloque} aria-labelledby="titulo-falta">
        <h2 id="titulo-falta">Lo que te falta</h2>
        {faltan.length === 0 ? (
          <p className={comunes.exito}>Cumples todas las habilidades que pide esta vacante.</p>
        ) : (
          <>
            <p className={formulario.ayuda}>
              {faltan.length === 1 ? 'Una habilidad' : `${faltan.length} habilidades`} por debajo de lo que pide la
              empresa. Las obligatorias pesan el doble en tu compatibilidad.
            </p>
            <ul className={estilos.listaHabilidades}>
              {faltan.map((habilidad) => (
                <FilaHabilidad key={habilidad.habilidadId} habilidad={habilidad} />
              ))}
            </ul>
            <p className={formulario.nota}>
              ¿Ya mejoraste en alguna? <Link to="/candidato/perfil">Actualiza tus habilidades</Link> y tu compatibilidad
              se recalcula.
            </p>
          </>
        )}
      </section>

      {cumple.length > 0 && (
        <section className={estilos.bloque} aria-labelledby="titulo-cumple">
          <h2 id="titulo-cumple">Lo que ya cumples</h2>
          <ul className={estilos.listaHabilidades}>
            {cumple.map((habilidad) => (
              <FilaHabilidad key={habilidad.habilidadId} habilidad={habilidad} />
            ))}
          </ul>
        </section>
      )}

      <section className={estilos.bloque} aria-labelledby="titulo-descripcion">
        <h2 id="titulo-descripcion">Descripción</h2>
        <p className={estilos.descripcion}>{vacante.descripcion}</p>
      </section>
    </>
  )
}

function FilaHabilidad({ habilidad }: { habilidad: HabilidadEvaluada }) {
  return (
    <li className={`${estilos.filaHabilidad} ${habilidad.cumple ? estilos.cumplida : estilos.faltante}`}>
      <span className={estilos.marca} aria-hidden="true">
        {habilidad.cumple ? '✓' : '✗'}
      </span>
      <span className={estilos.nombreHabilidad}>
        {habilidad.nombre}
        <span className={estilos.etiqueta}>{habilidad.obligatoria ? 'Obligatoria' : 'Deseable'}</span>
      </span>
      <span className={comunes.detalles}>
        Piden {TEXTO_NIVEL[habilidad.nivelRequerido].toLowerCase()} ·{' '}
        {habilidad.nivelCandidato ? `tienes ${TEXTO_NIVEL[habilidad.nivelCandidato].toLowerCase()}` : 'no la tienes'}
      </span>
    </li>
  )
}

interface PropsPostulacion {
  vacante: VacanteDetalle
  compatibilidad: CompatibilidadDetalle
  recibe: boolean
}

/** Estado de la postulación o el botón para postularse, según la regla 2 y si ya se postuló. */
function Postulacion({ vacante, compatibilidad, recibe }: PropsPostulacion) {
  const { confirmar, enviando, enviada, error, rechazada } = usePostularse({
    id: vacante.id,
    titulo: vacante.titulo,
    razonSocialEmpresa: vacante.empresa.razonSocial,
    compatibilidad: compatibilidad.porcentaje,
  })
  const { postulacion } = compatibilidad

  if (postulacion) {
    const cambio = postulacion.compatibilidad !== compatibilidad.porcentaje
    return (
      <div className={comunes.mensajeExito} role="status">
        <p className={estilos.textoPostulacion}>
          Te postulaste el {formatearFecha(postulacion.fecha)} con {formatearCompatibilidad(postulacion.compatibilidad)} de
          compatibilidad. Estado: <strong>{TEXTO_ESTADO_POSTULACION[postulacion.estado]}</strong>.
        </p>
        {cambio && (
          <p className={`${estilos.textoPostulacion} ${comunes.detalles}`}>
            Hoy tu compatibilidad es {formatearCompatibilidad(compatibilidad.porcentaje)}; la empresa ve la que tenías al
            postularte.
          </p>
        )}
        <p className={estilos.textoPostulacion}>
          <Link to="/candidato/postulaciones">Ver mis postulaciones</Link>
        </p>
      </div>
    )
  }

  if (!recibe) {
    return <p className={formulario.aviso}>Esta vacante ya no recibe postulaciones.</p>
  }

  return (
    <div className={estilos.accionDetalle}>
      {!rechazada && !enviada && (
        <button type="button" className="boton" onClick={confirmar} disabled={enviando}>
          {enviando ? 'Enviando…' : 'Postularme'}
        </button>
      )}
      {compatibilidad.habilidades.some((h) => !h.cumple) && !enviada && !rechazada && (
        <p className={formulario.nota}>Te puedes postular aunque te falte algo: la empresa verá tu compatibilidad.</p>
      )}
      {error && (
        <p className={formulario.errorCampo} role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
