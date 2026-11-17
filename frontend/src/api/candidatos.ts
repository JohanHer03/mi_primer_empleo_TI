import { peticion, usarMocks } from './cliente'
import { actualizarMiPerfilCandidatoMock, actualizarMisHabilidadesMock, obtenerMiPerfilCandidatoMock } from './mocks/candidatos'
import type { ActualizarCandidatoPeticion, CandidatoHabilidad, CandidatoHabilidadPeticion, CandidatoPerfil } from './tipos'

// docs/contrato-api.md, secciones 5.1 y 5.2

export function obtenerMiPerfilCandidato(): Promise<CandidatoPerfil> {
  if (usarMocks) return obtenerMiPerfilCandidatoMock()
  return peticion<CandidatoPerfil>('/candidatos/me')
}

export function actualizarMiPerfilCandidato(datos: ActualizarCandidatoPeticion): Promise<CandidatoPerfil> {
  if (usarMocks) return actualizarMiPerfilCandidatoMock(datos)
  return peticion<CandidatoPerfil>('/candidatos/me', { metodo: 'PUT', cuerpo: datos })
}

/** Reemplaza la lista completa (de 0 a 30) y devuelve la guardada, en el orden del catálogo. */
export function actualizarMisHabilidades(habilidades: CandidatoHabilidadPeticion[]): Promise<CandidatoHabilidad[]> {
  if (usarMocks) return actualizarMisHabilidadesMock(habilidades)
  return peticion<CandidatoHabilidad[]>('/candidatos/me/habilidades', { metodo: 'PUT', cuerpo: habilidades })
}
