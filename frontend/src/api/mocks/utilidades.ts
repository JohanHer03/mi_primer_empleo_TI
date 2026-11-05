import { obtenerTokenActual, sesionDesdeToken, type Sesion } from '../../auth/sesion'
import { ErrorApi } from '../cliente'

const LATENCIA_POR_DEFECTO_MS = 400
const CLAVE_LATENCIA = 'primer-empleo.mock.latencia'

/**
 * Simula la latencia de red para que se vean los estados de carga.
 * Para probar la espera del arranque en frío, en la consola del navegador:
 * localStorage.setItem('primer-empleo.mock.latencia', '9000')
 */
export function esperar(ms = leerLatencia()): Promise<void> {
  return new Promise((resolver) => setTimeout(resolver, ms))
}

function leerLatencia(): number {
  try {
    const valor = Number(localStorage.getItem(CLAVE_LATENCIA))
    return Number.isFinite(valor) && valor > 0 ? valor : LATENCIA_POR_DEFECTO_MS
  } catch {
    return LATENCIA_POR_DEFECTO_MS
  }
}

/** Usuario de la sesión simulada, como lo sacaría el backend del token (401 si no hay sesión). */
export function usuarioActualMock(): Sesion {
  const token = obtenerTokenActual()
  const sesion = token ? sesionDesdeToken(token) : null
  if (!sesion) throw new ErrorApi(401, 'Necesitas iniciar sesión')
  return sesion
}

/** Lee un valor guardado en localStorage; si no hay o está dañado, devuelve el valor inicial. */
export function leerGuardado<T>(clave: string, inicial: () => T): T {
  try {
    const texto = localStorage.getItem(clave)
    if (texto) return JSON.parse(texto) as T
  } catch {
    // Almacenamiento bloqueado o dato dañado
  }
  return inicial()
}

export function guardar(clave: string, valor: unknown) {
  try {
    localStorage.setItem(clave, JSON.stringify(valor))
  } catch {
    // Sin almacenamiento el modo simulado no conserva cambios
  }
}
