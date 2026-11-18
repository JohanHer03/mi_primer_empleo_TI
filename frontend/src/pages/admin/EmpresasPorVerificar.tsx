import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { listarEmpresas, verificarEmpresa } from '../../api/empresas'
import type { EmpresaResumen } from '../../api/tipos'
import { Cargando, EstadoVacio, MensajeError } from '../../components/Estados'
import formulario from '../../components/Formulario.module.css'
import { formatearFecha } from '../../components/formato'
import { Paginacion } from '../../components/Paginacion'
import comunes from '../../components/Paginas.module.css'

const CLAVE_EMPRESAS = ['empresas', 'admin'] as const

/** El filtro por defecto son las pendientes: es el trabajo del administrador. */
const FILTROS = [
  { valor: 'pendientes', texto: 'Por verificar', verificada: false },
  { valor: 'verificadas', texto: 'Verificadas', verificada: true },
  { valor: 'todas', texto: 'Todas', verificada: undefined },
] as const

/** HU-10: el administrador verifica empresas para que puedan publicar (regla 3; GET /api/empresas y PATCH verificar). */
export default function EmpresasPorVerificar() {
  const [parametros, setParametros] = useSearchParams()
  const filtro = FILTROS.find((f) => f.valor === parametros.get('filtro')) ?? FILTROS[0]
  const pagina = Math.max(0, Number(parametros.get('pagina')) || 0)

  const empresas = useQuery({
    queryKey: [...CLAVE_EMPRESAS, filtro.valor, pagina],
    queryFn: () => listarEmpresas(filtro.verificada, pagina),
    placeholderData: keepPreviousData,
  })

  const cambiar = (valor: string, nuevaPagina = 0) => {
    const siguientes = new URLSearchParams()
    if (valor !== 'pendientes') siguientes.set('filtro', valor)
    if (nuevaPagina > 0) siguientes.set('pagina', String(nuevaPagina))
    setParametros(siguientes)
  }

  return (
    <section className={comunes.pagina}>
      <h1>Empresas</h1>
      <p className={comunes.detalles}>
        Solo las empresas verificadas pueden publicar vacantes. Revisa la razón social, el NIT y el sitio web antes de
        verificar.
      </p>

      <div className={comunes.filtros} role="group" aria-label="Filtrar por verificación">
        {FILTROS.map((opcion) => (
          <button
            key={opcion.valor}
            type="button"
            className={comunes.filtro}
            aria-pressed={filtro.valor === opcion.valor}
            onClick={() => cambiar(opcion.valor)}
          >
            {opcion.texto}
          </button>
        ))}
      </div>

      {empresas.isPending && <Cargando texto="Cargando empresas…" />}
      {empresas.isError && <MensajeError error={empresas.error} alReintentar={() => empresas.refetch()} />}
      {empresas.isSuccess && empresas.data.contenido.length === 0 && (
        <EstadoVacio
          titulo={filtro.valor === 'pendientes' ? 'No hay empresas por verificar' : 'No hay empresas con este filtro'}
          descripcion={filtro.valor === 'pendientes' ? 'Todas las empresas registradas ya están verificadas.' : undefined}
        />
      )}
      {empresas.isSuccess && empresas.data.contenido.length > 0 && (
        <>
          <ul className={comunes.lista}>
            {empresas.data.contenido.map((empresa) => (
              <TarjetaEmpresa key={empresa.id} empresa={empresa} />
            ))}
          </ul>
          <Paginacion
            pagina={pagina}
            totalPaginas={empresas.data.totalPaginas}
            alCambiar={(nueva) => cambiar(filtro.valor, nueva)}
          />
        </>
      )}
    </section>
  )
}

function TarjetaEmpresa({ empresa }: { empresa: EmpresaResumen }) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)

  const verificacion = useMutation({
    mutationFn: () => verificarEmpresa(empresa.id),
    onMutate: () => setError(null),
    // Sale de "Por verificar" y entra en "Verificadas": se recargan todos los filtros
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CLAVE_EMPRESAS }),
    onError: (e) => setError(e.message),
  })

  const confirmar = () => {
    if (window.confirm(`¿Verificar a ${empresa.razonSocial} (NIT ${empresa.nit})? Podrá publicar vacantes y no se puede deshacer.`)) {
      verificacion.mutate()
    }
  }

  return (
    <li className={comunes.tarjeta}>
      <div className={comunes.tarjetaDatos}>
        <h2 className={comunes.tarjetaTitulo}>
          {empresa.razonSocial}
          <span className={`${comunes.insignia} ${empresa.verificada ? comunes.insigniaABIERTA : comunes.insigniaVENCIDA}`}>
            {empresa.verificada ? 'Verificada' : 'Por verificar'}
          </span>
        </h2>
        <p className={comunes.detalles}>
          NIT {empresa.nit} · {empresa.email}
        </p>
        <p className={comunes.detalles}>
          {[empresa.sector, empresa.ciudad].filter(Boolean).join(' · ')}
          {empresa.sitioWeb && (
            <>
              {(empresa.sector || empresa.ciudad) && ' · '}
              <a href={empresa.sitioWeb} target="_blank" rel="noreferrer">
                {empresa.sitioWeb}
              </a>
            </>
          )}
        </p>
        <p className={comunes.detalles}>Registrada el {formatearFecha(empresa.fechaCreacion)}</p>
        {error && (
          <p className={formulario.errorCampo} role="alert">
            {error}
          </p>
        )}
      </div>
      {!empresa.verificada && (
        <div className={comunes.acciones}>
          <button type="button" className="boton" onClick={confirmar} disabled={verificacion.isPending}>
            {verificacion.isPending ? 'Verificando…' : 'Verificar'}
          </button>
        </div>
      )}
    </li>
  )
}
