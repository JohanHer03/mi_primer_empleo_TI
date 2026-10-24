import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import estilos from '../layouts/LayoutPublico.module.css'

/** Se muestra si una pantalla falla al renderizar, en vez de dejar la página en blanco. */
export function ErrorDeRuta() {
  const error = useRouteError()
  const detalle = isRouteErrorResponse(error) ? `${error.status} ${error.statusText}` : null

  return (
    <main className={estilos.contenedor}>
      <div className={estilos.tarjeta} role="alert">
        <h1>Algo salió mal</h1>
        <p>La página tuvo un problema inesperado. Puedes volver al inicio e intentarlo de nuevo.</p>
        {detalle && <p className={estilos.pie}>{detalle}</p>}
        <Link className="boton" to="/">
          Volver al inicio
        </Link>
      </div>
    </main>
  )
}
