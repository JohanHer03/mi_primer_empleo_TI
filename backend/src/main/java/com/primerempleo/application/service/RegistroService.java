package com.primerempleo.application.service;

import com.primerempleo.application.port.in.RegistrarCandidatoUseCase;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.GeneradorTokenPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de candidatos (HU-01) y empresas (HU-06): el usuario y su perfil se crean en una sola transacción
 * y la respuesta trae el JWT, así la persona queda con la sesión iniciada.
 * Orden: primero las reglas del dominio (400), luego los duplicados (409), y al final se guarda.
 */
@Service
public class RegistroService implements RegistrarCandidatoUseCase, RegistrarEmpresaUseCase {

    static final String CORREO_REPETIDO = "Ya existe una cuenta con ese correo";
    static final String NIT_REPETIDO = "Ya existe una empresa registrada con ese NIT";

    private final UsuarioRepositoryPort usuarios;
    private final CandidatoRepositoryPort candidatos;
    private final EmpresaRepositoryPort empresas;
    private final CifradorContrasenaPort cifrador;
    private final GeneradorTokenPort generadorToken;

    public RegistroService(UsuarioRepositoryPort usuarios, CandidatoRepositoryPort candidatos,
                           EmpresaRepositoryPort empresas, CifradorContrasenaPort cifrador,
                           GeneradorTokenPort generadorToken) {
        this.usuarios = usuarios;
        this.candidatos = candidatos;
        this.empresas = empresas;
        this.cifrador = cifrador;
        this.generadorToken = generadorToken;
    }

    @Override
    @Transactional
    public String registrar(RegistrarCandidatoComando comando) {
        Usuario usuario = Usuario.nuevo(comando.email(), cifrador.cifrar(comando.password()), Rol.CANDIDATO);
        Candidato candidato = Candidato.nuevo(usuario.id(), comando.nombres(), comando.apellidos(),
                comando.universidad(), comando.programa(), comando.estadoAcademico(), comando.semestre(),
                comando.ciudad());

        exigirCorreoLibre(usuario);

        usuarios.guardar(usuario);
        candidatos.guardar(candidato);
        return generadorToken.generarToken(usuario);
    }

    @Override
    @Transactional
    public String registrar(RegistrarEmpresaComando comando) {
        Usuario usuario = Usuario.nuevo(comando.email(), cifrador.cifrar(comando.password()), Rol.EMPRESA);
        Empresa empresa = Empresa.nueva(usuario.id(), comando.razonSocial(), comando.nit(), comando.sector(),
                comando.ciudad(), comando.sitioWeb());

        exigirCorreoLibre(usuario);
        if (empresas.existePorNit(empresa.nit())) {
            throw new RecursoDuplicadoException(NIT_REPETIDO);
        }

        usuarios.guardar(usuario);
        empresas.guardar(empresa);
        return generadorToken.generarToken(usuario);
    }

    private void exigirCorreoLibre(Usuario usuario) {
        if (usuarios.existePorEmail(usuario.email())) {
            throw new RecursoDuplicadoException(CORREO_REPETIDO);
        }
    }
}
