// Tipos que reflejan los DTOs del backend. Se completan con el contrato de cada funcionalidad.

export type Rol = 'CANDIDATO' | 'EMPRESA' | 'ADMIN'

/** Formato uniforme de error del backend: { status, error, mensaje, timestamp }. */
export interface ErrorRespuesta {
  status: number
  error: string
  mensaje: string
  timestamp: string
}

export interface LoginPeticion {
  email: string
  password: string
}

/** El JWT trae el id, el rol y la expiración; el frontend los lee del token. */
export interface LoginRespuesta {
  token: string
}
