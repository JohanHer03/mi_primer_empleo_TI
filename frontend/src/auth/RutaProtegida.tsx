import { Navigate, Outlet, useLocation } from 'react-router'
import type { Rol } from '../api/tipos'
import { destinoTrasLogin, INICIO_POR_ROL } from './rutasPorRol'
import { useAuth } from './useAuth'

/** Sin sesión lleva al login (y vuelve después); con otro rol lleva al inicio de su propio rol. */
export function RutaProtegida({ roles }: { roles: Rol[] }) {
  const { sesion } = useAuth()
  const ubicacion = useLocation()

  if (!sesion) {
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />
  }
  if (!roles.includes(sesion.rol)) {
    return <Navigate to={INICIO_POR_ROL[sesion.rol]} replace />
  }
  return <Outlet />
}

/**
 * Para login y registro: con sesión lleva al inicio del rol o a la página que pidió el login.
 * Es el único lugar que redirige tras iniciar sesión, para no competir con otra navegación.
 */
export function SoloSinSesion() {
  const { sesion } = useAuth()
  const ubicacion = useLocation()
  if (sesion) {
    const desde = (ubicacion.state as { desde?: string } | null)?.desde
    return <Navigate to={destinoTrasLogin(sesion.rol, desde)} replace />
  }
  return <Outlet />
}
