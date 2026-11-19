import { useId, useState } from 'react'
import { Link } from 'react-router'
import '@fontsource-variable/mona-sans/wdth.css'
import { calcularCompatibilidadMock } from '../../api/mocks/compatibilidad'
import type { CandidatoHabilidad, EstadoPostulacion, HabilidadRequerida, NivelHabilidad } from '../../api/tipos'
import { TEXTO_ESTADO_POSTULACION, TEXTO_NIVEL, formatearCompatibilidad } from '../../components/formato'
import estilos from './Landing.module.css'

// Contenido tomado de docs/fundamentacion.md (problema, diferencial, usuarios y ODS 8).

const CIFRAS = [
  {
    valor: '2,33 millones',
    texto: 'de jóvenes de 15 a 28 años no estudian ni trabajan en Colombia (≈ 21 %).',
    fuente: 'DANE, jun–ago 2026',
    url: 'https://www.larepublica.co/economia/el-numero-de-jovenes-ninis-alcanzo-su-nivel-mas-bajo-en-casi-una-decada-4503982',
  },
  {
    valor: '14,7 %',
    texto: 'de desempleo juvenil en el país, casi el doble del desempleo general.',
    fuente: 'DANE, abr–jun 2026',
    url: 'https://www.portafolio.co/economia/desempleo-en-colombia-en-junio-se-situo-en-8-cifra-similar-a-la-de-mayo-499400',
  },
  {
    valor: '12,9 %',
    texto: 'de desempleo juvenil en Medellín, aun siendo la ciudad con menor desempleo general.',
    fuente: 'DANE, abr–jun 2026',
    url: 'https://www.elpais.com.co/economia/tasa-de-desempleo-en-colombia-se-mantiene-estable-y-se-ubico-en-8-en-junio-de-2026-3146.html',
  },
  {
    valor: '68.000 a 112.000',
    texto: 'desarrolladores de brecha proyectada en Colombia (estimación de MinTIC).',
    fuente: 'MinTIC, proyección',
    url: 'https://www.larepublica.co/alta-gerencia/de-no-tomar-acciones-colombia-tendria-deficit-de-112-000-desarrolladores-en-2025-3440141',
  },
]

const PASOS = [
  {
    titulo: 'Registra tus habilidades',
    texto: 'Lenguajes, frameworks, bases de datos y habilidades blandas, cada una con su nivel: básico, intermedio o avanzado.',
  },
  {
    titulo: 'Mira tu compatibilidad',
    texto: 'Cada vacante te muestra un porcentaje y la lista de lo que te falta para encajar, habilidad por habilidad.',
  },
  {
    titulo: 'Postúlate y haz seguimiento',
    texto: 'La empresa mueve tu postulación por cada etapa y tú ves en cuál va.',
  },
]

const ETAPAS: EstadoPostulacion[] = ['ENVIADA', 'EN_REVISION', 'ENTREVISTA', 'CONTRATADO']

const BENEFICIOS_EMPRESA = [
  'Publica vacantes de práctica o junior con las habilidades que pides y su nivel.',
  'Recibe postulantes ordenados por compatibilidad, no por orden de llegada.',
  'Mueve cada postulación a revisión, entrevista o contratado, o recházala.',
]

// Valores posibles con la fórmula real para la vacante de ejemplo (pesos 2, 2, 2 y 1).
const POSTULANTES_EJEMPLO: { nombre: string; compatibilidad: number; estado: EstadoPostulacion }[] = [
  { nombre: 'Laura M.', compatibilidad: 100, estado: 'ENTREVISTA' },
  { nombre: 'Santiago R.', compatibilidad: 85.7, estado: 'EN_REVISION' },
  { nombre: 'Valentina G.', compatibilidad: 71.4, estado: 'ENVIADA' },
]

// Vacante de ejemplo del simulador. Los ids solo sirven para cruzar con los niveles elegidos.
const VACANTE_EJEMPLO: HabilidadRequerida[] = [
  { habilidadId: 'java', nombre: 'Java', categoria: 'LENGUAJE', nivelRequerido: 'INTERMEDIO', obligatoria: true },
  { habilidadId: 'spring', nombre: 'Spring Boot', categoria: 'FRAMEWORK', nivelRequerido: 'INTERMEDIO', obligatoria: true },
  { habilidadId: 'docker', nombre: 'Docker', categoria: 'HERRAMIENTA', nivelRequerido: 'INTERMEDIO', obligatoria: true },
  { habilidadId: 'git', nombre: 'Git', categoria: 'HERRAMIENTA', nivelRequerido: 'BASICO', obligatoria: false },
]

type NivelElegido = NivelHabilidad | 'NINGUNO'

