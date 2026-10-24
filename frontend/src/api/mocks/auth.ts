import { ErrorApi } from '../cliente'
import type { LoginPeticion, LoginRespuesta, Rol } from '../tipos'
import { esperar } from './utilidades'

// Usuarios de demostración solo para VITE_USAR_MOCKS=true (ver frontend/README.md).
const USUARIOS_DEMO: Record<string, { id: string; rol: Rol }> = {
  'candidato@demo.co': { id: '11111111-1111-1111-1111-111111111111', rol: 'CANDIDATO' },
  'empresa@demo.co': { id: '22222222-2222-2222-2222-222222222222', rol: 'EMPRESA' },
  'admin@demo.co': { id: '33333333-3333-3333-3333-333333333333', rol: 'ADMIN' },
}
const CONTRASENA_DEMO = 'demo1234'

export async function loginMock({ email, password }: LoginPeticion): Promise<LoginRespuesta> {
  await esperar()
  const usuario = USUARIOS_DEMO[email.trim().toLowerCase()]
  if (!usuario || password !== CONTRASENA_DEMO) {
    throw new ErrorApi(401, 'Correo o contraseña incorrectos')
  }
  return { token: crearTokenFalso(usuario.id, email.trim().toLowerCase(), usuario.rol) }
}

/** JWT sin firma válida, con los mismos campos que se esperan del backend (sub, email, rol, exp). */
function crearTokenFalso(id: string, email: string, rol: Rol): string {
  const base64Url = (objeto: object) =>
    btoa(JSON.stringify(objeto)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_')
  const expiracion = Math.floor(Date.now() / 1000) + 2 * 60 * 60
  return `${base64Url({ alg: 'none', typ: 'JWT' })}.${base64Url({ sub: id, email, rol, exp: expiracion })}.mock`
}
