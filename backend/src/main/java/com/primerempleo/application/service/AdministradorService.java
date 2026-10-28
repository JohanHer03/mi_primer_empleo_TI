package com.primerempleo.application.service;

import com.primerempleo.application.port.in.CrearAdministradorUseCase;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService implements CrearAdministradorUseCase {

    /** Los mismos límites que el registro: BCrypt solo usa los primeros 72 bytes. */
    private static final int LARGO_MINIMO_CONTRASENA = 8;
    private static final int LARGO_MAXIMO_CONTRASENA = 72;

    private final UsuarioRepositoryPort usuarios;
    private final CifradorContrasenaPort cifrador;

    public AdministradorService(UsuarioRepositoryPort usuarios, CifradorContrasenaPort cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    @Override
    @Transactional
    public boolean crearSiNoExiste(String email, String password) {
        String correo = Usuario.normalizarEmail(email);
        if (usuarios.existePorEmail(correo)) {
            return false;
        }
        if (password == null || password.length() < LARGO_MINIMO_CONTRASENA
                || password.length() > LARGO_MAXIMO_CONTRASENA) {
            throw new DatosInvalidosException("password: debe tener entre 8 y 72 caracteres");
        }
        usuarios.guardar(Usuario.nuevo(correo, cifrador.cifrar(password), Rol.ADMIN));
        return true;
    }
}
