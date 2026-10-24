/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Origen del backend sin /api: vacío en Vercel, http://localhost:8080 en local. */
  readonly VITE_API_URL?: string
  /** "true" para usar las respuestas simuladas de src/api/mocks mientras el backend no tenga el endpoint. */
  readonly VITE_USAR_MOCKS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
