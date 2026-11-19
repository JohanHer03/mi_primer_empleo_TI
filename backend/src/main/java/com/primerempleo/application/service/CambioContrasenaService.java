package com.primerempleo.application.service;

import com.primerempleo.application.port.in.CambiarContrasenaUseCase;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.PoliticaContrasena;
import com.primerempleo.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cambio de contraseña del usuario del token (docs/contrato-api.md, 8.1).
 * Orden: primero la contraseña actual, luego que la cuenta esté activa (403), la política de la nueva y por último
 * que sea distinta; así quien no conoce la actual no aprende nada más. Los errores de datos son 400, no 401:
 * la sesión sigue siendo válida y el frontend cierra la sesión ante un 401.
 */
@Service
public class CambioContrasenaService implements CambiarContrasenaUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final CifradorContrasenaPort cifrador;

    public CambioContrasenaService(UsuarioRepositoryPort usuarios, CifradorContrasenaPort cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    @Override
    @Transactional
    public void cambiarContrasena(CambiarContrasenaComando comando) {
        Usuario usuario = usuarios.buscarPorId(comando.usuarioId())
                .orElseThrow(RecursoNoEncontradoException::usuario);
        if (!cifrador.coincide(comando.contrasenaActual(), usuario.passwordHash())) {
            throw new DatosInvalidosException("contrasenaActual: no es correcta");
        }
        // Como en el login: el 403 solo se revela a quien conoce la contraseña
        if (!usuario.activo()) {
            throw new CuentaDesactivadaException();
        }
        PoliticaContrasena.validar(comando.contrasenaNueva(), "contrasenaNueva");
        if (comando.contrasenaNueva().equals(comando.contrasenaActual())) {
            throw new DatosInvalidosException("contrasenaNueva: debe ser distinta de la actual");
        }
        usuarios.actualizarContrasena(usuario.conContrasena(cifrador.cifrar(comando.contrasenaNueva())));
    }
}
