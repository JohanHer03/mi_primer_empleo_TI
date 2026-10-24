/** Simula la latencia de red para que se vean los estados de carga. */
export function esperar(ms = 400): Promise<void> {
  return new Promise((resolver) => setTimeout(resolver, ms))
}
