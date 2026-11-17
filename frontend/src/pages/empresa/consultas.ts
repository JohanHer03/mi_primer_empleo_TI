import { useQuery } from '@tanstack/react-query'
import { obtenerMiPerfil } from '../../api/empresas'

// Claves de TanStack Query de las pantallas de empresa, para leer e invalidar siempre las mismas.
// El catálogo de habilidades está en hooks/useCatalogoHabilidades (lo usa también el candidato).

export const CLAVES = {
  perfil: ['empresa', 'perfil'] as const,
  misVacantes: ['vacantes', 'mias'] as const,
  vacante: (id: string) => ['vacantes', 'detalle', id] as const,
}

export function useMiPerfil() {
  return useQuery({ queryKey: CLAVES.perfil, queryFn: obtenerMiPerfil })
}
