import type { Rol } from '../api/tipos'

// Persistencia del token en localStorage (decisión del 2026-10-23) y lectura de sus datos.

const CLAVE_TOKEN = 'primer-empleo.token'

export interface Sesion {
  token: string
  usuarioId: string
  email: string
  rol: Rol
  /** Expiración en milisegundos desde epoch. */
  expiraEn: number
}

const ROLES: readonly Rol[] = ['CANDIDATO', 'EMPRESA', 'ADMIN']

// Copia en memoria para el cliente HTTP: funciona aunque localStorage esté bloqueado
// y se actualiza en el mismo momento del login, antes de que las pantallas pidan datos.
let tokenActual: string | null = null

export function obtenerTokenActual(): string | null {
  return tokenActual
}

/** Lee los datos del token sin validar la firma: eso lo hace el backend en cada petición. */
export function sesionDesdeToken(token: string): Sesion | null {
  try {
    const [, payload] = token.split('.')
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const datos = JSON.parse(atob(base64)) as { sub?: string; email?: string; rol?: string; exp?: number }
    if (!datos.sub || !datos.rol || !datos.exp || !ROLES.includes(datos.rol as Rol)) return null
    return {
      token,
      usuarioId: datos.sub,
      email: datos.email ?? '',
      rol: datos.rol as Rol,
      expiraEn: datos.exp * 1000,
    }
  } catch {
    return null
  }
}

export function sesionVigente(sesion: Sesion | null): sesion is Sesion {
  return sesion !== null && sesion.expiraEn > Date.now()
}

export function leerSesionGuardada(): Sesion | null {
  try {
    const token = localStorage.getItem(CLAVE_TOKEN)
    const sesion = token ? sesionDesdeToken(token) : null
    if (!sesionVigente(sesion)) return null
    tokenActual = sesion.token
    return sesion
  } catch {
    // localStorage bloqueado (modo privado estricto): la sesión dura solo mientras la pestaña esté abierta
    return null
  }
}

export function guardarToken(token: string) {
  tokenActual = token
  try {
    localStorage.setItem(CLAVE_TOKEN, token)
  } catch {
    // Sin almacenamiento: se mantiene solo en memoria
  }
}

export function borrarToken() {
  tokenActual = null
  try {
    localStorage.removeItem(CLAVE_TOKEN)
  } catch {
    // Nada que borrar
  }
}