const OPCIONES_NIVEL: { valor: NivelElegido; texto: string }[] = [
  { valor: 'NINGUNO', texto: 'Ninguno' },
  { valor: 'BASICO', texto: TEXTO_NIVEL.BASICO },
  { valor: 'INTERMEDIO', texto: TEXTO_NIVEL.INTERMEDIO },
  { valor: 'AVANZADO', texto: TEXTO_NIVEL.AVANZADO },
]

const VALOR_NIVEL: Record<NivelElegido, number> = { NINGUNO: 0, BASICO: 1, INTERMEDIO: 2, AVANZADO: 3 }

// Con estos niveles el ejemplo da 85,7 %: (2·1 + 2·1 + 2·0,5 + 1·1) / 7
const NIVELES_INICIALES: Record<string, NivelElegido> = {
  java: 'AVANZADO',
  spring: 'INTERMEDIO',
  docker: 'BASICO',
  git: 'INTERMEDIO',
}

interface Resultado {
  compatibilidad: number
  faltantes: { nombre: string; requerido: NivelHabilidad; tiene: NivelElegido }[]
}

/** Usa la misma fórmula de CalculadoraCompatibilidad (copia del modo simulado). */
function calcular(niveles: Record<string, NivelElegido>): Resultado {
  const delCandidato: CandidatoHabilidad[] = VACANTE_EJEMPLO.flatMap((habilidad) => {
    const nivel = niveles[habilidad.habilidadId]
    if (nivel === 'NINGUNO') return []
    return [{ habilidadId: habilidad.habilidadId, nombre: habilidad.nombre, categoria: habilidad.categoria, nivel }]
  })
  const faltantes = VACANTE_EJEMPLO.filter(
    (habilidad) => VALOR_NIVEL[niveles[habilidad.habilidadId]] < VALOR_NIVEL[habilidad.nivelRequerido],
  ).map((habilidad) => ({
    nombre: habilidad.nombre,
    requerido: habilidad.nivelRequerido,
    tiene: niveles[habilidad.habilidadId],
  }))
  return { compatibilidad: calcularCompatibilidadMock(VACANTE_EJEMPLO, delCandidato), faltantes }
}

function textoFaltante({ nombre, requerido, tiene }: Resultado['faltantes'][number]): string {
  const pide = TEXTO_NIVEL[requerido].toLowerCase()
  if (tiene === 'NINGUNO') return `${nombre}: la vacante pide nivel ${pide} y no la tienes.`
  return `${nombre}: la vacante pide nivel ${pide} y tienes ${TEXTO_NIVEL[tiene].toLowerCase()}.`
}

const RADIO_ANILLO = 44
const CIRCUNFERENCIA = 2 * Math.PI * RADIO_ANILLO

function Anillo({ valor }: { valor: number }) {
  const [entero, decimal] = formatearCompatibilidad(valor).replace(' %', '').split(',')
  return (
    <div className={estilos.anillo} aria-hidden="true">
      <svg viewBox="0 0 100 100">
        <circle className={estilos.anilloPista} cx="50" cy="50" r={RADIO_ANILLO} />
        <circle
          className={estilos.anilloValor}
          cx="50"
          cy="50"
          r={RADIO_ANILLO}
          strokeDasharray={CIRCUNFERENCIA}
          strokeDashoffset={CIRCUNFERENCIA * (1 - valor / 100)}
        />
      </svg>
      <p className={estilos.anilloTexto}>
        {entero}
        <span>,{decimal} %</span>
      </p>
    </div>
  )
}

interface PropsSimulador {
  niveles: Record<string, NivelElegido>
  resultado: Resultado
  alCambiar: (habilidadId: string, nivel: NivelElegido) => void
}

