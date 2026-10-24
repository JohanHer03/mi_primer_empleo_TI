import { createContext } from 'react'
import type { Sesion } from './sesion'

export interface ValorAuth {
  sesion: Sesion | null
  /** Guarda el token recibido del login. Devuelve la sesión o null si el token no es válido. */
  iniciarSesion: (token: string) => Sesion | null
  cerrarSesion: () => void
}

export const ContextoAuth = createContext<ValorAuth | null>(null)
