// Datos de demostración para la sustentación (ficticios: ninguna empresa ni persona es real).
// Los correos usan el dominio reservado .example, así no llegan a ningún buzón ni se confunden con cuentas reales.
// Las habilidades deben existir en el catálogo inicial (V4__catalogo_habilidades.sql); `--revisar` lo comprueba.

/** `verificar: false` deja una empresa pendiente para mostrar la verificación del administrador en vivo. */
export const EMPRESAS = [
  {
    clave: 'andina',
    email: 'talento@andinacodigo.example',
    razonSocial: 'Andina Código S.A.S.',
    nit: '9019876541',
    sector: 'Desarrollo de software',
    ciudad: 'Medellín',
    sitioWeb: 'https://andinacodigo.example',
    verificar: true,
  },
  {
    clave: 'nube',
    email: 'personas@nubepaisa.example',
    razonSocial: 'Nube Paisa Tecnología S.A.S.',
    nit: '9012345674',
    sector: 'Servicios en la nube',
    ciudad: 'Envigado',
    sitioWeb: 'https://nubepaisa.example',
    verificar: true,
  },
  {
    clave: 'analitica',
    email: 'equipo@analiticaaburra.example',
    razonSocial: 'Analítica del Aburrá S.A.S.',
    nit: '9015557772',
    sector: 'Analítica de datos',
    ciudad: 'Medellín',
    sitioWeb: 'https://analiticaaburra.example',
    verificar: true,
  },
  {
    clave: 'cafetal',
    email: 'hola@cafetallabs.example',
    razonSocial: 'Cafetal Labs S.A.S.',
    nit: '9018884440',
    sector: 'Producto digital',
    ciudad: 'Rionegro',
    sitioWeb: 'https://cafetallabs.example',
    verificar: false,
  },
]

/**
 * `diasCierre`: días desde hoy (hora de Colombia) hasta la fecha de cierre.
 * `cerrar: true` la cierra después de cargar las postulaciones (para mostrar una vacante cerrada con contratado).
 * Habilidades: [nombre, nivelRequerido, obligatoria].
 */