function Simulador({ niveles, resultado, alCambiar }: PropsSimulador) {
  const id = useId()
  return (
    <section className={estilos.simulador} aria-labelledby={`${id}-titulo`}>
      <div className={estilos.simuladorEncabezado}>
        <div>
          <h2 id={`${id}-titulo`} className={estilos.simuladorTitulo}>
            Desarrollador/a Java junior
          </h2>
          <p className={estilos.simuladorMeta}>Vacante de ejemplo en Medellín, modalidad híbrida</p>
          <p className={estilos.simuladorInstruccion}>Cambia tus niveles y mira cómo cambia tu compatibilidad.</p>
        </div>
        <Anillo valor={resultado.compatibilidad} />
      </div>

      <div className={estilos.habilidades}>
        {VACANTE_EJEMPLO.map((habilidad) => {
          const elegido = niveles[habilidad.habilidadId]
          const cubre = Math.min(1, VALOR_NIVEL[elegido] / VALOR_NIVEL[habilidad.nivelRequerido])
          return (
            <fieldset key={habilidad.habilidadId} className={estilos.habilidad}>
              <legend className={estilos.habilidadNombre}>
                {habilidad.nombre}
                <span className={estilos.habilidadPide}>
                  {habilidad.obligatoria ? 'Obligatoria' : 'Deseable'}, pide {TEXTO_NIVEL[habilidad.nivelRequerido].toLowerCase()}
                </span>
              </legend>
              <div className={estilos.segmentos}>
                {OPCIONES_NIVEL.map((opcion) => (
                  <label key={opcion.valor} className={estilos.segmento}>
                    <input
                      type="radio"
                      name={`${id}-${habilidad.habilidadId}`}
                      value={opcion.valor}
                      checked={elegido === opcion.valor}
                      onChange={() => alCambiar(habilidad.habilidadId, opcion.valor)}
                    />
                    <span>{opcion.texto}</span>
                  </label>
                ))}
              </div>
              <div className={estilos.medidor} aria-hidden="true">
                <div
                  className={cubre < 1 ? estilos.medidorIncompleto : estilos.medidorCompleto}
                  style={{ width: `${cubre * 100}%` }}
                />
              </div>
            </fieldset>
          )
        })}
      </div>

      <div className={resultado.faltantes.length > 0 ? estilos.falta : estilos.completo}>
        <p className={estilos.faltaTitulo}>
          {resultado.faltantes.length > 0 ? 'Lo que te falta' : 'Cumples todo lo que pide la vacante'}
        </p>
        {resultado.faltantes.length > 0 && (
          <ul>
            {resultado.faltantes.map((faltante) => (
              <li key={faltante.nombre}>{textoFaltante(faltante)}</li>
            ))}
          </ul>
        )}
      </div>

      <p className={estilos.oculto} aria-live="polite">
        Compatibilidad del {formatearCompatibilidad(resultado.compatibilidad)}.{' '}
        {resultado.faltantes.length > 0
          ? `Te falta: ${resultado.faltantes.map((f) => f.nombre).join(', ')}.`
          : 'No te falta nada.'}
      </p>
    </section>
  )
}

