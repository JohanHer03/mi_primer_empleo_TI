import { peticion, usarMocks } from './cliente'
import { loginMock, registrarCandidatoMock, registrarEmpresaMock } from './mocks/auth'
import type { AuthRespuesta, LoginPeticion, RegistroCandidatoPeticion, RegistroEmpresaPeticion } from './tipos'

// Endpoints de docs/contrato-api.md, sección 2

export function login(datos: LoginPeticion): Promise<AuthRespuesta> {
  if (usarMocks) return loginMock(datos)
  return peticion<AuthRespuesta>('/auth/login', { metodo: 'POST', cuerpo: datos })
}

export function registrarCandidato(datos: RegistroCandidatoPeticion): Promise<AuthRespuesta> {
  if (usarMocks) return registrarCandidatoMock(datos)
  return peticion<AuthRespuesta>('/auth/registro/candidato', { metodo: 'POST', cuerpo: datos })
}

export function registrarEmpresa(datos: RegistroEmpresaPeticion): Promise<AuthRespuesta> {
  if (usarMocks) return registrarEmpresaMock(datos)
  return peticion<AuthRespuesta>('/auth/registro/empresa', { metodo: 'POST', cuerpo: datos })
}
