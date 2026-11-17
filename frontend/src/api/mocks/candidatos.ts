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