export default function Landing() {
  const [niveles, setNiveles] = useState(NIVELES_INICIALES)
  const resultado = calcular(niveles)

  function cambiarNivel(habilidadId: string, nivel: NivelElegido) {
    setNiveles((anteriores) => ({ ...anteriores, [habilidadId]: nivel }))
  }

  return (
    <div className={estilos.pagina}>
      <header className={estilos.encabezado}>
        <nav className={estilos.barra} aria-label="Principal">
          <Link to="/" className={estilos.marca}>
            Primer Empleo TI
          </Link>
          <ul className={estilos.enlaces}>
            <li>
              <a href="#como-funciona">Cómo funciona</a>
            </li>
            <li>
              <a href="#empresas">Para empresas</a>
            </li>
          </ul>
          <div className={estilos.acciones}>
            <Link to="/login" className="boton boton-secundario">
              Iniciar sesión
            </Link>
            <Link to="/registro" className="boton">
              Crear cuenta
            </Link>
          </div>
        </nav>
      </header>

      <main>
        <section className={estilos.hero} aria-labelledby="hero-titulo">
          <div className={estilos.heroTexto}>
            <h1 id="hero-titulo" className={estilos.heroTitulo}>
              Tu primer empleo en tecnología, sin adivinar qué te falta
            </h1>
            <p className={estilos.heroDescripcion}>
              Vacantes de práctica y nivel junior en Medellín para estudiantes de últimos semestres y recién graduados de
              software. En cada una ves qué tan compatible eres y qué habilidades te faltan para encajar.
            </p>
            <div className={estilos.heroAcciones}>
              <Link to="/registro" className="boton">
                Crear cuenta de candidato
              </Link>
              <Link to="/registro?tipo=empresa" className="boton boton-secundario">
                Soy empresa
              </Link>
            </div>
          </div>
          <Simulador niveles={niveles} resultado={resultado} alCambiar={cambiarNivel} />
        </section>

        <section className={estilos.seccion} aria-labelledby="problema-titulo">
          <div className={estilos.seccionIntro}>
            <h2 id="problema-titulo">La paradoja del junior</h2>
            <p className={estilos.seccionDescripcion}>
              Las vacantes “junior” piden de 6 meses a 2 años de experiencia, mientras el sector TI reporta falta de
              talento. El problema no es solo encontrar desarrolladores: es conectar al recién graduado con su primer
              empleo.
            </p>
          </div>
          <ul className={estilos.cifras}>
            {CIFRAS.map((cifra) => (
              <li key={cifra.valor} className={estilos.cifra}>
                <p className={estilos.cifraValor}>{cifra.valor}</p>
                <div>
                  <p className={estilos.cifraTexto}>{cifra.texto}</p>
                  <a className={estilos.cifraFuente} href={cifra.url} target="_blank" rel="noopener noreferrer">
                    {cifra.fuente}
                    <span className={estilos.oculto}> (abre en una pestaña nueva)</span>
                  </a>
                </div>
              </li>
            ))}
          </ul>
        </section>

        <section id="como-funciona" className={estilos.seccion} aria-labelledby="pasos-titulo">
          <h2 id="pasos-titulo">Cómo funciona para candidatos</h2>
          <ol className={estilos.pasos}>
            {PASOS.map((paso, indice) => (
              <li key={paso.titulo} className={estilos.paso}>
                <span className={estilos.pasoNumero} aria-hidden="true">
                  {indice + 1}
                </span>
                <h3>{paso.titulo}</h3>
                <p>{paso.texto}</p>
              </li>
            ))}
          </ol>
          <ol className={estilos.etapas} aria-label="Etapas de una postulación">
            {ETAPAS.map((etapa) => (
              <li key={etapa}>{TEXTO_ESTADO_POSTULACION[etapa]}</li>
            ))}
          </ol>
        </section>

        <section className={estilos.diferencial} aria-labelledby="diferencial-titulo">
          <div className={estilos.diferencialContenido}>
            <h2 id="diferencial-titulo">Saber cuánto encajas no basta. Necesitas saber qué te falta.</h2>
            <p className={estilos.seccionDescripcion}>
              La misma vacante del ejemplo, con los niveles que elegiste arriba. Además, solo verás prácticas y empleos de
              nivel inicial publicados por empresas verificadas.
            </p>
            <div className={estilos.comparacion}>
              <div className={estilos.comparacionOtra}>
                <h3>Otras plataformas</h3>
                <p className={estilos.comparacionValor}>{formatearCompatibilidad(resultado.compatibilidad)}</p>
                <p>de compatibilidad, sin decirte por qué.</p>
              </div>
              <div className={estilos.comparacionNuestra}>
                <h3>Primer Empleo TI</h3>
                <p className={estilos.comparacionValor}>{formatearCompatibilidad(resultado.compatibilidad)}</p>
                {resultado.faltantes.length > 0 ? (
                  <ul>
                    {resultado.faltantes.map((faltante) => (
                      <li key={faltante.nombre}>{textoFaltante(faltante)}</li>
                    ))}
                  </ul>
                ) : (
                  <p>Cumples todo lo que pide: es buen momento para postularte.</p>
                )}
              </div>
            </div>
          </div>
        </section>

        <section id="empresas" className={`${estilos.seccion} ${estilos.empresas}`} aria-labelledby="empresas-titulo">
          <div>
            <h2 id="empresas-titulo">Para empresas que contratan talento junior</h2>
            <p className={estilos.seccionDescripcion}>
              Encuentra practicantes y desarrolladores junior en Medellín que ya conocen las habilidades que necesitas.
            </p>
            <ul className={estilos.beneficios}>
              {BENEFICIOS_EMPRESA.map((beneficio) => (
                <li key={beneficio}>{beneficio}</li>
              ))}
            </ul>
            <Link to="/registro?tipo=empresa" className="boton">
              Registrar mi empresa
            </Link>
          </div>
          <figure className={estilos.postulantes}>
            <figcaption>Postulantes de Desarrollador/a Java junior, de más a menos compatible</figcaption>
            <ol>
              {POSTULANTES_EJEMPLO.map((postulante) => (
                <li key={postulante.nombre}>
                  <span className={estilos.postulanteNombre}>{postulante.nombre}</span>
                  <span className={estilos.postulanteEstado}>{TEXTO_ESTADO_POSTULACION[postulante.estado]}</span>
                  <span className={estilos.postulanteValor}>{formatearCompatibilidad(postulante.compatibilidad)}</span>
                  <span className={estilos.postulanteBarra} aria-hidden="true">
                    <span style={{ width: `${postulante.compatibilidad}%` }} />
                  </span>
                </li>
              ))}
            </ol>
          </figure>
        </section>

        <section className={estilos.ods} aria-labelledby="ods-titulo">
          <p className={estilos.odsInsignia} aria-hidden="true">
            ODS 8
          </p>
          <div>
            <h2 id="ods-titulo">Trabajo decente y crecimiento económico</h2>
            <p>
              Aporta a las metas 8.6, reducir la proporción de jóvenes que no estudian ni trabajan, y 8.b, estrategias de
              empleo juvenil, desde el sector con más demanda de la ciudad.
            </p>
          </div>
        </section>
      </main>

      <footer className={estilos.pie}>
        <p>Proyecto académico de Ingeniería Informática, Medellín 2026</p>
        <p>
          <Link to="/login">Iniciar sesión</Link> o <Link to="/registro">crear cuenta</Link>
        </p>
      </footer>
    </div>
  )
}
