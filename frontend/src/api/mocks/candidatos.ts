import { ErrorApi } from '../cliente'
import type {
  ActualizarCandidatoPeticion,
  CandidatoHabilidad,
  CandidatoHabilidadPeticion,
  CandidatoPerfil,
  NivelHabilidad,
  RegistroCandidatoPeticion,
} from '../tipos'
import { CATALOGO_MOCK } from './habilidades'
import { esperar, guardar, leerGuardado, usuarioActualMock } from './utilidades'

// Simulación de /api/candidatos/me (contrato 5.1 y 5.2), con los mismos mensajes del backend.
// Los perfiles se guardan por id de usuario. El candidato de demostración (candidato@demo.co) ya tiene
// habilidades; los registrados en /registro empiezan sin ninguna.

const CLAVE_CANDIDATOS = 'primer-empleo.mock.candidatos'
const ID_USUARIO_CANDIDATO_DEMO = '11111111-1111-1111-1111-111111111111'
const MAXIMO_HABILIDADES = 30

export const ID_CANDIDATO_DEMO = '77777777-7777-7777-7777-777777777777'

function candidatoDemo(): CandidatoPerfil {
  return {
    id: ID_CANDIDATO_DEMO,
    email: 'candidato@demo.co',
    nombres: 'Laura',
    apellidos: 'Gómez Restrepo',
    estadoAcademico: 'ESTUDIANTE',
    semestre: 9,
    universidad: 'Universidad de Antioquia',
    programa: 'Ingeniería de Sistemas',
    ciudad: 'Medellín',
    githubUrl: 'https://github.com/lauragomez',
    resumen: 'Me interesa el desarrollo backend con Java y las bases de datos.',
    habilidades: conNombres([
      { habilidadId: idDe('Java'), nivel: 'BASICO' },
      { habilidadId: idDe('SQL'), nivel: 'INTERMEDIO' },
      { habilidadId: idDe('Spring Boot'), nivel: 'BASICO' },
      { habilidadId: idDe('Git'), nivel: 'INTERMEDIO' },
      { habilidadId: idDe('Trabajo en equipo'), nivel: 'AVANZADO' },
    ]),
  }
}

/**
 * Candidatos de demostración sin cuenta: se postularon a la vacante "Desarrollador Java junior" de la empresa
 * de demostración para que la pantalla de postulantes (HU-08) tenga a quién mostrar.
 */
const CANDIDATOS_SIN_CUENTA: CandidatoPerfil[] = [
  sinCuenta('77777777-0000-4000-8000-000000000001', 'Andrés', 'Restrepo Vélez', 'EGRESADO', null,
    'Universidad Nacional de Colombia', 'Ingeniería de Sistemas e Informática', 'https://github.com/andresrestrepo',
    [['Java', 'AVANZADO'], ['Spring Boot', 'INTERMEDIO'], ['Git', 'INTERMEDIO'], ['PostgreSQL', 'INTERMEDIO']]),
  sinCuenta('77777777-0000-4000-8000-000000000002', 'Camila', 'Ortiz Londoño', 'ESTUDIANTE', 10,
    'Universidad EAFIT', 'Ingeniería de Sistemas', null, [['Java', 'INTERMEDIO'], ['Git', 'BASICO'], ['Inglés', 'AVANZADO']]),
  sinCuenta('77777777-0000-4000-8000-000000000003', 'Santiago', 'Mejía Arango', 'ESTUDIANTE', 9,
    'Politécnico Colombiano Jaime Isaza Cadavid', 'Tecnología en Sistemas', 'https://github.com/santimejia',
    [['Java', 'BASICO'], ['Spring Boot', 'BASICO'], ['Docker', 'BASICO']]),
  sinCuenta('77777777-0000-4000-8000-000000000004', 'Valeria', 'Cano Gil', 'EGRESADO', null,
    'Universidad de Medellín', 'Ingeniería de Software', null, [['Python', 'INTERMEDIO'], ['SQL', 'INTERMEDIO']]),
]

function sinCuenta(
  id: string,
  nombres: string,
  apellidos: string,
  estadoAcademico: CandidatoPerfil['estadoAcademico'],
  semestre: number | null,
  universidad: string,
  programa: string,
  githubUrl: string | null,
  habilidades: [string, NivelHabilidad][],
): CandidatoPerfil {
  return {
    id,
    email: `${nombres.toLowerCase()}.${apellidos.split(' ')[0].toLowerCase()}@example.com`,
    nombres,
    apellidos,
    estadoAcademico,
    semestre,
    universidad,
    programa,
    ciudad: 'Medellín',
    githubUrl,
    resumen: `Me interesa empezar mi carrera como desarrollador en un equipo que me acompañe.`,
    habilidades: conNombres(habilidades.map(([nombre, nivel]) => ({ habilidadId: idDe(nombre), nivel }))),
  }
}

