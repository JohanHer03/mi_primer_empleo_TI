#!/usr/bin/env node
// Carga los datos de demostración (datos.mjs) usando la API, como lo haría una persona desde la aplicación.
// Uso y variables de entorno: ver README.md de esta carpeta. No guarda ni imprime contraseñas ni tokens.
//
//   node scripts/datos-demo/cargar.mjs --revisar   comprueba los datos sin conectarse a nada
//   node scripts/datos-demo/cargar.mjs             los carga en la API de DEMO_API
//
// Se puede volver a correr: las cuentas que ya existen inician sesión, las vacantes se buscan por título,
// una postulación repetida (409) se ignora y los estados solo avanzan.

import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { CANDIDATOS, EMPRESAS, POSTULACIONES, VACANTES } from './datos.mjs'

const AQUI = dirname(fileURLToPath(import.meta.url))
const CATALOGO_V4 = resolve(AQUI, '../../backend/src/main/resources/db/migration/V4__catalogo_habilidades.sql')

/** Regla 5 (contrato 7.2): a qué estado avanza cada uno camino al final pedido. */
const ORDEN_ESTADOS = ['ENVIADA', 'EN_REVISION', 'ENTREVISTA', 'CONTRATADO']
const NIVELES = ['BASICO', 'INTERMEDIO', 'AVANZADO']

// ---------------------------------------------------------------- revisión sin conexión

