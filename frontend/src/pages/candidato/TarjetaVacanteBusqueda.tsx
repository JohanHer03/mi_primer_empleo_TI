import { Link } from 'react-router'
import type { VacanteResumen } from '../../api/tipos'
import formulario from '../../components/Formulario.module.css'
import {
  formatearCompatibilidad,
  formatearFecha,
  formatearSalario,
  TEXTO_MODALIDAD,
  TEXTO_TIPO,
} from '../../components/formato'
import comunes from '../../components/Paginas.module.css'
import { IndicadorCompatibilidad } from './IndicadorCompatibilidad'
import estilos from './PaginasCandidato.module.css'
import { usePostularse } from './usePostularse'

interface PropsTarjeta {
  vacante: VacanteResumen
  /** h3 cuando la lista va dentro de una sección con su propio h2 (inicio). */
  nivelTitulo?: 'h2' | 'h3'
}

/** Vacante de la búsqueda con su compatibilidad, enlace al detalle y el botón "Postularme" (HU-03, HU-04 y HU-05). */
export function TarjetaVacanteBusqueda({ vacante, nivelTitulo: Titulo = 'h2' }: PropsTarjeta) {
  const salario = formatearSalario(vacante.salarioMin, vacante.salarioMax)
  const { confirmar, enviando, enviada, error, rechazada } = usePostularse({
    id: vacante.id,
    titulo: vacante.titulo,
    razonSocialEmpresa: vacante.empresa.razonSocial,
    compatibilidad: vacante.compatibilidad,
  })

  const yaPostulado = vacante.postulado || enviada !== null

  return (
    <li className={estilos.tarjeta}>
      <IndicadorCompatibilidad valor={vacante.compatibilidad} />
      <div className={comunes.tarjetaDatos}>
        <Titulo className={comunes.tarjetaTitulo}>
          <Link to={`/candidato/vacantes/${vacante.id}`} className={estilos.enlaceTitulo}>
            {vacante.titulo}
          </Link>
        </Titulo>
        <p className={estilos.empresa}>{vacante.empresa.razonSocial}</p>
        <p className={comunes.detalles}>
          {TEXTO_TIPO[vacante.tipo]} · {TEXTO_MODALIDAD[vacante.modalidad]} · {vacante.ciudad}
          {salario && ` · ${salario}`}
        </p>
        <p className={comunes.detalles}>
          Publicada el {formatearFecha(vacante.fechaPublicacion)} · Cierra el {formatearFecha(vacante.fechaCierre)}
        </p>
      </div>
      <div className={estilos.accionTarjeta}>
        {yaPostulado ? (
          <span className={estilos.yaPostulado}>Ya te postulaste</span>
        ) : rechazada ? null : (
          <button type="button" className="boton" onClick={confirmar} disabled={enviando}>
            {enviando ? 'Enviando…' : 'Postularme'}
          </button>
        )}
        <Link to={`/candidato/vacantes/${vacante.id}`} className={estilos.enlaceSecundario}>
          {vacante.compatibilidad < 100 ? 'Ver qué te falta' : 'Ver detalle'}
        </Link>
      </div>
      {enviada && (
        <p className={`${comunes.mensajeExito} ${estilos.mensajeTarjeta}`} role="status">
          Te postulaste con {formatearCompatibilidad(enviada.compatibilidad)} de compatibilidad. Sigue su estado en{' '}
          <Link to="/candidato/postulaciones">Mis postulaciones</Link>.
        </p>
      )}
      {error && (
        <p className={`${formulario.errorCampo} ${estilos.mensajeTarjeta}`} role="alert">
          {error}
        </p>
      )}
    </li>
  )
}
