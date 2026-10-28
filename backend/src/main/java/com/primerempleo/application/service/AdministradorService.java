package com.primerempleo.application.service;

import com.primerempleo.application.port.in.CrearAdministradorUseCase;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.model.PoliticaContrasena;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService implements CrearAdministradorUseCase {

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
        PoliticaContrasena.validar(password);
        usuarios.crear(Usuario.nuevo(correo, cifrador.cifrar(password), Rol.ADMIN));
        return true;
    }
}
