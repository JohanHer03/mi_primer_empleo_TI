import type { ComponentType } from 'react'
import { createBrowserRouter, Navigate } from 'react-router'
import { InicioSegunSesion } from './auth/InicioSegunSesion'
import { RutaProtegida, SoloSinSesion } from './auth/RutaProtegida'
import { ErrorDeRuta } from './components/ErrorDeRuta'
import { Cargando } from './components/Estados'
import { LayoutApp } from './layouts/LayoutApp'
import { LayoutPublico } from './layouts/LayoutPublico'
import Login from './pages/publico/Login'
import NoEncontrada from './pages/publico/NoEncontrada'
import Registro from './pages/publico/Registro'

/**
 * Las páginas de cada rol se cargan al entrar a ellas (división del bundle por rutas): quien visita la landing
 * no descarga las pantallas de la empresa ni del administrador. Las públicas van en el bundle inicial.
 */
const pagina = (cargar: () => Promise<{ default: ComponentType }>) => async () => ({
  Component: (await cargar()).default,
})

export const router = createBrowserRouter([
  {
    errorElement: <ErrorDeRuta />,
    // Se ve solo si se recarga directamente una ruta cuya página aún no se descargó
    hydrateFallbackElement: <Cargando />,
    children: [
      { index: true, element: <InicioSegunSesion /> },
      {
        element: <SoloSinSesion />,
        children: [
          {
            element: <LayoutPublico />,
            children: [
              { path: 'login', element: <Login /> },
              { path: 'registro', element: <Registro /> },
            ],
          },
        ],
      },
      {
        path: 'candidato',
        element: <RutaProtegida roles={['CANDIDATO']} />,
        children: [
          {
            element: <LayoutApp />,
            children: [
              { index: true, lazy: pagina(() => import('./pages/candidato/InicioCandidato')) },
              { path: 'perfil', lazy: pagina(() => import('./pages/candidato/PerfilCandidato')) },
              { path: 'vacantes', lazy: pagina(() => import('./pages/candidato/BuscarVacantes')) },
              { path: 'vacantes/:id', lazy: pagina(() => import('./pages/candidato/DetalleVacante')) },
              { path: 'postulaciones', lazy: pagina(() => import('./pages/candidato/MisPostulaciones')) },
            ],
          },
        ],
      },
      {
        path: 'empresa',
        element: <RutaProtegida roles={['EMPRESA']} />,
        children: [
          {
            element: <LayoutApp />,
            children: [
              { index: true, lazy: pagina(() => import('./pages/empresa/InicioEmpresa')) },
              { path: 'perfil', lazy: pagina(() => import('./pages/empresa/PerfilEmpresa')) },
              { path: 'vacantes', lazy: pagina(() => import('./pages/empresa/MisVacantes')) },
              { path: 'vacantes/nueva', lazy: pagina(() => import('./pages/empresa/FormularioVacante')) },
              { path: 'vacantes/:id/editar', lazy: pagina(() => import('./pages/empresa/FormularioVacante')) },
              { path: 'vacantes/:id/postulantes', lazy: pagina(() => import('./pages/empresa/Postulantes')) },
            ],
          },
        ],
      },
      {
        path: 'admin',
        element: <RutaProtegida roles={['ADMIN']} />,
        children: [
          {
            element: <LayoutApp />,
            children: [
              { index: true, element: <Navigate to="habilidades" replace /> },
              { path: 'habilidades', lazy: pagina(() => import('./pages/admin/Habilidades')) },
              { path: 'empresas', lazy: pagina(() => import('./pages/admin/EmpresasPorVerificar')) },
            ],
          },
        ],
      },
      { path: '*', element: <NoEncontrada /> },
    ],
  },
])
