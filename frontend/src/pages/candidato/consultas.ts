import { useQuery } from '@tanstack/react-query'
import { obtenerMiPerfilCandidato } from '../../api/candidatos'

// Claves de TanStack Query de las pantallas del candidato, para leer e invalidar siempre las mismas.

export const CLAVES = {
  perfil: ['candidato', 'perfil'] as const,
  /** La compatibilidad depende de las habilidades del candidato: se invalida al cambiarlas. */
  busqueda: ['vacantes', 'busqueda'] as const,
  misPostulaciones: ['postulaciones', 'mias'] as const,
}

export function useMiPerfilCandidato() {
  return useQuery({ queryKey: CLAVES.perfil, queryFn: obtenerMiPerfilCandidato })
}
