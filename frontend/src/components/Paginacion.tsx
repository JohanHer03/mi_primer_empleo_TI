import estilos from './Paginas.module.css'

interface PropsPaginacion {
  /** Página actual, desde 0 como en el backend. */
  pagina: number
  totalPaginas: number
  alCambiar: (pagina: number) => void
}

/** Anterior / siguiente de un listado paginado (`PaginaDto`); no se muestra si hay una sola página. */
export function Paginacion({ pagina, totalPaginas, alCambiar }: PropsPaginacion) {
  if (totalPaginas <= 1) return null
  return (
    <nav className={estilos.paginacion} aria-label="Paginación">
      <button type="button" className="boton boton-secundario" disabled={pagina === 0} onClick={() => alCambiar(pagina - 1)}>
        Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button
        type="button"
        className="boton boton-secundario"
        disabled={pagina + 1 >= totalPaginas}
        onClick={() => alCambiar(pagina + 1)}
      >
        Siguiente
      </button>
    </nav>
  )
}