export const VACANTES = [
  {
    clave: 'backend-java',
    empresa: 'andina',
    titulo: 'Desarrollador backend Java junior',
    descripcion:
      'Te unirás al equipo que construye las APIs de pagos para comercios de Medellín. Trabajarás con Spring Boot, ' +
      'PostgreSQL y Docker, con revisión de código diaria y un mentor asignado durante los primeros tres meses.',
    tipo: 'JUNIOR',
    modalidad: 'HIBRIDA',
    ciudad: 'Medellín',
    salarioMin: 3000000,
    salarioMax: 3800000,
    diasCierre: 30,
    habilidades: [
      ['Java', 'INTERMEDIO', true],
      ['Spring Boot', 'BASICO', true],
      ['PostgreSQL', 'BASICO', true],
      ['Git', 'BASICO', false],
      ['Docker', 'BASICO', false],
    ],
  },
  {
    clave: 'practica-web',
    empresa: 'andina',
    titulo: 'Practicante de desarrollo web',
    descripcion:
      'Práctica de seis meses en el equipo de producto. Apoyarás la construcción de pantallas en React para el portal ' +
      'de clientes y aprenderás buenas prácticas de pruebas y accesibilidad.',
    tipo: 'PRACTICA',
    modalidad: 'PRESENCIAL',
    ciudad: 'Medellín',
    salarioMin: 1423500,
    salarioMax: null,
    diasCierre: 40,
    habilidades: [
      ['JavaScript', 'BASICO', true],
      ['React', 'BASICO', true],
      ['Git', 'BASICO', false],
      ['Trabajo en equipo', 'INTERMEDIO', false],
    ],
  },
  {
    clave: 'frontend',
    empresa: 'andina',
    titulo: 'Desarrollador frontend junior',
    descripcion:
      'Desarrollo de interfaces con React y TypeScript para aplicaciones internas. Proceso ya cerrado: se usa en la ' +
      'demostración para mostrar una vacante cerrada con una persona contratada.',
    tipo: 'JUNIOR',
    modalidad: 'REMOTA',
    ciudad: 'Medellín',
    salarioMin: 3200000,
    salarioMax: 3600000,
    diasCierre: 25,
    cerrar: true,
    habilidades: [
      ['React', 'INTERMEDIO', true],
      ['TypeScript', 'BASICO', true],
      ['Git', 'BASICO', false],
    ],
  },
  {
    clave: 'practica-nube',
    empresa: 'nube',
    titulo: 'Practicante de infraestructura en la nube',
    descripcion:
      'Acompañarás al equipo de plataforma en la automatización de despliegues con Docker y AWS. Buscamos curiosidad ' +
      'por Linux y ganas de aprender sobre redes y seguridad.',
    tipo: 'PRACTICA',
    modalidad: 'HIBRIDA',
    ciudad: 'Envigado',
    salarioMin: 1600000,
    salarioMax: null,
    diasCierre: 35,
    habilidades: [
      ['Linux', 'BASICO', true],
      ['Docker', 'BASICO', true],
      ['AWS', 'BASICO', false],
      ['Inglés', 'INTERMEDIO', false],
    ],
  },
  {
    clave: 'node',
    empresa: 'nube',
    titulo: 'Desarrollador Node.js junior',
    descripcion:
      'Construirás servicios en Node.js y Express que procesan eventos de miles de dispositivos. Trabajo remoto con ' +
      'reuniones de equipo dos veces por semana.',
    tipo: 'JUNIOR',
    modalidad: 'REMOTA',
    ciudad: 'Medellín',
    salarioMin: 3200000,
    salarioMax: 4000000,
    diasCierre: 28,
    habilidades: [
      ['JavaScript', 'INTERMEDIO', true],
      ['Node.js', 'BASICO', true],
      ['Express', 'BASICO', false],
      ['MongoDB', 'BASICO', false],
      ['Git', 'BASICO', false],
    ],
  },
  {
    clave: 'analista',
    empresa: 'analitica',
    titulo: 'Analista de datos junior',
    descripcion:
      'Prepararás tableros e informes para empresas del Valle de Aburrá a partir de datos de ventas y logística. ' +
      'Usamos SQL todos los días y Python para automatizar.',
    tipo: 'JUNIOR',
    modalidad: 'HIBRIDA',
    ciudad: 'Medellín',
    salarioMin: 2800000,
    salarioMax: 3400000,
    diasCierre: 32,
    habilidades: [
      ['SQL', 'INTERMEDIO', true],
      ['Python', 'BASICO', true],
      ['Comunicación', 'INTERMEDIO', false],
    ],
  },
  {
    clave: 'practica-datos',
    empresa: 'analitica',
    titulo: 'Practicante de ingeniería de datos',
    descripcion:
      'Apoyarás la construcción de flujos de datos con Python y PostgreSQL. Ideal para estudiantes de últimos ' +
      'semestres que quieran enfocarse en datos.',
    tipo: 'PRACTICA',
    modalidad: 'PRESENCIAL',
    ciudad: 'Medellín',
    salarioMin: 1500000,
    salarioMax: null,
    diasCierre: 45,
    habilidades: [
      ['Python', 'BASICO', true],
      ['SQL', 'BASICO', true],
      ['PostgreSQL', 'BASICO', false],
      ['Git', 'BASICO', false],
    ],
  },
]

