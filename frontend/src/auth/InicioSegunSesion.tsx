import { Navigate } from 'react-router'
import Landing from '../pages/publico/Landing'
import { INICIO_POR_ROL } from './rutasPorRol'
import { useAuth } from './useAuth'

/** "/": con sesión lleva al inicio del rol; sin sesión muestra la landing. */
export function InicioSegunSesion() {
  const { sesion } = useAuth()
  if (sesion) return <Navigate to={INICIO_POR_ROL[sesion.rol]} replace />
  return <Landing />
}
