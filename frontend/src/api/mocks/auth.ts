import { ErrorApi } from '../cliente'
import type {
  AuthRespuesta,
  LoginPeticion,
  RegistroCandidatoPeticion,
  RegistroEmpresaPeticion,
  Rol,
} from '../tipos'
import { esperar } from './utilidades'

// Simulación de /api/auth según docs/contrato-api.md, solo para VITE_USAR_MOCKS=true.
// Las cuentas registradas se guardan en localStorage para que el login funcione después de recargar.

interface CuentaMock {
  id: string
  email: string
  password: string
  rol: Rol
  activo: boolean
  nit?: string
}

const CLAVE_CUENTAS = 'primer-empleo.mock.cuentas'
const CONTRASENA_DEMO = 'demo1234'

// Usuarios de demostración (ver frontend/README.md)
const CUENTAS_DEMO: CuentaMock[] = [
  { id: '11111111-1111-1111-1111-111111111111', email: 'candidato@demo.co', password: CONTRASENA_DEMO, rol: 'CANDIDATO', activo: true },
  { id: '22222222-2222-2222-2222-222222222222', email: 'empresa@demo.co', password: CONTRASENA_DEMO, rol: 'EMPRESA', activo: true, nit: '9001234567' },
  { id: '33333333-3333-3333-3333-333333333333', email: 'admin@demo.co', password: CONTRASENA_DEMO, rol: 'ADMIN', activo: true },
  { id: '44444444-4444-4444-4444-444444444444', email: 'inactivo@demo.co', password: CONTRASENA_DEMO, rol: 'CANDIDATO', activo: false },
]

function leerCuentasRegistradas(): CuentaMock[] {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_CUENTAS) ?? '[]') as CuentaMock[]
  } catch {
    return []
  }
}

function guardarCuentaRegistrada(cuenta: CuentaMock) {
  try {
    localStorage.setItem(CLAVE_CUENTAS, JSON.stringify([...leerCuentasRegistradas(), cuenta]))
  } catch {
    // Sin almacenamiento: la cuenta solo vale para la sesión actual
  }
}

function todasLasCuentas(): CuentaMock[] {
  return [...CUENTAS_DEMO, ...leerCuentasRegistradas()]
}

const normalizarEmail = (email: string) => email.trim().toLowerCase()
const normalizarNit = (nit: string) => nit.replace(/[.\-\s]/g, '')

export async function loginMock({ email, password }: LoginPeticion): Promise<AuthRespuesta> {
  await esperar()
  const cuenta = todasLasCuentas().find((c) => c.email === normalizarEmail(email))
  // Mismo mensaje para correo inexistente y contraseña errónea (contrato 2.5)
  if (!cuenta || cuenta.password !== password) {
    throw new ErrorApi(401, 'Correo o contraseña incorrectos')
  }
  if (!cuenta.activo) throw new ErrorApi(403, 'Tu cuenta está desactivada')
  return { token: crearTokenFalso(cuenta) }
}

export async function registrarCandidatoMock(datos: RegistroCandidatoPeticion): Promise<AuthRespuesta> {
  await esperar()
  validarCorreoLibre(datos.email)
  if ((datos.estadoAcademico === 'ESTUDIANTE') !== (datos.semestre !== null)) {
    throw new ErrorApi(400, 'semestre: es obligatorio para estudiantes y debe ir vacío para egresados')
  }
  const cuenta = crearCuenta(datos.email, datos.password, 'CANDIDATO')
  guardarCuentaRegistrada(cuenta)
  return { token: crearTokenFalso(cuenta) }
}

export async function registrarEmpresaMock(datos: RegistroEmpresaPeticion): Promise<AuthRespuesta> {
  await esperar()
  validarCorreoLibre(datos.email)
  const nit = normalizarNit(datos.nit)
  if (!/^[0-9]{6,15}$/.test(nit)) throw new ErrorApi(400, 'nit: debe tener entre 6 y 15 dígitos')
  if (todasLasCuentas().some((c) => c.nit === nit)) {
    throw new ErrorApi(409, 'Ya existe una empresa registrada con ese NIT')
  }
  const cuenta = { ...crearCuenta(datos.email, datos.password, 'EMPRESA'), nit }
  guardarCuentaRegistrada(cuenta)
  return { token: crearTokenFalso(cuenta) }
}

function validarCorreoLibre(email: string) {
  if (todasLasCuentas().some((c) => c.email === normalizarEmail(email))) {
    throw new ErrorApi(409, 'Ya existe una cuenta con ese correo')
  }
}

function crearCuenta(email: string, password: string, rol: Rol): CuentaMock {
  return { id: crypto.randomUUID(), email: normalizarEmail(email), password, rol, activo: true }
}

/** JWT sin firma válida, con los mismos claims que el contrato (sub, email, rol, iat, exp). */
function crearTokenFalso({ id, email, rol }: CuentaMock): string {
  const base64Url = (objeto: object) =>
    btoa(JSON.stringify(objeto)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_')
  const ahora = Math.floor(Date.now() / 1000)
  const payload = { sub: id, email, rol, iat: ahora, exp: ahora + 120 * 60 }
  return `${base64Url({ alg: 'none', typ: 'JWT' })}.${base64Url(payload)}.mock`
}
