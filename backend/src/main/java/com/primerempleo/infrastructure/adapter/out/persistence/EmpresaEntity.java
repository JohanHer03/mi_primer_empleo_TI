package com.primerempleo.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/** Tabla empresas. fecha_creacion no se mapea: la pone la base con su valor por defecto. */
@Entity
@Table(name = "empresas")
public class EmpresaEntity extends EntidadConIdAsignado {

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nit", nullable = false)
    private String nit;

    @Column(name = "sector")
    private String sector;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "sitio_web")
    private String sitioWeb;

    @Column(name = "verificada", nullable = false)
    private boolean verificada;

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public boolean isVerificada() {
        return verificada;
    }

    public void setVerificada(boolean verificada) {
        this.verificada = verificada;
    }
}
