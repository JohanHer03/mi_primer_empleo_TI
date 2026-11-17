// Tipos que reflejan los DTOs del backend (docs/contrato-api.md). Se completan con cada fase.

export type Rol = 'CANDIDATO' | 'EMPRESA' | 'ADMIN'

export type EstadoAcademico = 'ESTUDIANTE' | 'EGRESADO'

/** Formato uniforme de error del backend: { status, error, mensaje, timestamp }. */
export interface ErrorRespuesta {
  status: number
  error: string
  mensaje: string
  timestamp: string
}

/** Respuesta de login y registro. El JWT trae sub, email, rol, iat y exp; el frontend los lee del token. */
export interface AuthRespuesta {
  token: string
}

export interface LoginPeticion {
  email: string
  password: string
}

export interface RegistroCandidatoPeticion {
  email: string
  password: string
  nombres: string
  apellidos: string
  estadoAcademico: EstadoAcademico
  /** Obligatorio para ESTUDIANTE, null para EGRESADO. */
  semestre: number | null
  universidad: string | null
  programa: string | null
  ciudad: string | null
}

export interface RegistroEmpresaPeticion {
  email: string
  password: string
  razonSocial: string
  /** Solo dígitos, con el dígito de verificación incluido. */
  nit: string
  sector: string | null
  ciudad: string | null
  sitioWeb: string | null
}

/** Listado paginado (docs/contrato-api.md, sección 1). */
export interface PaginaDto<T> {
  contenido: T[]
  pagina: number
  tamano: number
  totalElementos: number
  totalPaginas: number
}

// Fase 2: habilidades, empresas y vacantes (docs/contrato-api.md, sección 4)

export type CategoriaHabilidad = 'LENGUAJE' | 'FRAMEWORK' | 'BASE_DATOS' | 'HERRAMIENTA' | 'BLANDA'

export interface Habilidad {
  id: string
  nombre: string
  categoria: CategoriaHabilidad
}

export interface EmpresaPerfil {
  id: string
  razonSocial: string
  nit: string
  email: string
  sector: string | null
  ciudad: string | null
  sitioWeb: string | null
  verificada: boolean
}

/** El NIT y el correo no se editan (contrato 4.2). */
export interface ActualizarEmpresaPeticion {
  razonSocial: string
  sector: string | null
  ciudad: string | null
  sitioWeb: string | null
}

export type TipoVacante = 'PRACTICA' | 'JUNIOR'
export type Modalidad = 'PRESENCIAL' | 'HIBRIDA' | 'REMOTA'
export type EstadoVacante = 'ABIERTA' | 'CERRADA'
export type NivelHabilidad = 'BASICO' | 'INTERMEDIO' | 'AVANZADO'

export interface VacanteHabilidadPeticion {
  habilidadId: string
  nivelRequerido: NivelHabilidad
  obligatoria: boolean
}

/** Cuerpo para publicar y para editar (reemplaza todos los campos, también las habilidades). */
export interface VacantePeticion {
  titulo: string
  descripcion: string
  tipo: TipoVacante
  modalidad: Modalidad
  ciudad: string
  /** Pesos colombianos sin decimales. */
  salarioMin: number | null
  salarioMax: number | null
  /** "AAAA-MM-DD", hoy o después en hora de Colombia. */
  fechaCierre: string
  habilidades: VacanteHabilidadPeticion[]
}

export interface HabilidadRequerida {
  habilidadId: string
  nombre: string
  categoria: CategoriaHabilidad
  nivelRequerido: NivelHabilidad
  obligatoria: boolean
}

export interface VacanteDetalle {
  id: string
  titulo: string
  descripcion: string
  tipo: TipoVacante
  modalidad: Modalidad
  ciudad: string
  salarioMin: number | null
  salarioMax: number | null
  estado: EstadoVacante
  fechaPublicacion: string
  fechaCierre: string
  empresa: { id: string; razonSocial: string }
  /** Primero las obligatorias y luego por nombre. */
  habilidades: HabilidadRequerida[]
}

export interface VacanteDeEmpresaResumen {
  id: string
  titulo: string
  tipo: TipoVacante
  modalidad: Modalidad
  ciudad: string
  estado: EstadoVacante
  fechaPublicacion: string
  fechaCierre: string
  totalPostulaciones: number
}

// Fase 3: candidato y postulaciones (docs/contrato-api.md, sección 5)

/** Habilidad del candidato con su nivel, en el orden del catálogo. */
export interface CandidatoHabilidad {
  habilidadId: string
  nombre: string
  categoria: CategoriaHabilidad
  nivel: NivelHabilidad
}

export interface CandidatoPerfil {
  id: string
  email: string
  nombres: string
  apellidos: string
  estadoAcademico: EstadoAcademico
  semestre: number | null
  universidad: string | null
  programa: string | null
  ciudad: string | null
  githubUrl: string | null
  resumen: string | null
  habilidades: CandidatoHabilidad[]
}

/** El correo no se edita y las habilidades van por PUT /api/candidatos/me/habilidades (contrato 5.1). */
export interface ActualizarCandidatoPeticion {
  nombres: string
  apellidos: string
  estadoAcademico: EstadoAcademico
  semestre: number | null
  universidad: string | null
  programa: string | null
  ciudad: string | null
  githubUrl: string | null
  resumen: string | null
}

export interface CandidatoHabilidadPeticion {
  habilidadId: string
  nivel: NivelHabilidad
}

export type OrdenVacantes = 'compatibilidad' | 'fechaPublicacion'

/** Filtros opcionales de GET /api/vacantes (contrato 5.4). */
export interface FiltrosVacantes {
  modalidad?: Modalidad
  tipo?: TipoVacante
  habilidadId?: string
  ciudad?: string
  orden?: OrdenVacantes
}

export interface VacanteResumen {
  id: string
  titulo: string
  empresa: { id: string; razonSocial: string }
  tipo: TipoVacante
  modalidad: Modalidad
  ciudad: string
  salarioMin: number | null
  salarioMax: number | null
  fechaPublicacion: string
  fechaCierre: string
  /** De 0 a 100 con un decimal; se calcula al consultar. */
  compatibilidad: number
  postulado: boolean
}

export type EstadoPostulacion = 'ENVIADA' | 'EN_REVISION' | 'ENTREVISTA' | 'RECHAZADA' | 'CONTRATADO'

export interface PostulacionDeCandidato {
  id: string
  fecha: string
  estado: EstadoPostulacion
  /** La calculada al postularse (regla 6): no cambia aunque el candidato edite sus habilidades. */
  compatibilidad: number
  vacante: {
    id: string
    titulo: string
    empresa: { id: string; razonSocial: string }
    modalidad: Modalidad
    ciudad: string
    estado: EstadoVacante
    fechaCierre: string
  }
}
