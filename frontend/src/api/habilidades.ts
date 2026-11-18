import { peticion, usarMocks } from './cliente'
import { crearHabilidadMock, listarHabilidadesMock } from './mocks/habilidades'
import type { CategoriaHabilidad, CrearHabilidadPeticion, Habilidad } from './tipos'

// docs/contrato-api.md, sección 4.1

/** Catálogo completo (sin paginar), ordenado por categoría y nombre. */
export function listarHabilidades(categoria?: CategoriaHabilidad): Promise<Habilidad[]> {
  if (usarMocks) return listarHabilidadesMock(categoria)
  return peticion<Habilidad[]>('/habilidades', { parametros: { categoria } })
}

/** Solo ADMIN (HU-10). 409 si ya existe una con ese nombre, sin distinguir mayúsculas. */
export function crearHabilidad(datos: CrearHabilidadPeticion): Promise<Habilidad> {
  if (usarMocks) return crearHabilidadMock(datos)
  return peticion<Habilidad>('/habilidades', { metodo: 'POST', cuerpo: datos })
}
