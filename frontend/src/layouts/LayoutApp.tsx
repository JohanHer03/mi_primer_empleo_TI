import { Link, NavLink, Outlet } from 'react-router'
import { INICIO_POR_ROL, MENU_POR_ROL } from '../auth/rutasPorRol'
import { useAuth } from '../auth/useAuth'
import estilos from './LayoutApp.module.css'

/** Layout de las rutas autenticadas: menú según el rol y botón de cerrar sesión. */
export function LayoutApp() {
  const { sesion, cerrarSesion } = useAuth()
  if (!sesion) return null

  return (
    <>
      <header className={estilos.encabezado}>
        <nav className={estilos.barra} aria-label="Navegación principal">
          <Link to={INICIO_POR_ROL[sesion.rol]} className={estilos.marca}>
            Primer Empleo TI
          </Link>
          <ul className={estilos.menu}>
            {MENU_POR_ROL[sesion.rol].map((enlace) => (
              <li key={enlace.ruta}>
                <NavLink
                  to={enlace.ruta}
                  end={enlace.ruta === INICIO_POR_ROL[sesion.rol]}
                  className={({ isActive }) => `${estilos.enlace} ${isActive ? estilos.activo : ''}`}
                >
                  {enlace.texto}
                </NavLink>
              </li>
            ))}
          </ul>
          <div className={estilos.usuario}>
            <span>{sesion.email}</span>
            <button type="button" className="boton boton-secundario" onClick={cerrarSesion}>
              Cerrar sesión
            </button>
          </div>
        </nav>
      </header>
      <main className={estilos.contenido}>
        <Outlet />
      </main>
    </>
  )
}
