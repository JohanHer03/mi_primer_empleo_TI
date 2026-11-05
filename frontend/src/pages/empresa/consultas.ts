import { useQuery } from '@tanstack/react-query'
import { obtenerMiPerfil } from '../../api/empresas'
import { listarHabilidades } from '../../api/habilidades'

// Claves de TanStack Query de las pantallas de empresa, para leer e invalidar siempre las mismas.

export const CLAVES = {
  perfil: ['empresa', 'perfil'] as const,
  habilidades: ['habilidades'] as const,
  misVacantes: ['vacantes', 'mias'] as const,
  vacante: (id: string) => ['vacantes', 'detalle', id] as const,
}

export function useMiPerfil() {
  return useQuery({ queryKey: CLAVES.perfil, queryFn: obtenerMiPerfil })
}

/** El catálogo casi no cambia: se reutiliza durante la sesión sin volver a pedirlo. */
export function useCatalogoHabilidades() {
  return useQuery({ queryKey: CLAVES.habilidades, queryFn: () => listarHabilidades(), staleTime: Infinity })
}
