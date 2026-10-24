import { useContext } from 'react'
import { ContextoAuth } from './contextoAuth'

export function useAuth() {
  const valor = useContext(ContextoAuth)
  if (!valor) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return valor
}
