import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router'
import { ErrorApi } from '../../api/cliente'
import { postularse } from '../../api/postulaciones'
import type { PostulacionDeCandidato, VacanteResumen } from '../../api/tipos'
import formulario from '../../components/Formulario.module.css'
import {
  formatearCompatibilidad,
  formatearFecha,
  formatearSalario,
  TEXTO_MODALIDAD,
  TEXTO_TIPO,
} from '../../components/formato'
import comunes from '../../components/Paginas.module.css'
import { CLAVES } from './consultas'
import { IndicadorCompatibilidad } from './IndicadorCompatibilidad'
import estilos from './PaginasCandidato.module.css'

interface PropsTarjeta {
  vacante: VacanteResumen
  /** h3 cuando la lista va dentro de una sección con su propio h2 (inicio). */
  nivelTitulo?: 'h2' | 'h3'
}

/** Vacante de la búsqueda con su compatibilidad y el botón "Postularme" (HU-03 y HU-05). */
export function TarjetaVacanteBusqueda({ vacante, nivelTitulo: Titulo = 'h2' }: PropsTarjeta) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [enviada, setEnviada] = useState<PostulacionDeCandidato | null>(null)
  const [sinBoton, setSinBoton] = useState(false)
  const salario = formatearSalario(vacante.salarioMin, vacante.salarioMax)

  const postulacion = useMutation({
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
      // 409: ya se postuló (en otra pestaña) o la vacante cerró mientras la tenía en pantalla.
      // No se recarga el listado para que el mensaje no desaparezca con la tarjeta; solo se quita el botón.
      if (e instanceof ErrorApi && e.status === 409) setSinBoton(true)
    },
  })

  const confirmar = () => {
    const pregunta =
      `¿Postularte a "${vacante.titulo}" en ${vacante.empresa.razonSocial}? ` +
      `Se enviará con tu compatibilidad actual (${formatearCompatibilidad(vacante.compatibilidad)}) y no podrás retirarla.`
    if (window.confirm(pregunta)) postulacion.mutate()
  }

  const yaPostulado = vacante.postulado || enviada !== null

  return (
    <li className={estilos.tarjeta}>
      <IndicadorCompatibilidad valor={vacante.compatibilidad} />
      <div className={comunes.tarjetaDatos}>
        <Titulo className={comunes.tarjetaTitulo}>{vacante.titulo}</Titulo>
        <p className={estilos.empresa}>{vacante.empresa.razonSocial}</p>
        <p className={comunes.detalles}>
          {TEXTO_TIPO[vacante.tipo]} · {TEXTO_MODALIDAD[vacante.modalidad]} · {vacante.ciudad}
          {salario && ` · ${salario}`}
        </p>
        <p className={comunes.detalles}>
          Publicada el {formatearFecha(vacante.fechaPublicacion)} · Cierra el {formatearFecha(vacante.fechaCierre)}
        </p>
      </div>
      <div className={estilos.accionTarjeta}>
        {yaPostulado ? (
          <span className={estilos.yaPostulado}>Ya te postulaste</span>
        ) : sinBoton ? null : (
          <button type="button" className="boton" onClick={confirmar} disabled={postulacion.isPending}>
            {postulacion.isPending ? 'Enviando…' : 'Postularme'}
          </button>
        )}
      </div>
      {enviada && (
        <p className={`${comunes.mensajeExito} ${estilos.mensajeTarjeta}`} role="status">
          Te postulaste con {formatearCompatibilidad(enviada.compatibilidad)} de compatibilidad. Sigue su estado en{' '}
          <Link to="/candidato/postulaciones">Mis postulaciones</Link>.
        </p>
      )}
      {error && (
        <p className={`${formulario.errorCampo} ${estilos.mensajeTarjeta}`} role="alert">
          {error}
        </p>
      )}
    </li>
  )
}
