package com.primerempleo.application.service;

import com.primerempleo.application.port.in.IniciarSesionUseCase;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.GeneradorTokenPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.CredencialesInvalidasException;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.model.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicio de sesión (docs/contrato-api.md, 2.5). Correo inexistente y contraseña errónea dan el mismo 401;
 * el 403 de cuenta desactivada solo se revela a quien conoce la contraseña.
 */
@Service
public class InicioSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final CifradorContrasenaPort cifrador;
    private final GeneradorTokenPort generadorToken;

    public InicioSesionService(UsuarioRepositoryPort usuarios, CifradorContrasenaPort cifrador,
                               GeneradorTokenPort generadorToken) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.generadorToken = generadorToken;
    }

    @Override
    @Transactional(readOnly = true)
    public String iniciarSesion(IniciarSesionComando comando) {
        Optional<Usuario> encontrado = usuarios.buscarPorEmail(Usuario.normalizarEmail(comando.email()));
        if (encontrado.isEmpty()) {
            // Comparar igual, así la respuesta tarda parecido exista o no el correo
            cifrador.simularComparacion(comando.password());
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = encontrado.get();
        if (!cifrador.coincide(comando.password(), usuario.passwordHash())) {
            throw new CredencialesInvalidasException();
        }
        if (!usuario.activo()) {
            throw new CuentaDesactivadaException();
        }
        return generadorToken.generarToken(usuario);
    }
}
