import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router'
import type { EstadoVacante, VacanteDeEmpresaResumen } from '../../api/tipos'
import { cerrarVacante, listarMisVacantes } from '../../api/vacantes'
import { AvisoVerificacion } from '../../components/AvisoVerificacion'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import { estadoVisible, formatearFecha, TEXTO_MODALIDAD, TEXTO_TIPO } from '../../components/formato'
import { CLAVES, useMiPerfil } from './consultas'
import estilos from './PaginasEmpresa.module.css'

const FILTROS: { estado: EstadoVacante | undefined; texto: string }[] = [
  { estado: undefined, texto: 'Todas' },
  { estado: 'ABIERTA', texto: 'Abiertas' },
  { estado: 'CERRADA', texto: 'Cerradas' },
]

const TEXTO_ESTADO = { ABIERTA: 'Abierta', CERRADA: 'Cerrada', VENCIDA: 'Vencida' }

/** HU-07: la empresa ve sus vacantes, las edita y las cierra (GET /api/empresas/me/vacantes). */
export default function MisVacantes() {
  const perfil = useMiPerfil()
  const location = useLocation()
  const avisoNavegacion = (location.state as { aviso?: string } | null)?.aviso
  // Filtro y página en la URL: se conservan al recargar o al volver desde el formulario
  const [parametros, setParametros] = useSearchParams()
  const estado = (['ABIERTA', 'CERRADA'] as const).find((e) => e === parametros.get('estado'))
  const pagina = Math.max(0, Number(parametros.get('pagina')) || 0)

  const vacantes = useQuery({
    queryKey: [...CLAVES.misVacantes, estado ?? 'TODAS', pagina],
    queryFn: () => listarMisVacantes(estado, pagina),
    placeholderData: keepPreviousData,
  })

  const cambiar = (nuevoEstado: EstadoVacante | undefined, nuevaPagina = 0) => {
    const siguientes = new URLSearchParams()
    if (nuevoEstado) siguientes.set('estado', nuevoEstado)
    if (nuevaPagina > 0) siguientes.set('pagina', String(nuevaPagina))
    setParametros(siguientes)
  }

  const verificada = perfil.data?.verificada ?? false

  return (
    <section className={estilos.pagina}>
      <div className={estilos.encabezado}>
        <h1>Mis vacantes</h1>
        {verificada && (
          <Link to="/empresa/vacantes/nueva" className="boton">
            Publicar vacante
          </Link>
        )}
      </div>

      {perfil.isSuccess && !verificada && <AvisoVerificacion />}
      {avisoNavegacion && (
        <p className={estilos.mensajeExito} role="status">
          {avisoNavegacion}
        </p>
      )}

      <div className={estilos.filtros} role="group" aria-label="Filtrar por estado">
        {FILTROS.map((filtro) => (
          <button
            key={filtro.texto}
            type="button"
            className={estilos.filtro}
            aria-pressed={estado === filtro.estado}
            onClick={() => cambiar(filtro.estado)}
          >
            {filtro.texto}
          </button>
        ))}
      </div>

      {vacantes.isPending && <Cargando texto="Cargando tus vacantes…" />}
      {vacantes.isError && <MensajeError error={vacantes.error} alReintentar={() => vacantes.refetch()} />}
      {vacantes.isSuccess && vacantes.data.contenido.length === 0 && (
        <EstadoVacio
          titulo={estado ? 'No hay vacantes con este estado' : 'Aún no has publicado vacantes'}
          descripcion={
            estado
              ? 'Prueba con otro filtro.'
              : verificada
                ? 'Publica tu primera vacante para empezar a recibir postulaciones.'
                : 'Podrás publicar cuando un administrador verifique tu empresa.'
          }
          accion={
            !estado && verificada ? (
              <Link to="/empresa/vacantes/nueva" className="boton">
                Publicar vacante
              </Link>
            ) : undefined
          }
        />
      )}
      {vacantes.isSuccess && vacantes.data.contenido.length > 0 && (
        <>
          <ul className={estilos.lista}>
            {vacantes.data.contenido.map((vacante) => (
              <TarjetaVacante key={vacante.id} vacante={vacante} />
            ))}
          </ul>
          {vacantes.data.totalPaginas > 1 && (
            <nav className={estilos.paginacion} aria-label="Paginación">
              <button
                type="button"
                className="boton boton-secundario"
                disabled={pagina === 0}
                onClick={() => cambiar(estado, pagina - 1)}
              >
                Anterior
              </button>
              <span>
                Página {pagina + 1} de {vacantes.data.totalPaginas}
              </span>
              <button
                type="button"
                className="boton boton-secundario"
                disabled={pagina + 1 >= vacantes.data.totalPaginas}
                onClick={() => cambiar(estado, pagina + 1)}
              >
                Siguiente
              </button>
            </nav>
          )}
        </>
      )}
    </section>
  )
}

function TarjetaVacante({ vacante }: { vacante: VacanteDeEmpresaResumen }) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const visible = estadoVisible(vacante.estado, vacante.fechaCierre)

  const cierre = useMutation({
    mutationFn: () => cerrarVacante(vacante.id),
    onMutate: () => setError(null),
    onSuccess: (detalle) => {
      queryClient.setQueryData(CLAVES.vacante(vacante.id), detalle)
      return queryClient.invalidateQueries({ queryKey: CLAVES.misVacantes })
    },
    onError: (e) => setError(e.message),
  })

  const confirmarCierre = () => {
    if (window.confirm(`¿Cerrar "${vacante.titulo}"? Los candidatos ya no podrán postularse y no se puede reabrir.`)) {
      cierre.mutate()
    }
  }

  return (
    <li className={estilos.tarjeta}>
      <div className={estilos.tarjetaDatos}>
        <h2 className={estilos.tarjetaTitulo}>
          {vacante.titulo}
          <span className={`${estilos.insignia} ${estilos[`insignia${visible}`]}`}>{TEXTO_ESTADO[visible]}</span>
        </h2>
        <p className={estilos.detalles}>
          {TEXTO_TIPO[vacante.tipo]} · {TEXTO_MODALIDAD[vacante.modalidad]} · {vacante.ciudad}
        </p>
        <p className={estilos.detalles}>
          Publicada el {formatearFecha(vacante.fechaPublicacion)} · Cierra el {formatearFecha(vacante.fechaCierre)} ·{' '}
          {vacante.totalPostulaciones === 1 ? '1 postulación' : `${vacante.totalPostulaciones} postulaciones`}
        </p>
        {error && (
          <p className={formulario.errorCampo} role="alert">
            {error}
          </p>
        )}
      </div>
      <div className={estilos.acciones}>
        <Link to={`/empresa/vacantes/${vacante.id}/postulantes`} className="boton boton-secundario">
          Postulantes
        </Link>
        {vacante.estado === 'ABIERTA' && (
          <>
            <Link to={`/empresa/vacantes/${vacante.id}/editar`} className="boton boton-secundario">
              Editar
            </Link>
            <button type="button" className="boton boton-secundario" onClick={confirmarCierre} disabled={cierre.isPending}>
              {cierre.isPending ? 'Cerrando…' : 'Cerrar'}
            </button>
          </>
        )}
      </div>
    </li>
  )
}
