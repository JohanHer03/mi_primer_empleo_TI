import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ErrorApi } from '../api/cliente'

/**
 * Pone bajo cada campo los errores de validación del backend, que llegan en `mensaje`
 * como "campo: mensaje; campo: mensaje" (docs/contrato-api.md). Devuelve true si asignó alguno.
 */
export function aplicarErroresDeValidacion<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  campos: readonly Path<T>[],
): boolean {
  if (!(error instanceof ErrorApi) || error.status !== 400) return false
  let asignados = 0
  for (const parte of error.message.split(';')) {
    const separador = parte.indexOf(':')
    if (separador < 0) continue
    const campo = parte.slice(0, separador).trim() as Path<T>
    if (campos.includes(campo)) {
      setError(campo, { type: 'server', message: parte.slice(separador + 1).trim() })
      asignados++
    }
  }
  return asignados > 0
}
