package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmpresaTest {

    private static final UUID USUARIO = UUID.randomUUID();

    @Test
    void deberiaCrearLaEmpresaSinVerificarYConElNitSoloEnDigitos() {
        Empresa empresa = Empresa.nueva(USUARIO, "Software Medellín S.A.S.", "900.123.456-7",
                "Desarrollo de software", "Medellín", "https://softwaremedellin.co");

        assertThat(empresa.nit()).isEqualTo("9001234567");
        assertThat(empresa.verificada()).isFalse();
    }

    @Test
    void deberiaQuitarEspaciosDelNit() {
        assertThat(Empresa.normalizarNit(" 900 123 456 7 ")).isEqualTo("9001234567");
    }

    @Test
    void deberiaRechazarUnNitConLetras() {
        assertThatThrownBy(() -> Empresa.normalizarNit("90012ABC"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("nit:");
    }

    @Test
    void deberiaRechazarUnNitMuyCorto() {
        assertThatThrownBy(() -> Empresa.normalizarNit("12.345"))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void deberiaEditarElPerfilSinCambiarNitNiVerificacion() {
        Empresa empresa = Empresa.nueva(USUARIO, "Software Medellín S.A.S.", "9001234567", null, null, null).verificar();

        Empresa editada = empresa.conPerfil(" Software Medellín S.A.S. BIC ", "Fintech", "Envigado", "");

        assertThat(editada.id()).isEqualTo(empresa.id());
        assertThat(editada.nit()).isEqualTo("9001234567");
        assertThat(editada.verificada()).isTrue();
        assertThat(editada.razonSocial()).isEqualTo("Software Medellín S.A.S. BIC");
        assertThat(editada.sitioWeb()).isNull();
    }

    @Test
    void deberiaQuedarVerificadaAlVerificarla() {
        Empresa empresa = Empresa.nueva(USUARIO, "Software Medellín S.A.S.", "9001234567", null, null, null);

        assertThat(empresa.verificada()).isFalse();
        assertThat(empresa.verificar().verificada()).isTrue();
    }

    @Test
    void deberiaExigirRazonSocial() {
        assertThatThrownBy(() -> Empresa.nueva(USUARIO, "", "9001234567", null, null, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("razonSocial: es obligatorio");
    }
}
