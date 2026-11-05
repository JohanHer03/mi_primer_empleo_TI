import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { listarMisVacantes } from '../../api/vacantes'
import { AvisoVerificacion } from '../../components/AvisoVerificacion'
import { Cargando, MensajeError } from '../../components/Estados'
import { CLAVES, useMiPerfil } from './consultas'
import estilos from './PaginasEmpresa.module.css'

/** Inicio de la empresa: estado de la verificación y accesos a sus vacantes. */
export default function InicioEmpresa() {
  const perfil = useMiPerfil()
  // Solo interesa el total de abiertas: se pide una página de un elemento
  const abiertas = useQuery({
    queryKey: [...CLAVES.misVacantes, 'ABIERTA', 'total'],
    queryFn: () => listarMisVacantes('ABIERTA', 0, 1),
    enabled: perfil.data?.verificada === true,
  })

  if (perfil.isPending) return <Cargando />
  if (perfil.isError) return <MensajeError error={perfil.error} alReintentar={() => perfil.refetch()} />

  const { razonSocial, verificada } = perfil.data
  const totalAbiertas = abiertas.data?.totalElementos

  return (
    <section className={estilos.pagina}>
      <h1>Hola, {razonSocial}</h1>
      {!verificada && <AvisoVerificacion />}
      {verificada && (
        <p className={estilos.detalles}>
          {totalAbiertas === undefined
            ? 'Tu empresa está verificada: ya puedes publicar vacantes.'
            : totalAbiertas === 0
              ? 'No tienes vacantes abiertas en este momento.'
              : `Tienes ${totalAbiertas} ${totalAbiertas === 1 ? 'vacante abierta' : 'vacantes abiertas'}.`}
        </p>
      )}
      <div className={estilos.acciones}>
        {verificada && (
          <Link to="/empresa/vacantes/nueva" className="boton">
            Publicar vacante
          </Link>
        )}
        <Link to="/empresa/vacantes" className="boton boton-secundario">
          Ver mis vacantes
        </Link>
        <Link to="/empresa/perfil" className="boton boton-secundario">
          {verificada ? 'Perfil de empresa' : 'Completar perfil'}
        </Link>
      </div>
    </section>
  )
}
