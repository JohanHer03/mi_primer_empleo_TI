import { EstadoVacio } from './Estados'

/** Marcador de las pantallas que se construyen en fases posteriores de docs/plan-frontend.md. */
export function PantallaPendiente({ titulo, historia, fase }: { titulo: string; historia?: string; fase: number }) {
  return (
    <section>
      <h1>{titulo}</h1>
      <EstadoVacio
        titulo="Pantalla en construcción"
        descripcion={`Se implementa en la fase ${fase} del frontend${historia ? ` (${historia})` : ''}.`}
      />
    </section>
  )
}
