import { Navigate } from 'react-router'
import { INICIO_POR_ROL } from './rutasPorRol'
import { useAuth } from './useAuth'

/** "/" lleva al inicio del rol con sesión, o al login sin ella. */
export function RedireccionInicio() {
  const { sesion } = useAuth()
  return <Navigate to={sesion ? INICIO_POR_ROL[sesion.rol] : '/login'} replace />
}
