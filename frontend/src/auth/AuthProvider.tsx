import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { configurarSesionCliente } from '../api/cliente'
import { ContextoAuth, type ValorAuth } from './contextoAuth'
import {
  borrarToken,
  guardarToken,
  leerSesionGuardada,
  obtenerTokenActual,
  sesionDesdeToken,
  type Sesion,
} from './sesion'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [sesion, setSesion] = useState<Sesion | null>(leerSesionGuardada)

  const cerrarSesion = useCallback(() => {
    borrarToken()
    setSesion(null)
    // Los datos en caché eran del usuario anterior
    queryClient.clear()
  }, [queryClient])

  const iniciarSesion = useCallback((token: string) => {
    const nueva = sesionDesdeToken(token)
    if (!nueva) return null
    guardarToken(token)
    setSesion(nueva)
    return nueva
  }, [])

  // El cliente HTTP toma el token de aquí y cierra la sesión si el backend responde 401
  useEffect(() => {
    configurarSesionCliente(obtenerTokenActual, cerrarSesion)
  }, [cerrarSesion])

  // Cierra la sesión cuando el token vence aunque el usuario no haga peticiones
  useEffect(() => {
    if (!sesion) return
    const restante = sesion.expiraEn - Date.now()
    const temporizador = setTimeout(cerrarSesion, Math.max(restante, 0))
    return () => clearTimeout(temporizador)
  }, [sesion, cerrarSesion])

  const valor = useMemo<ValorAuth>(
    () => ({ sesion, iniciarSesion, cerrarSesion }),
    [sesion, iniciarSesion, cerrarSesion],
  )

  return <ContextoAuth.Provider value={valor}>{children}</ContextoAuth.Provider>
}
