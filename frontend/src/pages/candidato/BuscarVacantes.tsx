import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router'
import type { FiltrosVacantes, Modalidad, OrdenVacantes, TipoVacante } from '../../api/tipos'
import { buscarVacantes } from '../../api/vacantes'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import { TEXTO_MODALIDAD, TEXTO_TIPO } from '../../components/formato'
import { OpcionesHabilidades } from '../../components/OpcionesHabilidades'
import { Paginacion } from '../../components/Paginacion'
import comunes from '../../components/Paginas.module.css'
import { useCatalogoHabilidades } from '../../hooks/useCatalogoHabilidades'
import { CLAVES, useMiPerfilCandidato } from './consultas'
import estilos from './PaginasCandidato.module.css'
import { TarjetaVacanteBusqueda } from './TarjetaVacanteBusqueda'

const MODALIDADES = Object.keys(TEXTO_MODALIDAD) as Modalidad[]
const TIPOS = Object.keys(TEXTO_TIPO) as TipoVacante[]
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

/** Filtros y orden desde la URL: se conservan al recargar, al compartir el enlace y al volver atrás. */
function leerFiltros(parametros: URLSearchParams): Required<Pick<FiltrosVacantes, 'orden'>> & FiltrosVacantes {
  const habilidadId = parametros.get('habilidad') ?? ''
  return {
    modalidad: MODALIDADES.find((m) => m === parametros.get('modalidad')),
    tipo: TIPOS.find((t) => t === parametros.get('tipo')),
    habilidadId: UUID.test(habilidadId) ? habilidadId : undefined,
    ciudad: parametros.get('ciudad')?.trim() || undefined,
    orden: parametros.get('orden') === 'fechaPublicacion' ? 'fechaPublicacion' : 'compatibilidad',
  }
}

function aParametros(filtros: FiltrosVacantes, pagina = 0): URLSearchParams {
  const parametros = new URLSearchParams()
  if (filtros.modalidad) parametros.set('modalidad', filtros.modalidad)
  if (filtros.tipo) parametros.set('tipo', filtros.tipo)
  if (filtros.habilidadId) parametros.set('habilidad', filtros.habilidadId)
  if (filtros.ciudad) parametros.set('ciudad', filtros.ciudad)
  if (filtros.orden === 'fechaPublicacion') parametros.set('orden', filtros.orden)
  if (pagina > 0) parametros.set('pagina', String(pagina))
  return parametros
}

