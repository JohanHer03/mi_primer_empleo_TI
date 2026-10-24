import { useEffect, useState } from 'react'

/** Tiempo tras el cual una petición se considera lenta (el arranque en frío del backend en Vercel tarda ~9 s). */
export const UMBRAL_DEMORA_MS = 3000

/** Devuelve true cuando `activo` lleva más de `ms` milisegundos en true; vuelve a false al terminar. */
export function useDemoraLarga(activo: boolean, ms = UMBRAL_DEMORA_MS): boolean {
  const [demorado, setDemorado] = useState(false)

  useEffect(() => {
    if (!activo) return
    const temporizador = setTimeout(() => setDemorado(true), ms)
    return () => {
      clearTimeout(temporizador)
      setDemorado(false)
    }
  }, [activo, ms])

  return activo && demorado
}
