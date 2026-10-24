import { Outlet } from 'react-router'
import estilos from './LayoutPublico.module.css'

export function LayoutPublico() {
  return (
    <main className={estilos.contenedor}>
      <div className={estilos.tarjeta}>
        <p className={estilos.marca}>Primer Empleo TI · Medellín</p>
        <Outlet />
      </div>
    </main>
  )
}
