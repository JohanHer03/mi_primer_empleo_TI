import type { ReactNode } from 'react'
import formulario from './Formulario.module.css'

interface PropsCampo {
  id: string
  etiqueta: string
  error?: string
  ayuda?: string
  opcional?: boolean
  children: ReactNode
}

/** Etiqueta, control, texto de ayuda y error de un campo de formulario. */
export function Campo({ id, etiqueta, error, ayuda, opcional, children }: PropsCampo) {
  return (
    <div className={formulario.campo}>
      <label htmlFor={id}>
        {etiqueta}
        {opcional && <span className={formulario.opcional}> (opcional)</span>}
      </label>
      {children}
      {ayuda && (
        <p id={`${id}-ayuda`} className={formulario.ayuda}>
          {ayuda}
        </p>
      )}
      {error && (
        <p id={`${id}-error`} className={formulario.errorCampo}>
          {error}
        </p>
      )}
    </div>
  )
}
