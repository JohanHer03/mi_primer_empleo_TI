import { useDemoraLarga } from '../hooks/useDemoraLarga'
import formulario from './Formulario.module.css'

/**
 * Explica la espera cuando una petición tarda: en Vercel el backend se apaga tras 5 minutos sin tráfico
 * y la primera petición despierta la JVM (CLAUDE.md, sección Despliegue).
 */
export function AvisoDemora({ activo }: { activo: boolean }) {
  const demorado = useDemoraLarga(activo)
  if (!demorado) return null
  return (
    <p className={formulario.aviso} role="status">
      Estamos despertando el servidor. La primera petición después de un rato puede tardar unos segundos.
    </p>
  )
}
