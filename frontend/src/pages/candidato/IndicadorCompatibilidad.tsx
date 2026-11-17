import { formatearCompatibilidad, nivelCompatibilidad } from '../../components/formato'
import estilos from './PaginasCandidato.module.css'

/** Porcentaje de compatibilidad con un color según el rango (alta ≥ 70, media ≥ 40, baja). */
export function IndicadorCompatibilidad({ valor, texto = 'compatible' }: { valor: number; texto?: string }) {
  return (
    <div className={`${estilos.compatibilidad} ${estilos[`compatibilidad${nivelCompatibilidad(valor)}`]}`}>
      <span className={estilos.compatibilidadValor}>{formatearCompatibilidad(valor)}</span>
      <span className={estilos.compatibilidadTexto}>{texto}</span>
    </div>
  )
}
