import { Link, Outlet } from 'react-router'
import estilos from './LayoutPublico.module.css'

export function LayoutPublico() {
  return (
    <main className={estilos.contenedor}>
      <div className={estilos.tarjeta}>
        <Link to="/" className={estilos.marca}>
          Primer Empleo TI · Medellín
        </Link>
        <Outlet />
      </div>
    </main>
  )
}