/** HU-03 y HU-05: el candidato busca vacantes abiertas, ordenadas por compatibilidad, y se postula. */
export default function BuscarVacantes() {
  const [parametros, setParametros] = useSearchParams()
  const filtros = leerFiltros(parametros)
  const pagina = Math.max(0, Number(parametros.get('pagina')) || 0)
  const hayFiltros = Boolean(filtros.modalidad || filtros.tipo || filtros.habilidadId || filtros.ciudad)

  const perfil = useMiPerfilCandidato()
  const catalogo = useCatalogoHabilidades()
  const vacantes = useQuery({
    queryKey: [...CLAVES.busqueda, filtros, pagina],
    queryFn: () => buscarVacantes(filtros, pagina),
    placeholderData: keepPreviousData,
  })

  const aplicar = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault()
    const datos = new FormData(evento.currentTarget)
    const texto = (campo: string) => String(datos.get(campo) ?? '').trim() || undefined
    setParametros(
      aParametros({
        modalidad: texto('modalidad') as Modalidad | undefined,
        tipo: texto('tipo') as TipoVacante | undefined,
        habilidadId: texto('habilidad'),
        ciudad: texto('ciudad'),
        orden: filtros.orden,
      }),
    )
  }

  const cambiarOrden = (orden: OrdenVacantes) => setParametros(aParametros({ ...filtros, orden }))
  const total = vacantes.data?.totalElementos

  return (
    <section className={comunes.pagina}>
      <h1>Buscar vacantes</h1>

      {perfil.isSuccess && perfil.data.habilidades.length === 0 && (
        <p className={formulario.aviso}>
          Aún no tienes habilidades en tu perfil: tu compatibilidad con todas las vacantes es 0 %.{' '}
          <Link to="/candidato/perfil">Agrega tus habilidades</Link> antes de postularte.
        </p>
      )}

      {/* key: al limpiar, al volver atrás o al llegar el catálogo, los campos toman los valores de la URL */}
      <form key={`${parametros.toString()}|${catalogo.isSuccess}`} className={estilos.filtros} onSubmit={aplicar} aria-label="Filtros de búsqueda">
        <div className={formulario.campo}>
          <label htmlFor="b-modalidad">Modalidad</label>
          <select id="b-modalidad" name="modalidad" defaultValue={filtros.modalidad ?? ''}>
            <option value="">Todas</option>
            {MODALIDADES.map((modalidad) => (
              <option key={modalidad} value={modalidad}>
                {TEXTO_MODALIDAD[modalidad]}
              </option>
            ))}
          </select>
        </div>

        <div className={formulario.campo}>
          <label htmlFor="b-tipo">Nivel</label>
          <select id="b-tipo" name="tipo" defaultValue={filtros.tipo ?? ''}>
            <option value="">Práctica y junior</option>
            {TIPOS.map((tipo) => (
              <option key={tipo} value={tipo}>
                {TEXTO_TIPO[tipo]}
              </option>
            ))}
          </select>
        </div>

        <div className={formulario.campo}>
          <label htmlFor="b-habilidad">Habilidad</label>
          <select
            id="b-habilidad"
            name="habilidad"
            defaultValue={filtros.habilidadId ?? ''}
            disabled={!catalogo.isSuccess}
          >
            <option value="">Cualquiera</option>
            {catalogo.isSuccess && <OpcionesHabilidades catalogo={catalogo.data} />}
          </select>
        </div>

        <div className={formulario.campo}>
          <label htmlFor="b-ciudad">Ciudad</label>
          <input id="b-ciudad" name="ciudad" placeholder="Medellín" defaultValue={filtros.ciudad ?? ''} />
        </div>

        <div className={comunes.acciones}>
          <button type="submit" className="boton">
            Buscar
          </button>
          {hayFiltros && (
            <button
              type="button"
              className="boton boton-secundario"
              onClick={() => setParametros(aParametros({ orden: filtros.orden }))}
            >
              Limpiar
            </button>
          )}
        </div>
      </form>

      <div className={estilos.barraResultados}>
        <p role="status">
          {total === undefined ? '' : total === 1 ? '1 vacante encontrada' : `${total} vacantes encontradas`}
        </p>
        <div className={estilos.orden}>
          <label htmlFor="b-orden">Ordenar por</label>
          <select id="b-orden" value={filtros.orden} onChange={(e) => cambiarOrden(e.target.value as OrdenVacantes)}>
            <option value="compatibilidad">Mayor compatibilidad</option>
            <option value="fechaPublicacion">Más recientes</option>
          </select>
        </div>
      </div>

      {vacantes.isPending && <Cargando texto="Buscando vacantes…" />}
      {vacantes.isError && <MensajeError error={vacantes.error} alReintentar={() => vacantes.refetch()} />}
      {vacantes.isSuccess && vacantes.data.contenido.length === 0 && (
        <EstadoVacio
          titulo={hayFiltros ? 'Ninguna vacante coincide con tu búsqueda' : 'No hay vacantes abiertas en este momento'}
          descripcion={hayFiltros ? 'Prueba quitando algún filtro.' : 'Vuelve pronto: las empresas publican cada semana.'}
        />
      )}
      {vacantes.isSuccess && vacantes.data.contenido.length > 0 && (
        <>
          <ul className={comunes.lista}>
            {vacantes.data.contenido.map((vacante) => (
              <TarjetaVacanteBusqueda key={vacante.id} vacante={vacante} />
            ))}
          </ul>
          <Paginacion
            pagina={pagina}
            totalPaginas={vacantes.data.totalPaginas}
            alCambiar={(nueva) => {
              setParametros(aParametros(filtros, nueva))
              window.scrollTo({ top: 0 })
            }}
          />
        </>
      )}
    </section>
  )
}
