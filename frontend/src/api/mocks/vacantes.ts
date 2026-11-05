import { hoyEnColombia } from '../../components/formato'
import { ErrorApi } from '../cliente'
import type {
  EstadoVacante,
  HabilidadRequerida,
  PaginaDto,
  VacanteDeEmpresaResumen,
  VacanteDetalle,
  VacantePeticion,
} from '../tipos'
import { perfilEmpresaActualMock } from './empresas'
import { CATALOGO_MOCK } from './habilidades'
import { esperar, guardar, leerGuardado, usuarioActualMock } from './utilidades'

// Simulación de las vacantes de empresa (contrato 3.2 y 4.4), con las reglas 3 y 4 y los mismos mensajes del backend.

const CLAVE_VACANTES = 'primer-empleo.mock.vacantes'

interface VacanteGuardada {
  detalle: VacanteDetalle
  totalPostulaciones: number
}

const leerVacantes = () => leerGuardado<VacanteGuardada[]>(CLAVE_VACANTES, vacantesDemo)

function vacantesDemo(): VacanteGuardada[] {
  const empresa = { id: '55555555-5555-5555-5555-555555555555', razonSocial: 'Software Medellín S.A.S.' }
  const enDias = (dias: number) => {
    const fecha = new Date(`${hoyEnColombia()}T12:00:00Z`)
    fecha.setUTCDate(fecha.getUTCDate() + dias)
    return fecha.toISOString().slice(0, 10)
  }
  const base = {
    descripcion: 'Apoyarás el desarrollo de APIs REST con Spring Boot para clientes del sector financiero en Medellín.',
    ciudad: 'Medellín',
    empresa,
  }
  return [
    {
      totalPostulaciones: 12,
      detalle: {
        ...base,
        id: '66666666-0000-4000-8000-000000000001',
        titulo: 'Desarrollador Java junior',
        tipo: 'JUNIOR',
        modalidad: 'HIBRIDA',
        salarioMin: 2800000,
        salarioMax: 3500000,
        estado: 'ABIERTA',
        fechaPublicacion: `${enDias(-3)}T15:00:00Z`,
        fechaCierre: enDias(27),
        habilidades: requeridas(['Java', 'INTERMEDIO', true], ['Spring Boot', 'BASICO', true], ['Git', 'BASICO', false]),
      },
    },
    {
      totalPostulaciones: 4,
      detalle: {
        ...base,
        id: '66666666-0000-4000-8000-000000000002',
        titulo: 'Practicante de desarrollo web',
        tipo: 'PRACTICA',
        modalidad: 'REMOTA',
        salarioMin: 1423500,
        salarioMax: null,
        estado: 'ABIERTA',
        fechaPublicacion: `${enDias(-40)}T15:00:00Z`,
        fechaCierre: enDias(-2),
        habilidades: requeridas(['React', 'BASICO', true], ['TypeScript', 'BASICO', false]),
      },
    },
    {
      totalPostulaciones: 20,
      detalle: {
        ...base,
        id: '66666666-0000-4000-8000-000000000003',
        titulo: 'Analista de datos junior',
        tipo: 'JUNIOR',
        modalidad: 'PRESENCIAL',
        salarioMin: null,
        salarioMax: null,
        estado: 'CERRADA',
        fechaPublicacion: `${enDias(-60)}T15:00:00Z`,
        fechaCierre: enDias(-30),
        habilidades: requeridas(['SQL', 'INTERMEDIO', true], ['Python', 'BASICO', true]),
      },
    },
  ]
}

function requeridas(...lista: [string, HabilidadRequerida['nivelRequerido'], boolean][]): HabilidadRequerida[] {
  return lista.map(([nombre, nivelRequerido, obligatoria]) => {
    const habilidad = CATALOGO_MOCK.find((h) => h.nombre === nombre)!
    return { habilidadId: habilidad.id, nombre, categoria: habilidad.categoria, nivelRequerido, obligatoria }
  })
}

