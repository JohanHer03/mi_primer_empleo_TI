import { ErrorApi } from '../cliente'
import type { CategoriaHabilidad, CrearHabilidadPeticion, Habilidad } from '../tipos'
import { esperar, guardar, leerGuardado, usuarioActualMock } from './utilidades'

// Simulación de GET y POST /api/habilidades (contrato 4.1) con el catálogo inicial de V4.
// Las que crea el administrador se guardan en localStorage.

const CATALOGO_INICIAL: Record<CategoriaHabilidad, string[]> = {
  LENGUAJE: ['C#', 'Go', 'Java', 'JavaScript', 'Kotlin', 'PHP', 'Python', 'SQL', 'TypeScript'],
  FRAMEWORK: ['.NET', 'Angular', 'Django', 'Express', 'Node.js', 'React', 'Spring Boot', 'Vue.js'],
  BASE_DATOS: ['MongoDB', 'MySQL', 'PostgreSQL', 'Redis', 'SQL Server'],
  HERRAMIENTA: ['AWS', 'Azure', 'Docker', 'Git', 'Jira', 'Linux', 'Postman', 'Scrum'],
  BLANDA: ['Aprendizaje autónomo', 'Comunicación', 'Inglés', 'Resolución de problemas', 'Trabajo en equipo'],
}

/** Ids fijos para que las vacantes simuladas guardadas sigan apuntando a la misma habilidad. */
export const CATALOGO_MOCK: Habilidad[] = Object.entries(CATALOGO_INICIAL).flatMap(([categoria, nombres]) =>
  nombres.map((nombre) => ({ id: idDe(nombre), nombre, categoria: categoria as CategoriaHabilidad })),
)

function idDe(nombre: string): string {
  const numero = Object.values(CATALOGO_INICIAL).flat().indexOf(nombre) + 1
  return `00000000-0000-4000-8000-${String(numero).padStart(12, '0')}`
}

const CLAVE_CREADAS = 'primer-empleo.mock.habilidades'
const ORDEN_CATEGORIAS = Object.keys(CATALOGO_INICIAL)

const leerCreadas = () => leerGuardado<Habilidad[]>(CLAVE_CREADAS, () => [])

/** Catálogo completo, ordenado por categoría (orden del enum) y nombre, como el backend. */
function catalogoCompleto(): Habilidad[] {
  return [...CATALOGO_MOCK, ...leerCreadas()].sort(
    (a, b) =>
      ORDEN_CATEGORIAS.indexOf(a.categoria) - ORDEN_CATEGORIAS.indexOf(b.categoria) ||
      a.nombre.localeCompare(b.nombre, 'es'),
  )
}

export async function listarHabilidadesMock(categoria?: CategoriaHabilidad): Promise<Habilidad[]> {
  await esperar()
  const catalogo = catalogoCompleto()
  return categoria ? catalogo.filter((h) => h.categoria === categoria) : catalogo
}

export async function crearHabilidadMock({ nombre, categoria }: CrearHabilidadPeticion): Promise<Habilidad> {
  await esperar()
  if (usuarioActualMock().rol !== 'ADMIN') throw new ErrorApi(403, 'No tienes permiso para realizar esta acción')
  const limpio = nombre.trim()
  if (!limpio) throw new ErrorApi(400, 'nombre: es obligatorio')
  if (limpio.length > 100) throw new ErrorApi(400, 'nombre: máximo 100 caracteres')
  if (catalogoCompleto().some((h) => h.nombre.toLowerCase() === limpio.toLowerCase())) {
    throw new ErrorApi(409, 'Ya existe una habilidad con ese nombre')
  }
  const creada: Habilidad = { id: crypto.randomUUID(), nombre: limpio, categoria }
  guardar(CLAVE_CREADAS, [...leerCreadas(), creada])
  return creada
}
