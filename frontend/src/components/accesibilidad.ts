/** Ids de ayuda y error de un campo para el aria-describedby de su control (ver Campo.tsx). */
export function idsDescripcion(id: string, error?: string, ayuda?: string): string | undefined {
  const ids = [ayuda && `${id}-ayuda`, error && `${id}-error`].filter(Boolean)
  return ids.length ? ids.join(' ') : undefined
}