/** Habilidades: [nombre, nivel]. Laura es la candidata para iniciar sesión en la sustentación. */
export const CANDIDATOS = [
  {
    clave: 'laura',
    email: 'laura.gomez@demo.example',
    nombres: 'Laura',
    apellidos: 'Gómez Restrepo',
    estadoAcademico: 'ESTUDIANTE',
    semestre: 9,
    universidad: 'Universidad de Antioquia',
    programa: 'Ingeniería de Sistemas',
    ciudad: 'Medellín',
    resumen: 'Me interesa el desarrollo backend con Java y las bases de datos. Hice mi proyecto de grado sobre una API de reservas.',
    habilidades: [
      ['Java', 'INTERMEDIO'],
      ['Spring Boot', 'BASICO'],
      ['SQL', 'INTERMEDIO'],
      ['Git', 'INTERMEDIO'],
      ['Trabajo en equipo', 'AVANZADO'],
    ],
  },
  {
    clave: 'andres',
    email: 'andres.restrepo@demo.example',
    nombres: 'Andrés',
    apellidos: 'Restrepo Vélez',
    estadoAcademico: 'EGRESADO',
    semestre: null,
    universidad: 'Universidad Nacional de Colombia',
    programa: 'Ingeniería de Sistemas e Informática',
    ciudad: 'Medellín',
    resumen: 'Desarrollador frontend. Me gusta construir interfaces accesibles con React y TypeScript.',
    habilidades: [
      ['JavaScript', 'INTERMEDIO'],
      ['TypeScript', 'BASICO'],
      ['React', 'INTERMEDIO'],
      ['Node.js', 'BASICO'],
      ['Git', 'INTERMEDIO'],
    ],
  },
  {
    clave: 'camila',
    email: 'camila.ortiz@demo.example',
    nombres: 'Camila',
    apellidos: 'Ortiz Londoño',
    estadoAcademico: 'ESTUDIANTE',
    semestre: 10,
    universidad: 'Universidad EAFIT',
    programa: 'Ingeniería Matemática',
    ciudad: 'Envigado',
    resumen: 'Busco mi primera práctica en análisis de datos. Manejo Python y SQL y he trabajado con tableros.',
    habilidades: [
      ['Python', 'INTERMEDIO'],
      ['SQL', 'INTERMEDIO'],
      ['PostgreSQL', 'BASICO'],
      ['Inglés', 'AVANZADO'],
      ['Comunicación', 'AVANZADO'],
    ],
  },
  {
    clave: 'santiago',
    email: 'santiago.mejia@demo.example',
    nombres: 'Santiago',
    apellidos: 'Mejía Arango',
    estadoAcademico: 'ESTUDIANTE',
    semestre: 8,
    universidad: 'Politécnico Colombiano Jaime Isaza Cadavid',
    programa: 'Tecnología en Sistemas',
    ciudad: 'Bello',
    resumen: 'Me apasiona la infraestructura: administro servidores Linux en mi casa y estoy aprendiendo AWS.',
    habilidades: [
      ['Linux', 'INTERMEDIO'],
      ['Docker', 'BASICO'],
      ['AWS', 'BASICO'],
      ['Git', 'BASICO'],
    ],
  },
  {
    clave: 'valeria',
    email: 'valeria.cano@demo.example',
    nombres: 'Valeria',
    apellidos: 'Cano Gil',
    estadoAcademico: 'EGRESADO',
    semestre: null,
    universidad: 'Universidad de Medellín',
    programa: 'Ingeniería de Software',
    ciudad: 'Medellín',
    resumen: 'Recién egresada, con experiencia en proyectos académicos con Java y metodologías ágiles.',
    habilidades: [
      ['Java', 'BASICO'],
      ['SQL', 'BASICO'],
      ['Git', 'BASICO'],
      ['Scrum', 'BASICO'],
    ],
  },
  {
    clave: 'mateo',
    email: 'mateo.zapata@demo.example',
    nombres: 'Mateo',
    apellidos: 'Zapata Ríos',
    estadoAcademico: 'ESTUDIANTE',
    semestre: 9,
    universidad: 'Instituto Tecnológico Metropolitano',
    programa: 'Ingeniería de Sistemas',
    ciudad: 'Itagüí',
    resumen: 'Estudiante de último año, interesado en desarrollo web. Aprendo rápido y me gusta trabajar en equipo.',
    habilidades: [
      ['JavaScript', 'BASICO'],
      ['React', 'BASICO'],
      ['Git', 'BASICO'],
      ['Trabajo en equipo', 'INTERMEDIO'],
    ],
  },
]

/**
 * Postulaciones y el estado al que la empresa las lleva (regla 5, paso a paso).
 * Laura queda sin postularse a "Analista de datos junior" y "Practicante de ingeniería de datos" para postularse en vivo.
 */
export const POSTULACIONES = [
  { candidato: 'laura', vacante: 'backend-java', estado: 'ENTREVISTA' },
  { candidato: 'valeria', vacante: 'backend-java', estado: 'EN_REVISION' },
  { candidato: 'andres', vacante: 'frontend', estado: 'CONTRATADO' },
  { candidato: 'mateo', vacante: 'frontend', estado: 'RECHAZADA' },
  { candidato: 'mateo', vacante: 'practica-web', estado: 'EN_REVISION' },
  { candidato: 'andres', vacante: 'node', estado: 'ENVIADA' },
  { candidato: 'santiago', vacante: 'practica-nube', estado: 'EN_REVISION' },
  { candidato: 'camila', vacante: 'analista', estado: 'ENTREVISTA' },
  { candidato: 'camila', vacante: 'practica-datos', estado: 'ENVIADA' },
]
