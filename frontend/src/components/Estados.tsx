import type { ReactNode } from 'react'
import { ErrorApi } from '../api/cliente'
import estilos from './Estados.module.css'

// Estados obligatorios de cada pantalla (CLAUDE.md): carga, vacío y error.

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <div className={estilos.contenedor} role="status" aria-live="polite">
      <div className={estilos.spinner} aria-hidden="true" />
      <p className={estilos.descripcion}>{texto}</p>
    </div>
  )
}

export function EstadoVacio({ titulo, descripcion, accion }: { titulo: string; descripcion?: string; accion?: ReactNode }) {
  return (
    <div className={estilos.contenedor}>
      <p className={estilos.titulo}>{titulo}</p>
      {descripcion && <p className={estilos.descripcion}>{descripcion}</p>}
      {accion}
    </div>
  )
}

export function MensajeError({ error, alReintentar }: { error: unknown; alReintentar?: () => void }) {
  const mensaje = error instanceof ErrorApi || error instanceof Error ? error.message : 'Ocurrió un error inesperado.'
  return (
    <div className={`${estilos.contenedor} ${estilos.error}`} role="alert">
      <p className={estilos.titulo}>No pudimos cargar la información</p>
      <p className={estilos.descripcion}>{mensaje}</p>
      {alReintentar && (
        <button type="button" className="boton boton-secundario" onClick={alReintentar}>
          Reintentar
        </button>
      )}
    </div>
  )
}
