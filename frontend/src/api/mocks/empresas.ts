import { ErrorApi } from '../cliente'
import type { ActualizarEmpresaPeticion, EmpresaPerfil, RegistroEmpresaPeticion } from '../tipos'
import { esperar, guardar, leerGuardado, usuarioActualMock } from './utilidades'

// Simulación de GET y PUT /api/empresas/me (contrato 4.2). Los perfiles se guardan por id de usuario.
// La empresa de demostración (empresa@demo.co) ya está verificada; las registradas en /registro no,
// así se puede probar el aviso y el 403 de la regla 3.

const CLAVE_EMPRESAS = 'primer-empleo.mock.empresas'
const ID_USUARIO_EMPRESA_DEMO = '22222222-2222-2222-2222-222222222222'

const EMPRESA_DEMO: EmpresaPerfil = {
  id: '55555555-5555-5555-5555-555555555555',
  razonSocial: 'Software Medellín S.A.S.',
  nit: '9001234567',
  email: 'empresa@demo.co',
  sector: 'Desarrollo de software',
  ciudad: 'Medellín',
  sitioWeb: 'https://softwaremedellin.co',
  verificada: true,
}

type Perfiles = Record<string, EmpresaPerfil>

const leerPerfiles = () => leerGuardado<Perfiles>(CLAVE_EMPRESAS, () => ({}))

/** Perfil de la empresa del usuario simulado; también lo usan los mocks de vacantes. */
export function perfilEmpresaActualMock(): EmpresaPerfil {
  const sesion = usuarioActualMock()
  if (sesion.rol !== 'EMPRESA') throw new ErrorApi(403, 'No tienes permiso para realizar esta acción')
  const perfil = leerPerfiles()[sesion.usuarioId] ?? (sesion.usuarioId === ID_USUARIO_EMPRESA_DEMO ? EMPRESA_DEMO : null)
  if (!perfil) throw new ErrorApi(404, 'La empresa no existe')
  return perfil
}

export function registrarPerfilEmpresaMock(usuarioId: string, email: string, datos: RegistroEmpresaPeticion) {
  guardar(CLAVE_EMPRESAS, {
    ...leerPerfiles(),
    [usuarioId]: {
      id: crypto.randomUUID(),
      razonSocial: datos.razonSocial,
      nit: datos.nit,
      email,
      sector: datos.sector,
      ciudad: datos.ciudad,
      sitioWeb: datos.sitioWeb,
      verificada: false,
    },
  })
}

export async function obtenerMiPerfilMock(): Promise<EmpresaPerfil> {
  await esperar()
  return perfilEmpresaActualMock()
}

export async function actualizarMiPerfilMock(datos: ActualizarEmpresaPeticion): Promise<EmpresaPerfil> {
  await esperar()
  const actualizado = { ...perfilEmpresaActualMock(), ...datos }
  guardar(CLAVE_EMPRESAS, { ...leerPerfiles(), [usuarioActualMock().usuarioId]: actualizado })
  return actualizado
}
