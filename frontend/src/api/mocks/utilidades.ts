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
