import type { CategoriaHabilidad, Habilidad } from '../tipos'
import { esperar } from './utilidades'

// Simulación de GET /api/habilidades (contrato 4.1) con el catálogo inicial de V4.

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

export async function listarHabilidadesMock(categoria?: CategoriaHabilidad): Promise<Habilidad[]> {
  await esperar()
  return categoria ? CATALOGO_MOCK.filter((h) => h.categoria === categoria) : CATALOGO_MOCK
}
