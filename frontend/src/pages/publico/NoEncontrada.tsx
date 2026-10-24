import { Link } from 'react-router'
import estilos from '../../layouts/LayoutPublico.module.css'

export default function NoEncontrada() {
  return (
    <main className={estilos.contenedor}>
      <div className={estilos.tarjeta}>
        <h1>Página no encontrada</h1>
        <p>La dirección que buscas no existe o cambió de lugar.</p>
        <Link className="boton" to="/">
          Volver al inicio
        </Link>
      </div>
    </main>
  )
}
