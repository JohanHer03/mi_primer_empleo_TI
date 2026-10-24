import type { Rol } from '../api/tipos'

export const INICIO_POR_ROL: Record<Rol, string> = {
  CANDIDATO: '/candidato',
  EMPRESA: '/empresa',
  ADMIN: '/admin',
}

/** Después del login vuelve a la página que se pidió, solo si pertenece al rol del usuario. */
export function destinoTrasLogin(rol: Rol, desde?: string): string {
  const inicio = INICIO_POR_ROL[rol]
  return desde && (desde === inicio || desde.startsWith(`${inicio}/`)) ? desde : inicio
}

export interface EnlaceMenu {
  ruta: string
  texto: string
}

export const MENU_POR_ROL: Record<Rol, EnlaceMenu[]> = {
  CANDIDATO: [
    { ruta: '/candidato', texto: 'Inicio' },
    { ruta: '/candidato/vacantes', texto: 'Buscar vacantes' },
    { ruta: '/candidato/postulaciones', texto: 'Mis postulaciones' },
    { ruta: '/candidato/perfil', texto: 'Mi perfil' },
  ],
  EMPRESA: [
    { ruta: '/empresa', texto: 'Inicio' },
    { ruta: '/empresa/vacantes', texto: 'Mis vacantes' },
    { ruta: '/empresa/perfil', texto: 'Perfil de empresa' },
  ],
  ADMIN: [
    { ruta: '/admin/habilidades', texto: 'Habilidades' },
    { ruta: '/admin/empresas', texto: 'Empresas por verificar' },
  ],
}
