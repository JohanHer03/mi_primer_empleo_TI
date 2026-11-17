import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ErrorApi } from '../../api/cliente'
import { postularse } from '../../api/postulaciones'
import type { PostulacionDeCandidato } from '../../api/tipos'
import { formatearCompatibilidad } from '../../components/formato'
import { CLAVES } from './consultas'

interface VacanteAPostular {
  id: string
  titulo: string
  razonSocialEmpresa: string
  /** La de hoy: se muestra en la confirmación porque es la que se guardará (regla 6). */
  compatibilidad: number
}

/**
 * Flujo de "Postularme" de la búsqueda y del detalle: confirmación (no se puede retirar, contrato 5.5),
 * invalidación de la búsqueda, la compatibilidad y mis postulaciones, y manejo del 409.
 */
export function usePostularse(vacante: VacanteAPostular) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [enviada, setEnviada] = useState<PostulacionDeCandidato | null>(null)
  // 409: ya se postuló (en otra pestaña) o la vacante cerró mientras estaba en pantalla
  const [rechazada, setRechazada] = useState(false)

  const mutacion = useMutation({
    mutationFn: () => postularse(vacante.id),
    onMutate: () => setError(null),
    onSuccess: (creada) => {
      setEnviada(creada)
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: CLAVES.busqueda }),
        queryClient.invalidateQueries({ queryKey: CLAVES.misPostulaciones }),
      ])
    },
    onError: (e) => {
      setError(e.message)
      // No se recarga el listado para que el mensaje no desaparezca con la tarjeta; solo se quita el botón
      if (e instanceof ErrorApi && e.status === 409) setRechazada(true)
    },
  })

  const confirmar = () => {
    const pregunta =
      `¿Postularte a "${vacante.titulo}" en ${vacante.razonSocialEmpresa}? ` +
      `Se enviará con tu compatibilidad actual (${formatearCompatibilidad(vacante.compatibilidad)}) y no podrás retirarla.`
    if (window.confirm(pregunta)) mutacion.mutate()
  }

  return { confirmar, enviando: mutacion.isPending, enviada, error, rechazada }
}
