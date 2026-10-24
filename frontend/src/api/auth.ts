import { peticion, usarMocks } from './cliente'
import { loginMock } from './mocks/auth'
import type { LoginPeticion, LoginRespuesta } from './tipos'

export function login(datos: LoginPeticion): Promise<LoginRespuesta> {
  if (usarMocks) return loginMock(datos)
  return peticion<LoginRespuesta>('/auth/login', { metodo: 'POST', cuerpo: datos })
}
