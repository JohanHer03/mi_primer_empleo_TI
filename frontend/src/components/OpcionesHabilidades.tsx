import type { CategoriaHabilidad, Habilidad } from '../api/tipos'
import { TEXTO_CATEGORIA } from './formato'

/** Opciones de un <select> de habilidades, agrupadas por categoría en el orden del catálogo (contrato 4.1). */
export function OpcionesHabilidades({ catalogo }: { catalogo: Habilidad[] }) {
  return (Object.entries(TEXTO_CATEGORIA) as [CategoriaHabilidad, string][]).map(([categoria, texto]) => (
    <optgroup key={categoria} label={texto}>
      {catalogo
        .filter((habilidad) => habilidad.categoria === categoria)
        .map((habilidad) => (
          <option key={habilidad.id} value={habilidad.id}>
            {habilidad.nombre}
          </option>
        ))}
    </optgroup>
  ))
}
