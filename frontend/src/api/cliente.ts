import type { ErrorRespuesta } from './tipos'

// Cliente HTTP único: toda llamada a la API pasa por aquí (CLAUDE.md).

const URL_BASE = `${import.meta.env.VITE_API_URL ?? ''}/api`

export const usarMocks = import.meta.env.VITE_USAR_MOCKS === 'true'

/** Error de la API con el código HTTP y el mensaje del formato uniforme del backend. */
export class ErrorApi extends Error {
  readonly status: number

  constructor(status: number, mensaje: string) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.status = status
  }
}

type ObtenerToken = () => string | null
type AlNoAutorizado = () => void

let obtenerToken: ObtenerToken = () => null
let alNoAutorizado: AlNoAutorizado = () => {}

/** Lo registra AuthProvider para que el cliente agregue el token y cierre la sesión ante un 401. */
export function configurarSesionCliente(token: ObtenerToken, noAutorizado: AlNoAutorizado) {
  obtenerToken = token
  alNoAutorizado = noAutorizado
}

interface OpcionesPeticion {
  metodo?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  cuerpo?: unknown
  parametros?: Record<string, string | number | boolean | undefined>
}

export async function peticion<T>(ruta: string, opciones: OpcionesPeticion = {}): Promise<T> {
  const { metodo = 'GET', cuerpo, parametros } = opciones
  const token = obtenerToken()

  const encabezados: Record<string, string> = { Accept: 'application/json' }
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json'
  if (token) encabezados.Authorization = `Bearer ${token}`

  let respuesta: Response
  try {
    respuesta = await fetch(URL_BASE + ruta + construirQuery(parametros), {
      method: metodo,
      headers: encabezados,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.')
  }

  if (respuesta.status === 401 && token) {
    // Token vencido o inválido: se cierra la sesión y las rutas protegidas llevan al login
    alNoAutorizado()
  }

  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, await leerMensajeError(respuesta))
  }

  if (respuesta.status === 204) return undefined as T
  return (await respuesta.json()) as T
}

function construirQuery(parametros?: OpcionesPeticion['parametros']): string {
  if (!parametros) return ''
  const query = new URLSearchParams()
  for (const [clave, valor] of Object.entries(parametros)) {
    if (valor !== undefined && valor !== '') query.set(clave, String(valor))
  }
  const texto = query.toString()
  return texto ? `?${texto}` : ''
}

async function leerMensajeError(respuesta: Response): Promise<string> {
  try {
    const cuerpo = (await respuesta.json()) as Partial<ErrorRespuesta>
    if (cuerpo.mensaje) return cuerpo.mensaje
  } catch {
    // La respuesta no traía el formato uniforme (por ejemplo, un error del proxy)
  }
  return respuesta.status >= 500
    ? 'Ocurrió un error en el servidor. Inténtalo más tarde.'
    : 'No se pudo completar la petición.'
}
