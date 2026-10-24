import { z } from 'zod'

// Validaciones de docs/contrato-api.md (2.3 y 2.4): copian el Bean Validation y las restricciones del esquema.

const correo = z
  .string()
  .trim()
  .pipe(z.email('Escribe un correo válido').max(255, 'Máximo 255 caracteres'))

const contrasena = z
  .string()
  .min(8, 'Debe tener al menos 8 caracteres')
  .max(72, 'Máximo 72 caracteres')

const textoObligatorio = (mensaje: string, maximo: number) =>
  z.string().trim().min(1, mensaje).max(maximo, `Máximo ${maximo} caracteres`)

const textoOpcional = (maximo: number) => z.string().trim().max(maximo, `Máximo ${maximo} caracteres`)

/** Quita puntos, guiones y espacios: el backend guarda el NIT solo con dígitos. */
export const limpiarNit = (nit: string) => nit.replace(/[.\-\s]/g, '')

/** El contrato no acepta cadenas vacías en campos opcionales: se envían como null. */
export const vacioANulo = (texto: string): string | null => (texto.trim() === '' ? null : texto.trim())

export const SEMESTRES = Array.from({ length: 12 }, (_, i) => i + 1)

export const esquemaCandidato = z
  .object({
    email: correo,
    password: contrasena,
    confirmarPassword: z.string(),
    nombres: textoObligatorio('Escribe tus nombres', 100),
    apellidos: textoObligatorio('Escribe tus apellidos', 100),
    estadoAcademico: z.enum(['ESTUDIANTE', 'EGRESADO'], 'Elige si eres estudiante o egresado'),
    semestre: z.string(),
    universidad: textoOpcional(150),
    programa: textoOpcional(150),
    ciudad: textoOpcional(100),
  })
  .superRefine((datos, ctx) => {
    if (datos.password !== datos.confirmarPassword) {
      ctx.addIssue({ code: 'custom', path: ['confirmarPassword'], message: 'Las contraseñas no coinciden' })
    }
    // Restricción ck_candidatos_semestre_estado: obligatorio para estudiantes
    if (datos.estadoAcademico === 'ESTUDIANTE' && datos.semestre === '') {
      ctx.addIssue({ code: 'custom', path: ['semestre'], message: 'Elige el semestre que cursas' })
    }
  })

export type DatosCandidato = z.infer<typeof esquemaCandidato>

export const esquemaEmpresa = z
  .object({
    email: correo,
    password: contrasena,
    confirmarPassword: z.string(),
    razonSocial: textoObligatorio('Escribe la razón social', 200),
    nit: z
      .string()
      .trim()
      .min(1, 'Escribe el NIT')
      .refine((nit) => /^[0-9]{6,15}$/.test(limpiarNit(nit)), 'El NIT debe tener entre 6 y 15 dígitos'),
    sector: textoOpcional(100),
    ciudad: textoOpcional(100),
    sitioWeb: textoOpcional(255).refine(
      (url) => url === '' || /^https?:\/\/[^\s.]+\.[^\s]+$/.test(url),
      'Escribe una dirección que empiece por http:// o https://',
    ),
  })
  .superRefine((datos, ctx) => {
    if (datos.password !== datos.confirmarPassword) {
      ctx.addIssue({ code: 'custom', path: ['confirmarPassword'], message: 'Las contraseñas no coinciden' })
    }
  })

export type DatosEmpresa = z.infer<typeof esquemaEmpresa>
