import estilos from './AvisoVerificacion.module.css'

/** Regla 3: hasta que un administrador verifique la empresa, no puede publicar vacantes. */
export function AvisoVerificacion() {
  return (
    <div className={estilos.aviso} role="status">
      <p className={estilos.titulo}>Tu empresa está pendiente de verificación</p>
      <p className={estilos.texto}>
        Un administrador revisará tus datos antes de que puedas publicar vacantes. Mientras tanto puedes completar tu
        perfil.
      </p>
    </div>
  )
}
