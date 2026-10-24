import { Link } from 'react-router'
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
    texto: 'Sigue el estado de cada postulación: enviada, en revisión, entrevista o contratado.',
  },
]

const BENEFICIOS_EMPRESA = [
  'Publica vacantes de práctica o junior con las habilidades que pides y su nivel.',
  'Recibe postulantes ordenados por compatibilidad, no por orden de llegada.',
  'Gestiona cada postulación: en revisión, entrevista, rechazada o contratado.',
]

// Ejemplo calculado con la fórmula real: (2·1 + 2·1 + 2·0,5 + 1·1) / (2 + 2 + 2 + 1) = 85,7 %
const HABILIDADES_EJEMPLO = [
  { nombre: 'Java', requerido: 'Intermedio', tienes: 'Avanzado', cubre: 1, obligatoria: true },
  { nombre: 'Spring Boot', requerido: 'Intermedio', tienes: 'Intermedio', cubre: 1, obligatoria: true },
  { nombre: 'Docker', requerido: 'Intermedio', tienes: 'Básico', cubre: 0.5, obligatoria: true },
  { nombre: 'Git', requerido: 'Básico', tienes: 'Intermedio', cubre: 1, obligatoria: false },
]

function TarjetaEjemplo() {
  return (
    <figure className={estilos.tarjeta} aria-labelledby="ejemplo-titulo">
      <div className={estilos.tarjetaEncabezado}>
        <div>
          <p className={estilos.etiquetaEjemplo}>Ejemplo</p>
          <p id="ejemplo-titulo" className={estilos.tarjetaTitulo}>
            Desarrollador/a Java junior
          </p>
          <p className={estilos.tarjetaMeta}>Empresa de software · Medellín · Híbrida</p>
        </div>
        <div className={estilos.porcentaje} aria-label="Compatibilidad del 86 %">
          <span>86</span>%
        </div>
      </div>

      <ul className={estilos.habilidades}>
        {HABILIDADES_EJEMPLO.map((habilidad) => (
          <li key={habilidad.nombre}>
            <div className={estilos.habilidadFila}>
              <span>
                {habilidad.nombre}
                {!habilidad.obligatoria && <span className={estilos.deseable}> · deseable</span>}
              </span>
              <span className={estilos.habilidadNivel}>{habilidad.tienes}</span>
            </div>
            <div className={estilos.medidor} aria-hidden="true">
              <div
                className={habilidad.cubre < 1 ? estilos.medidorIncompleto : estilos.medidorCompleto}
                style={{ width: `${habilidad.cubre * 100}%` }}
              />
            </div>
          </li>
        ))}
      </ul>

      <figcaption className={estilos.falta}>
        <strong>Lo que te falta:</strong> Docker. La vacante pide nivel intermedio y tienes básico.
      </figcaption>
    </figure>
  )
}

export default function Landing() {
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
            <p className={estilos.antetitulo}>Medellín · Prácticas y empleos junior en software</p>
            <h1 id="hero-titulo" className={estilos.heroTitulo}>
              Tu primer empleo en tecnología, sin adivinar qué te falta
            </h1>
            <p className={estilos.heroDescripcion}>
              Conectamos a estudiantes de últimos semestres y recién graduados con vacantes de práctica y nivel junior.
              Para cada una te mostramos qué tan compatible eres y qué habilidades te faltan para encajar.
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
          <TarjetaEjemplo />
        </section>

        <section className={estilos.seccion} aria-labelledby="problema-titulo">
          <h2 id="problema-titulo">La paradoja del junior</h2>
          <p className={estilos.seccionDescripcion}>
            Las vacantes “junior” piden de 6 meses a 2 años de experiencia, mientras el sector TI reporta falta de talento.
            El problema no es solo encontrar desarrolladores: es conectar al recién graduado con su primer empleo.
          </p>
          <ul className={estilos.cifras}>
            {CIFRAS.map((cifra) => (
              <li key={cifra.valor} className={estilos.cifra}>
                <p className={estilos.cifraValor}>{cifra.valor}</p>
                <p className={estilos.cifraTexto}>{cifra.texto}</p>
                <a className={estilos.cifraFuente} href={cifra.url} target="_blank" rel="noopener noreferrer">
                  {cifra.fuente}
                  <span className={estilos.oculto}> (abre en una pestaña nueva)</span>
                </a>
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
        </section>

        <section className={estilos.diferencial} aria-labelledby="diferencial-titulo">
          <h2 id="diferencial-titulo">
            Otras plataformas te dicen qué vacantes encajan contigo. Nosotros te decimos <em>qué te falta para encajar</em>.
          </h2>
          <p>
            Y solo verás vacantes alcanzables para un perfil junior: prácticas y empleos de nivel inicial publicados por
            empresas verificadas.
          </p>
        </section>

        <section id="empresas" className={`${estilos.seccion} ${estilos.empresas}`} aria-labelledby="empresas-titulo">
          <div>
            <h2 id="empresas-titulo">Para empresas que contratan talento junior</h2>
            <p className={estilos.seccionDescripcion}>
              Encuentra practicantes y desarrolladores junior en Medellín que ya conocen las habilidades que necesitas.
            </p>
            <Link to="/registro?tipo=empresa" className="boton">
              Registrar mi empresa
            </Link>
          </div>
          <ul className={estilos.beneficios}>
            {BENEFICIOS_EMPRESA.map((beneficio) => (
              <li key={beneficio}>{beneficio}</li>
            ))}
          </ul>
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
        <p>Proyecto académico de Ingeniería Informática · Medellín, 2026</p>
        <p>
          <Link to="/login">Iniciar sesión</Link> · <Link to="/registro">Crear cuenta</Link>
        </p>
      </footer>
    </div>
  )
}
