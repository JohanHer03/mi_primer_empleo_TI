import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { listarMisPostulaciones } from '../../api/postulaciones'
import { buscarVacantes } from '../../api/vacantes'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import comunes from '../../components/Paginas.module.css'
import { CLAVES, useMiPerfilCandidato } from './consultas'
import estilos from './PaginasCandidato.module.css'
import { TarjetaVacanteBusqueda } from './TarjetaVacanteBusqueda'

const RECOMENDADAS = 3

/** Inicio del candidato: estado de su perfil, sus postulaciones y las vacantes más compatibles. */
export default function InicioCandidato() {
  const perfil = useMiPerfilCandidato()
  // Solo interesa el total: se pide una página de un elemento
  const postulaciones = useQuery({
    queryKey: [...CLAVES.misPostulaciones, 'TODAS', 'total'],
    queryFn: () => listarMisPostulaciones(undefined, 0, 1),
  })
  const recomendadas = useQuery({
    queryKey: [...CLAVES.busqueda, 'recomendadas'],
    queryFn: () => buscarVacantes({ orden: 'compatibilidad' }, 0, RECOMENDADAS),
  })

  if (perfil.isPending) return <Cargando />
  if (perfil.isError) return <MensajeError error={perfil.error} alReintentar={() => perfil.refetch()} />

  const { nombres, habilidades } = perfil.data
  const sinHabilidades = habilidades.length === 0

  return (
    <section className={comunes.pagina}>
      <h1>Hola, {nombres}</h1>

      {sinHabilidades && (
        <p className={formulario.aviso}>
          Agrega tus habilidades con su nivel para ver qué tan compatible eres con cada vacante.{' '}
          <Link to="/candidato/perfil">Completar mi perfil</Link>
        </p>
      )}

      <div className={estilos.resumen}>
        <div className={estilos.dato}>
          <span className={estilos.datoValor}>{habilidades.length}</span>
          <span className={estilos.datoTexto}>{habilidades.length === 1 ? 'habilidad registrada' : 'habilidades registradas'}</span>
        </div>
        <div className={estilos.dato}>
          <span className={estilos.datoValor}>{postulaciones.data?.totalElementos ?? '–'}</span>
          <span className={estilos.datoTexto}>
            {postulaciones.data?.totalElementos === 1 ? 'postulación enviada' : 'postulaciones enviadas'}
          </span>
        </div>
        <div className={estilos.dato}>
          <span className={estilos.datoValor}>{recomendadas.data?.totalElementos ?? '–'}</span>
          <span className={estilos.datoTexto}>vacantes abiertas</span>
        </div>
      </div>

      <div className={comunes.acciones}>
        <Link to="/candidato/vacantes" className="boton">
          Buscar vacantes
        </Link>
        <Link to="/candidato/postulaciones" className="boton boton-secundario">
          Mis postulaciones
        </Link>
        <Link to="/candidato/perfil" className="boton boton-secundario">
          Mi perfil
        </Link>
      </div>

      <section className={comunes.pagina} aria-labelledby="titulo-recomendadas">
        <h2 id="titulo-recomendadas">Vacantes más compatibles contigo</h2>
        {recomendadas.isPending && <Cargando texto="Buscando vacantes para ti…" />}
        {recomendadas.isError && <MensajeError error={recomendadas.error} alReintentar={() => recomendadas.refetch()} />}
        {recomendadas.isSuccess && recomendadas.data.contenido.length === 0 && (
          <EstadoVacio titulo="No hay vacantes abiertas en este momento" descripcion="Vuelve pronto: las empresas publican cada semana." />
        )}
        {recomendadas.isSuccess && recomendadas.data.contenido.length > 0 && (
          <ul className={comunes.lista}>
            {recomendadas.data.contenido.map((vacante) => (
              <TarjetaVacanteBusqueda key={vacante.id} vacante={vacante} nivelTitulo="h3" />
            ))}
          </ul>
        )}
      </section>
    </section>
  )
}