function revisar() {
  const errores = []
  const catalogo = new Set([...readFileSync(CATALOGO_V4, 'utf8').matchAll(/\('([^']+)',\s*'[A-Z_]+'\)/g)].map((m) => m[1]))
  const empresas = new Set(EMPRESAS.map((e) => e.clave))
  const vacantes = new Map(VACANTES.map((v) => [v.clave, v]))
  const candidatos = new Set(CANDIDATOS.map((c) => c.clave))
  const unicos = (lista, campo) => new Set(lista.map((x) => x[campo])).size === lista.length

  if (!unicos(EMPRESAS, 'email') || !unicos(CANDIDATOS, 'email')) errores.push('Hay correos repetidos')
  if (!unicos(EMPRESAS, 'nit')) errores.push('Hay NIT repetidos')
  if (!unicos(VACANTES, 'titulo')) errores.push('Hay títulos de vacante repetidos (el script busca por título)')

  for (const e of EMPRESAS) {
    if (!/^[0-9]{6,15}$/.test(e.nit)) errores.push(`${e.razonSocial}: el NIT debe tener entre 6 y 15 dígitos`)
    if (e.razonSocial.length > 200) errores.push(`${e.razonSocial}: razón social de más de 200 caracteres`)
  }
  for (const v of VACANTES) {
    const donde = `Vacante "${v.titulo}"`
    if (!empresas.has(v.empresa)) errores.push(`${donde}: empresa ${v.empresa} no existe`)
    if (EMPRESAS.find((e) => e.clave === v.empresa)?.verificar === false) {
      errores.push(`${donde}: su empresa no se verifica y no podría publicar (regla 3)`)
    }
    if (v.titulo.length > 150 || v.descripcion.length > 5000) errores.push(`${donde}: título o descripción demasiado largos`)
    if (v.salarioMin !== null && v.salarioMax !== null && v.salarioMin > v.salarioMax) errores.push(`${donde}: salario mínimo mayor que el máximo`)
    if (!(v.diasCierre >= 0)) errores.push(`${donde}: diasCierre debe ser 0 o más`)
    if (v.habilidades.length < 1 || v.habilidades.length > 15) errores.push(`${donde}: debe pedir de 1 a 15 habilidades`)
    if (!v.habilidades.some(([, , obligatoria]) => obligatoria)) errores.push(`${donde}: al menos una habilidad obligatoria`)
    revisarHabilidades(donde, v.habilidades, catalogo, errores)
  }
  for (const c of CANDIDATOS) {
    const donde = `Candidato ${c.nombres} ${c.apellidos}`
    if ((c.estadoAcademico === 'ESTUDIANTE') !== (c.semestre !== null)) errores.push(`${donde}: semestre solo para estudiantes`)
    if (c.semestre !== null && (c.semestre < 1 || c.semestre > 12)) errores.push(`${donde}: semestre entre 1 y 12`)
    if ((c.resumen ?? '').length > 1000) errores.push(`${donde}: resumen de más de 1000 caracteres`)
    if (c.habilidades.length > 30) errores.push(`${donde}: máximo 30 habilidades`)
    revisarHabilidades(donde, c.habilidades, catalogo, errores)
  }
  const pares = new Set()
  for (const p of POSTULACIONES) {
    const donde = `Postulación ${p.candidato} → ${p.vacante}`
    if (!candidatos.has(p.candidato)) errores.push(`${donde}: candidato desconocido`)
    if (!vacantes.has(p.vacante)) errores.push(`${donde}: vacante desconocida`)
    if (![...ORDEN_ESTADOS, 'RECHAZADA'].includes(p.estado)) errores.push(`${donde}: estado ${p.estado} desconocido`)
    if (pares.has(`${p.candidato}|${p.vacante}`)) errores.push(`${donde}: repetida (regla 1)`)
    pares.add(`${p.candidato}|${p.vacante}`)
  }
  return errores
}

function revisarHabilidades(donde, habilidades, catalogo, errores) {
  const nombres = habilidades.map(([nombre]) => nombre)
  if (new Set(nombres).size !== nombres.length) errores.push(`${donde}: habilidad repetida`)
  for (const [nombre, nivel] of habilidades) {
    if (!catalogo.has(nombre)) errores.push(`${donde}: "${nombre}" no está en el catálogo inicial (V4)`)
    if (!NIVELES.includes(nivel)) errores.push(`${donde}: nivel ${nivel} desconocido`)
  }
}

// ---------------------------------------------------------------- carga por la API

const API = `${(process.env.DEMO_API ?? 'http://localhost:8080').replace(/\/+$/, '')}/api`

class ErrorCarga extends Error {}

async function llamar(metodo, ruta, { cuerpo, token } = {}) {
  const encabezados = { Accept: 'application/json' }
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json'
  if (token) encabezados.Authorization = `Bearer ${token}`
  // El primer pedido puede tardar: el contenedor de Vercel arranca en frío (~10 s)
  const respuesta = await fetch(API + ruta, {
    method: metodo,
    headers: encabezados,
    body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    signal: AbortSignal.timeout(60_000),
  })
  const texto = await respuesta.text()
  let datos = null
  try {
    datos = texto ? JSON.parse(texto) : null
  } catch {
    datos = { mensaje: texto.slice(0, 200) }
  }
  return { status: respuesta.status, datos }
}

/** Como `llamar`, pero cualquier código distinto de los esperados detiene la carga con el mensaje del backend. */
async function exigir(esperados, metodo, ruta, opciones) {
  const { status, datos } = await llamar(metodo, ruta, opciones)
  if (!esperados.includes(status)) {
    throw new ErrorCarga(`${metodo} ${ruta} respondió ${status}: ${datos?.mensaje ?? 'sin mensaje'}`)
  }
  return { status, datos }
}

/** Registra la cuenta; si el correo ya existe (409), inicia sesión con la misma contraseña. */
async function cuenta(tipo, registro, clave) {
  const { status, datos } = await exigir([201, 409], 'POST', `/auth/registro/${tipo}`, {
    cuerpo: { ...registro, password: clave },
  })
  if (status === 201) return { token: datos.token, nueva: true }
  const login = await llamar('POST', '/auth/login', { cuerpo: { email: registro.email, password: clave } })
  if (login.status !== 200) {
    throw new ErrorCarga(`${registro.email} ya existe pero no se pudo iniciar sesión (${login.status}): ¿otra DEMO_PASSWORD?`)
  }
  return { token: login.datos.token, nueva: false }
}

function enDias(dias) {
  // Fecha de hoy en Colombia (como valida el backend) más `dias`
  const hoy = new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Bogota' }).format(new Date())
  const fecha = new Date(`${hoy}T12:00:00Z`)
  fecha.setUTCDate(fecha.getUTCDate() + dias)
  return fecha.toISOString().slice(0, 10)
}

function variable(nombre) {
  const valor = process.env[nombre]
  if (!valor) throw new ErrorCarga(`Falta la variable de entorno ${nombre} (ver scripts/datos-demo/README.md)`)
  return valor
}

async function esperarApi() {
  for (let intento = 1; intento <= 6; intento++) {
    try {
      const { status } = await llamar('GET', '/habilidades')
      if (status === 401) return
    } catch {
      // El contenedor todavía arranca
    }
    console.log(`  La API no responde todavía (intento ${intento} de 6)…`)
    await new Promise((resolver) => setTimeout(resolver, 5000))
  }
  throw new ErrorCarga(`No se pudo conectar con ${API}`)
}

async function cargar() {
  const clave = variable('DEMO_PASSWORD')
  if (clave.length < 8) throw new ErrorCarga('DEMO_PASSWORD debe tener al menos 8 caracteres')
  const adminEmail = variable('ADMIN_EMAIL')
  const adminClave = variable('ADMIN_PASSWORD')

  console.log(`Cargando datos de demostración en ${API}`)
  await esperarApi()

  const admin = await llamar('POST', '/auth/login', { cuerpo: { email: adminEmail, password: adminClave } })
  if (admin.status !== 200) throw new ErrorCarga(`No se pudo iniciar sesión como administrador (${admin.status})`)
  const tokenAdmin = admin.datos.token
  const { datos: catalogo } = await exigir([200], 'GET', '/habilidades', { token: tokenAdmin })
  const idHabilidad = new Map(catalogo.map((h) => [h.nombre, h.id]))
  const habilidadId = (nombre) => {
    const id = idHabilidad.get(nombre)
    if (!id) throw new ErrorCarga(`La habilidad "${nombre}" no está en el catálogo de esta API`)
    return id
  }

  // Empresas: registro (o inicio de sesión) y verificación (regla 3)
  const empresas = new Map()
  for (const e of EMPRESAS) {
    const { clave: _clave, verificar, ...registro } = e
    const { token, nueva } = await cuenta('empresa', registro, clave)
    let { datos: perfil } = await exigir([200], 'GET', '/empresas/me', { token })
    if (verificar && !perfil.verificada) {
      await exigir([200], 'PATCH', `/empresas/${perfil.id}/verificar`, { token: tokenAdmin })
      perfil = { ...perfil, verificada: true }
    }
    empresas.set(e.clave, { token, perfil })
    console.log(`  Empresa ${e.razonSocial}: ${nueva ? 'creada' : 'ya existía'}${perfil.verificada ? ', verificada' : ', sin verificar'}`)
  }

  // Vacantes: se reutilizan por título; las nuevas se publican abiertas (se cierran al final)
  const vacantes = new Map()
  for (const v of VACANTES) {
    const { token } = empresas.get(v.empresa)
    const { datos: propias } = await exigir([200], 'GET', '/empresas/me/vacantes?size=50', { token })
    let vacante = propias.contenido.find((x) => x.titulo === v.titulo)
    if (!vacante) {
      const { datos } = await exigir([201], 'POST', '/vacantes', {
        token,
        cuerpo: {
          titulo: v.titulo,
          descripcion: v.descripcion,
          tipo: v.tipo,
          modalidad: v.modalidad,
          ciudad: v.ciudad,
          salarioMin: v.salarioMin,
          salarioMax: v.salarioMax,
          fechaCierre: enDias(v.diasCierre),
          habilidades: v.habilidades.map(([nombre, nivelRequerido, obligatoria]) => ({
            habilidadId: habilidadId(nombre),
            nivelRequerido,
            obligatoria,
          })),
        },
      })
      vacante = datos
      console.log(`  Vacante "${v.titulo}": publicada`)
    } else {
      console.log(`  Vacante "${v.titulo}": ya existía`)
    }
    vacantes.set(v.clave, { id: vacante.id, estado: vacante.estado, empresa: v.empresa, cerrar: v.cerrar === true })
  }

  // Candidatos: registro (o inicio de sesión), resumen y habilidades
  const candidatos = new Map()
  for (const c of CANDIDATOS) {
    const registro = {
      email: c.email,
      nombres: c.nombres,
      apellidos: c.apellidos,
      estadoAcademico: c.estadoAcademico,
      semestre: c.semestre,
      universidad: c.universidad,
      programa: c.programa,
      ciudad: c.ciudad,
    }
    const { token, nueva } = await cuenta('candidato', registro, clave)
    const { email: _email, ...perfil } = registro
    await exigir([200], 'PUT', '/candidatos/me', { token, cuerpo: { ...perfil, githubUrl: null, resumen: c.resumen } })
    await exigir([200], 'PUT', '/candidatos/me/habilidades', {
      token,
      cuerpo: c.habilidades.map(([nombre, nivel]) => ({ habilidadId: habilidadId(nombre), nivel })),
    })
    candidatos.set(c.clave, { token, email: c.email })
    console.log(`  Candidato ${c.nombres} ${c.apellidos}: ${nueva ? 'creado' : 'ya existía'}, con ${c.habilidades.length} habilidades`)
  }

  // Postulaciones (antes de cerrar vacantes, regla 2) y estados que la empresa les da (regla 5)
  for (const p of POSTULACIONES) {
    const candidato = candidatos.get(p.candidato)
    const vacante = vacantes.get(p.vacante)
    const { status, datos } = await llamar('POST', `/vacantes/${vacante.id}/postulaciones`, { token: candidato.token })
    if (status === 409 && vacante.estado === 'CERRADA') {
      // Corrida anterior: la vacante ya se cerró después de cargar sus postulaciones
    } else if (status !== 201 && status !== 409) {
      throw new ErrorCarga(`Postular a ${candidato.email} respondió ${status}: ${datos?.mensaje ?? ''}`)
    }
    const { token } = empresas.get(vacante.empresa)
    const { datos: postulantes } = await exigir([200], 'GET', `/vacantes/${vacante.id}/postulaciones?size=50`, { token })
    const postulante = postulantes.contenido.find((x) => x.candidato.email === candidato.email)
    if (!postulante) throw new ErrorCarga(`No aparece la postulación de ${candidato.email} en la vacante`)
    const camino = p.estado === 'RECHAZADA' ? ['RECHAZADA'] : ORDEN_ESTADOS.slice(ORDEN_ESTADOS.indexOf(postulante.estado) + 1, ORDEN_ESTADOS.indexOf(p.estado) + 1)
    let actual = postulante.estado
    for (const siguiente of actual === p.estado || ['CONTRATADO', 'RECHAZADA'].includes(actual) ? [] : camino) {
      await exigir([200], 'PATCH', `/postulaciones/${postulante.id}/estado`, { token, cuerpo: { estado: siguiente } })
      actual = siguiente
    }
    console.log(`  Postulación ${p.candidato} → ${p.vacante}: ${actual}`)
  }

  // Cierre de las vacantes marcadas (después de sus postulaciones)
  for (const [claveVacante, vacante] of vacantes) {
    if (vacante.cerrar && vacante.estado !== 'CERRADA') {
      await exigir([200], 'PATCH', `/vacantes/${vacante.id}/cerrar`, { token: empresas.get(vacante.empresa).token })
      console.log(`  Vacante ${claveVacante}: cerrada`)
    }
  }

  console.log('\nListo. Cuentas para la demostración (contraseña: la de DEMO_PASSWORD):')
  console.log(`  Candidata: ${CANDIDATOS[0].email}`)
  console.log(`  Empresa verificada: ${EMPRESAS[0].email}`)
  console.log(`  Empresa por verificar: ${EMPRESAS.find((e) => !e.verificar).email}`)
  console.log('  Administrador: el de ADMIN_EMAIL')
}

// ---------------------------------------------------------------- inicio

try {
  const errores = revisar()
  if (errores.length) {
    console.error('Los datos de demostración tienen errores:')
    for (const error of errores) console.error(`  - ${error}`)
    process.exit(1)
  }
  if (process.argv.includes('--revisar')) {
    console.log(`Datos correctos: ${EMPRESAS.length} empresas, ${VACANTES.length} vacantes, ${CANDIDATOS.length} candidatos y ${POSTULACIONES.length} postulaciones.`)
  } else {
    await cargar()
  }
} catch (error) {
  console.error(error instanceof ErrorCarga ? `Error: ${error.message}` : error)
  process.exit(1)
}
