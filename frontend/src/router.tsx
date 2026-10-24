import { createBrowserRouter, Navigate } from 'react-router'
import { InicioSegunSesion } from './auth/InicioSegunSesion'
import { RutaProtegida, SoloSinSesion } from './auth/RutaProtegida'
import { ErrorDeRuta } from './components/ErrorDeRuta'
import { LayoutApp } from './layouts/LayoutApp'
import { LayoutPublico } from './layouts/LayoutPublico'
import EmpresasPorVerificar from './pages/admin/EmpresasPorVerificar'
import Habilidades from './pages/admin/Habilidades'
import BuscarVacantes from './pages/candidato/BuscarVacantes'
import DetalleVacante from './pages/candidato/DetalleVacante'
import InicioCandidato from './pages/candidato/InicioCandidato'
import MisPostulaciones from './pages/candidato/MisPostulaciones'
import PerfilCandidato from './pages/candidato/PerfilCandidato'
import FormularioVacante from './pages/empresa/FormularioVacante'
import InicioEmpresa from './pages/empresa/InicioEmpresa'
import MisVacantes from './pages/empresa/MisVacantes'
import PerfilEmpresa from './pages/empresa/PerfilEmpresa'
import Postulantes from './pages/empresa/Postulantes'
import Login from './pages/publico/Login'
import NoEncontrada from './pages/publico/NoEncontrada'
import Registro from './pages/publico/Registro'

export const router = createBrowserRouter([
  {
    errorElement: <ErrorDeRuta />,
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
              { index: true, element: <InicioCandidato /> },
              { path: 'perfil', element: <PerfilCandidato /> },
              { path: 'vacantes', element: <BuscarVacantes /> },
              { path: 'vacantes/:id', element: <DetalleVacante /> },
              { path: 'postulaciones', element: <MisPostulaciones /> },
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
              { index: true, element: <InicioEmpresa /> },
              { path: 'perfil', element: <PerfilEmpresa /> },
              { path: 'vacantes', element: <MisVacantes /> },
              { path: 'vacantes/nueva', element: <FormularioVacante /> },
              { path: 'vacantes/:id/editar', element: <FormularioVacante /> },
              { path: 'vacantes/:id/postulantes', element: <Postulantes /> },
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
              { path: 'habilidades', element: <Habilidades /> },
              { path: 'empresas', element: <EmpresasPorVerificar /> },
            ],
          },
        ],
      },
      { path: '*', element: <NoEncontrada /> },
    ],
  },
])
