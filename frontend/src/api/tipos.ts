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
