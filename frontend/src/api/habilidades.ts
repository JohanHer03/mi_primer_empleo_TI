import { peticion, usarMocks } from './cliente'
import { listarHabilidadesMock } from './mocks/habilidades'
import type { CategoriaHabilidad, Habilidad } from './tipos'

// docs/contrato-api.md, sección 4.1

/** Catálogo completo (sin paginar), ordenado por categoría y nombre. */
export function listarHabilidades(categoria?: CategoriaHabilidad): Promise<Habilidad[]> {
  if (usarMocks) return listarHabilidadesMock(categoria)
  return peticion<Habilidad[]>('/habilidades', { parametros: { categoria } })
}
