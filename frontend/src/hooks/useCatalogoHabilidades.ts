import { useQuery } from '@tanstack/react-query'
import { listarHabilidades } from '../api/habilidades'

export const CLAVE_CATALOGO = ['habilidades'] as const

/** El catálogo casi no cambia: se reutiliza durante la sesión sin volver a pedirlo (empresa y candidato). */
export function useCatalogoHabilidades() {
  return useQuery({ queryKey: CLAVE_CATALOGO, queryFn: () => listarHabilidades(), staleTime: Infinity })
}