/** Perfil de cualquier candidato por su id (no el de usuario): lo usa la lista de postulantes de la empresa. */
export function perfilCandidatoPorIdMock(candidatoId: string): CandidatoPerfil | undefined {
  return [...Object.values(leerPerfiles()), candidatoDemo(), ...CANDIDATOS_SIN_CUENTA].find((c) => c.id === candidatoId)
}

function idDe(nombre: string): string {
  return CATALOGO_MOCK.find((h) => h.nombre === nombre)!.id
}

type Perfiles = Record<string, CandidatoPerfil>

const leerPerfiles = () => leerGuardado<Perfiles>(CLAVE_CANDIDATOS, () => ({}))

/** Perfil del candidato del usuario simulado; también lo usan la búsqueda y las postulaciones. */
export function perfilCandidatoActualMock(): CandidatoPerfil {
  const sesion = usuarioActualMock()
  if (sesion.rol !== 'CANDIDATO') throw new ErrorApi(403, 'No tienes permiso para realizar esta acción')
  const perfil =
    leerPerfiles()[sesion.usuarioId] ?? (sesion.usuarioId === ID_USUARIO_CANDIDATO_DEMO ? candidatoDemo() : null)
  if (!perfil) throw new ErrorApi(404, 'El candidato no existe')
  return perfil
}

function guardarPerfilActual(perfil: CandidatoPerfil) {
  guardar(CLAVE_CANDIDATOS, { ...leerPerfiles(), [usuarioActualMock().usuarioId]: perfil })
}

export function registrarPerfilCandidatoMock(usuarioId: string, email: string, datos: RegistroCandidatoPeticion) {
  guardar(CLAVE_CANDIDATOS, {
    ...leerPerfiles(),
    [usuarioId]: {
      id: crypto.randomUUID(),
      email,
      nombres: datos.nombres.trim(),
      apellidos: datos.apellidos.trim(),
      estadoAcademico: datos.estadoAcademico,
      semestre: datos.semestre,
      universidad: datos.universidad,
      programa: datos.programa,
      ciudad: datos.ciudad,
      githubUrl: null,
      resumen: null,
      habilidades: [],
    },
  })
}

/** Nombre y categoría del catálogo, ordenadas como el catálogo (contrato 5.1). */
function conNombres(lista: CandidatoHabilidadPeticion[]): CandidatoHabilidad[] {
  return lista
    .map(({ habilidadId, nivel }) => {
      const habilidad = CATALOGO_MOCK.find((h) => h.id === habilidadId)
      if (!habilidad) throw new ErrorApi(400, `habilidades: la habilidad ${habilidadId} no existe`)
      return { habilidadId, nombre: habilidad.nombre, categoria: habilidad.categoria, nivel }
    })
    .sort((a, b) => CATALOGO_MOCK.findIndex((h) => h.id === a.habilidadId) - CATALOGO_MOCK.findIndex((h) => h.id === b.habilidadId))
}

export async function obtenerMiPerfilCandidatoMock(): Promise<CandidatoPerfil> {
  await esperar()
  return perfilCandidatoActualMock()
}

export async function actualizarMiPerfilCandidatoMock(datos: ActualizarCandidatoPeticion): Promise<CandidatoPerfil> {
  await esperar()
  const actual = perfilCandidatoActualMock()
  if ((datos.estadoAcademico === 'ESTUDIANTE') !== (datos.semestre !== null)) {
    throw new ErrorApi(400, 'semestre: es obligatorio para estudiantes y debe ir vacío para egresados')
  }
  if (datos.githubUrl !== null && !/^https:\/\/github\.com\/\S+$/.test(datos.githubUrl)) {
    throw new ErrorApi(400, 'githubUrl: debe empezar por https://github.com/')
  }
  const actualizado = { ...actual, ...datos }
  guardarPerfilActual(actualizado)
  return actualizado
}

export async function actualizarMisHabilidadesMock(lista: CandidatoHabilidadPeticion[]): Promise<CandidatoHabilidad[]> {
  await esperar()
  const actual = perfilCandidatoActualMock()
  if (lista.length > MAXIMO_HABILIDADES) throw new ErrorApi(400, `habilidades: máximo ${MAXIMO_HABILIDADES}`)
  const ids = lista.map((h) => h.habilidadId)
  if (new Set(ids).size !== ids.length) throw new ErrorApi(400, 'habilidades: no se puede repetir una habilidad')
  const niveles: NivelHabilidad[] = ['BASICO', 'INTERMEDIO', 'AVANZADO']
  lista.forEach(({ nivel }, indice) => {
    if (!niveles.includes(nivel)) throw new ErrorApi(400, `habilidades[${indice}].nivel: es obligatorio`)
  })
  const habilidades = conNombres(lista)
  guardarPerfilActual({ ...actual, habilidades })
  return habilidades
}