/** Mismas reglas del dominio del backend (contrato 4.4). */
function validar(datos: VacantePeticion): HabilidadRequerida[] {
  if (datos.fechaCierre < hoyEnColombia()) throw new ErrorApi(400, 'fechaCierre: debe ser hoy o una fecha posterior')
  if (datos.salarioMin !== null && datos.salarioMax !== null && datos.salarioMin > datos.salarioMax) {
    throw new ErrorApi(400, 'salarioMin: no puede ser mayor que salarioMax')
  }
  const ids = datos.habilidades.map((h) => h.habilidadId)
  if (new Set(ids).size !== ids.length) throw new ErrorApi(400, 'habilidades: no se puede repetir una habilidad')
  if (!datos.habilidades.some((h) => h.obligatoria)) {
    throw new ErrorApi(400, 'habilidades: al menos una debe ser obligatoria')
  }
  return datos.habilidades
    .map((pedida) => {
      const habilidad = CATALOGO_MOCK.find((h) => h.id === pedida.habilidadId)
      if (!habilidad) throw new ErrorApi(400, `habilidades: la habilidad ${pedida.habilidadId} no existe`)
      return { ...pedida, nombre: habilidad.nombre, categoria: habilidad.categoria }
    })
    .sort((a, b) => Number(b.obligatoria) - Number(a.obligatoria) || a.nombre.localeCompare(b.nombre, 'es'))
}

/** La vacante de la empresa del usuario: 404 si no existe, 403 si es de otra (regla 4). */
function vacantePropia(id: string): VacanteGuardada {
  const empresa = perfilEmpresaActualMock()
  const guardada = leerVacantes().find((v) => v.detalle.id === id)
  if (!guardada) throw new ErrorApi(404, 'La vacante no existe')
  if (guardada.detalle.empresa.id !== empresa.id) throw new ErrorApi(403, 'La vacante pertenece a otra empresa')
  return guardada
}

function reemplazar(actualizada: VacanteGuardada) {
  guardar(CLAVE_VACANTES, leerVacantes().map((v) => (v.detalle.id === actualizada.detalle.id ? actualizada : v)))
}

export async function publicarVacanteMock(datos: VacantePeticion): Promise<VacanteDetalle> {
  await esperar()
  const empresa = perfilEmpresaActualMock()
  if (!empresa.verificada) throw new ErrorApi(403, 'Tu empresa debe estar verificada para publicar vacantes')
  const detalle: VacanteDetalle = {
    ...datos,
    id: crypto.randomUUID(),
    estado: 'ABIERTA',
    fechaPublicacion: new Date().toISOString(),
    empresa: { id: empresa.id, razonSocial: empresa.razonSocial },
    habilidades: validar(datos),
  }
  guardar(CLAVE_VACANTES, [{ detalle, totalPostulaciones: 0 }, ...leerVacantes()])
  return detalle
}

export async function editarVacanteMock(id: string, datos: VacantePeticion): Promise<VacanteDetalle> {
  await esperar()
  const actual = vacantePropia(id)
  if (actual.detalle.estado === 'CERRADA') throw new ErrorApi(409, 'No se puede editar una vacante cerrada')
  const detalle = { ...actual.detalle, ...datos, habilidades: validar(datos) }
  reemplazar({ ...actual, detalle })
  return detalle
}

export async function cerrarVacanteMock(id: string): Promise<VacanteDetalle> {
  await esperar()
  const actual = vacantePropia(id)
  const detalle: VacanteDetalle = { ...actual.detalle, estado: 'CERRADA' }
  reemplazar({ ...actual, detalle })
  return detalle
}

export async function obtenerVacanteMock(id: string): Promise<VacanteDetalle> {
  await esperar()
  if (usuarioActualMock().rol === 'EMPRESA') return vacantePropia(id).detalle
  const guardada = leerVacantes().find((v) => v.detalle.id === id)
  if (!guardada) throw new ErrorApi(404, 'La vacante no existe')
  return guardada.detalle
}

export async function listarMisVacantesMock(
  estado: EstadoVacante | undefined,
  pagina: number,
  tamano: number,
): Promise<PaginaDto<VacanteDeEmpresaResumen>> {
  await esperar()
  const empresa = perfilEmpresaActualMock()
  const propias = leerVacantes()
    .filter((v) => v.detalle.empresa.id === empresa.id && (!estado || v.detalle.estado === estado))
    .sort((a, b) => b.detalle.fechaPublicacion.localeCompare(a.detalle.fechaPublicacion))
  const contenido = propias.slice(pagina * tamano, (pagina + 1) * tamano).map(({ detalle, totalPostulaciones }) => ({
    id: detalle.id,
    titulo: detalle.titulo,
    tipo: detalle.tipo,
    modalidad: detalle.modalidad,
    ciudad: detalle.ciudad,
    estado: detalle.estado,
    fechaPublicacion: detalle.fechaPublicacion,
    fechaCierre: detalle.fechaCierre,
    totalPostulaciones,
  }))
  return {
    contenido,
    pagina,
    tamano,
    totalElementos: propias.length,
    totalPaginas: Math.ceil(propias.length / tamano),
  }
}
