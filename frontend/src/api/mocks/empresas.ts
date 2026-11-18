import { ErrorApi } from '../cliente'
import type {
  ActualizarEmpresaPeticion,
  EmpresaPerfil,
  EmpresaResumen,
  PaginaDto,
  RegistroEmpresaPeticion,
} from '../tipos'
import { esperar, guardar, leerGuardado, usuarioActualMock } from './utilidades'

// Simulación de GET y PUT /api/empresas/me (contrato 4.2). Los perfiles se guardan por id de usuario.
// La empresa de demostración (empresa@demo.co) ya está verificada; las registradas en /registro no,
// así se puede probar el aviso y el 403 de la regla 3. Para el administrador hay además otras cuatro empresas sin cuenta
// de demostración: dos verificadas (las de las vacantes de la búsqueda) y dos por verificar.

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

type Perfiles = Record<string, EmpresaPerfil & { fechaCreacion?: string }>

const CLAVE_VERIFICADAS = 'primer-empleo.mock.empresas-verificadas'

/** Empresas de demostración sin cuenta, solo para la lista del administrador (contrato 3.1). */
const OTRAS_EMPRESAS: EmpresaResumen[] = [
  otra('55555555-5555-5555-5555-555555555556', 'Nodo Software S.A.S.', '9007654321', 'Medellín', true, '2026-10-25'),
  otra('55555555-5555-5555-5555-555555555557', 'Datos del Valle de Aburrá S.A.S.', '9011122233', 'Envigado', true, '2026-10-26'),
  otra('55555555-5555-5555-5555-555555555558', 'Innovación Antioquia S.A.S.', '9013344556', 'Medellín', false, '2026-11-12'),
  otra('55555555-5555-5555-5555-555555555559', 'Café Digital Labs S.A.S.', '9015566778', 'Rionegro', false, '2026-11-15'),
]

function otra(id: string, razonSocial: string, nit: string, ciudad: string, verificada: boolean, dia: string): EmpresaResumen {
  // Sin tildes: "Café Digital" → cafe.co
  const dominio = razonSocial.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').split(' ')[0]
  return {
    id,
    razonSocial,
    nit,
    email: `talento@${dominio}.co`,
    sector: 'Desarrollo de software',
    ciudad,
    sitioWeb: `https://${dominio}.co`,
    verificada,
    fechaCreacion: `${dia}T15:00:00Z`,
  }
}

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
      fechaCreacion: new Date().toISOString(),
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

/** Todas las empresas para el administrador, con las verificaciones hechas en el modo simulado. */
function todasLasEmpresas(): EmpresaResumen[] {
  const verificadas = new Set(leerGuardado<string[]>(CLAVE_VERIFICADAS, () => []))
  const registradas = Object.values(leerPerfiles()).map(({ fechaCreacion, ...perfil }) => ({
    ...perfil,
    fechaCreacion: fechaCreacion ?? '2026-11-01T15:00:00Z',
  }))
  return [{ ...EMPRESA_DEMO, fechaCreacion: '2026-10-24T15:00:00Z' }, ...OTRAS_EMPRESAS, ...registradas]
    .map((empresa) => ({ ...empresa, verificada: empresa.verificada || verificadas.has(empresa.id) }))
    .sort((a, b) => a.fechaCreacion.localeCompare(b.fechaCreacion))
}

function soloAdministrador() {
  if (usuarioActualMock().rol !== 'ADMIN') throw new ErrorApi(403, 'No tienes permiso para realizar esta acción')
}

export async function listarEmpresasMock(
  verificada: boolean | undefined,
  pagina: number,
  tamano: number,
): Promise<PaginaDto<EmpresaResumen>> {
  await esperar()
  soloAdministrador()
  const filtradas = todasLasEmpresas().filter((e) => verificada === undefined || e.verificada === verificada)
  return {
    contenido: filtradas.slice(pagina * tamano, (pagina + 1) * tamano),
    pagina,
    tamano,
    totalElementos: filtradas.length,
    totalPaginas: Math.ceil(filtradas.length / tamano),
  }
}

/** Idempotente, como el backend (contrato 4.3). */
export async function verificarEmpresaMock(id: string): Promise<EmpresaResumen> {
  await esperar()
  soloAdministrador()
  const empresa = todasLasEmpresas().find((e) => e.id === id)
  if (!empresa) throw new ErrorApi(404, 'La empresa no existe')
  const perfiles = leerPerfiles()
  const usuario = Object.keys(perfiles).find((clave) => perfiles[clave].id === id)
  if (usuario) {
    // Las registradas guardan el perfil: así la empresa ve que ya puede publicar
    guardar(CLAVE_EMPRESAS, { ...perfiles, [usuario]: { ...perfiles[usuario], verificada: true } })
  } else {
    guardar(CLAVE_VERIFICADAS, [...new Set([...leerGuardado<string[]>(CLAVE_VERIFICADAS, () => []), id])])
  }
  return { ...empresa, verificada: true }
}
