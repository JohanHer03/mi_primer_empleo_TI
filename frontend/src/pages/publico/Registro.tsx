import { Link, useSearchParams } from 'react-router'
import { usarMocks } from '../../api/cliente'
import formulario from '../../components/Formulario.module.css'
import { FormularioRegistroCandidato } from './registro/FormularioRegistroCandidato'
import { FormularioRegistroEmpresa } from './registro/FormularioRegistroEmpresa'
import estilos from './registro/Registro.module.css'

type TipoCuenta = 'candidato' | 'empresa'

const PESTANAS: { tipo: TipoCuenta; texto: string; descripcion: string }[] = [
  {
    tipo: 'candidato',
    texto: 'Soy candidato',
    descripcion: 'Para estudiantes de últimos semestres y egresados que buscan su primera práctica o empleo junior.',
  },
  {
    tipo: 'empresa',
    texto: 'Soy empresa',
    descripcion: 'Para empresas con área de TI que contratan practicantes o perfiles junior en Medellín.',
  },
]

/** HU-01 y HU-06: el tipo de cuenta va en ?tipo= para poder compartir el enlace directo. */
export default function Registro() {
  const [parametros, setParametros] = useSearchParams()
  const tipo: TipoCuenta = parametros.get('tipo') === 'empresa' ? 'empresa' : 'candidato'
  const actual = PESTANAS.find((pestana) => pestana.tipo === tipo)!

  return (
    <>
      <h1>Crear cuenta</h1>

      <div className={estilos.selector} role="tablist" aria-label="Tipo de cuenta">
        {PESTANAS.map((pestana) => (
          <button
            key={pestana.tipo}
            id={`pestana-${pestana.tipo}`}
            type="button"
            role="tab"
            aria-selected={pestana.tipo === tipo}
            aria-controls="panel-registro"
            className={estilos.pestana}
            onClick={() => setParametros({ tipo: pestana.tipo }, { replace: true })}
          >
            {pestana.texto}
          </button>
        ))}
      </div>

      <div id="panel-registro" role="tabpanel" aria-labelledby={`pestana-${tipo}`} className={estilos.panel}>
        <p className={estilos.descripcion}>{actual.descripcion}</p>
        {/* key: cada tipo de cuenta empieza con su formulario limpio */}
        {tipo === 'candidato' ? <FormularioRegistroCandidato key="candidato" /> : <FormularioRegistroEmpresa key="empresa" />}
      </div>

      <p className={formulario.nota}>
        ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
      </p>
      {usarMocks && (
        <p className={formulario.nota}>Modo simulado: la cuenta se guarda solo en este navegador.</p>
      )}
    </>
  )
}
