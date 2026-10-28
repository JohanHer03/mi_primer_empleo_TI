package com.primerempleo.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-pruebas-con-mas-de-32-bytes-de-largo";
    private static final Instant AHORA = Instant.parse("2026-10-27T15:00:00Z");
    private static final UUID ID = UUID.fromString("7f3c2a1e-5b4d-4c6e-9f8a-1b2c3d4e5f60");

    private final JwtService servicio = conReloj(AHORA);

    @Test
    void deberiaGenerarUnTokenQueSeValidaConLosMismosDatos() {
        String token = servicio.generarToken(ID, "laura@udea.edu.co", Rol.CANDIDATO);

        assertThat(servicio.validarToken(token))
                .contains(new UsuarioAutenticado(ID, "laura@udea.edu.co", Rol.CANDIDATO));
    }

    @Test
    void deberiaIncluirLosClaimsDelContratoYVencerA120Minutos() {
        String token = servicio.generarToken(ID, "talento@empresa.co", Rol.EMPRESA);

        Claims claims = Jwts.parser().verifyWith(clave(SECRETO)).clock(() -> Date.from(AHORA)).build()
                .parseSignedClaims(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo(ID.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("talento@empresa.co");
        assertThat(claims.get("rol", String.class)).isEqualTo("EMPRESA");
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(AHORA);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(AHORA.plus(Duration.ofMinutes(120)));
    }

    @Test
    void deberiaFirmarConHs256() {
        String token = servicio.generarToken(ID, "admin@primerempleo.co", Rol.ADMIN);

        String algoritmo = Jwts.parser().verifyWith(clave(SECRETO)).clock(() -> Date.from(AHORA)).build()
                .parseSignedClaims(token).getHeader().getAlgorithm();

        assertThat(algoritmo).isEqualTo("HS256");
    }

    @Test
    void deberiaRechazarUnTokenVencido() {
        String token = servicio.generarToken(ID, "laura@udea.edu.co", Rol.CANDIDATO);

        JwtService dosHorasDespues = conReloj(AHORA.plus(Duration.ofMinutes(121)));

        assertThat(dosHorasDespues.validarToken(token)).isEmpty();
    }

    @Test
    void deberiaRechazarUnTokenFirmadoConOtraClave() {
        String token = Jwts.builder()
                .subject(ID.toString())
                .claim("email", "laura@udea.edu.co")
                .claim("rol", "ADMIN")
                .expiration(Date.from(AHORA.plus(Duration.ofHours(1))))
                .signWith(clave("otra-clave-distinta-tambien-con-mas-de-32-bytes"), Jwts.SIG.HS256)
                .compact();

        assertThat(servicio.validarToken(token)).isEmpty();
    }

    @Test
    void deberiaRechazarUnTokenSinFirma() {
        String token = Jwts.builder()
                .subject(ID.toString())
                .claim("email", "laura@udea.edu.co")
                .claim("rol", "ADMIN")
                .expiration(Date.from(AHORA.plus(Duration.ofHours(1))))
                .compact();

        assertThat(servicio.validarToken(token)).isEmpty();
    }

    @Test
    void deberiaRechazarUnTokenConRolDesconocido() {
        String token = Jwts.builder()
                .subject(ID.toString())
                .claim("email", "laura@udea.edu.co")
                .claim("rol", "SUPERUSUARIO")
                .expiration(Date.from(AHORA.plus(Duration.ofHours(1))))
                .signWith(clave(SECRETO), Jwts.SIG.HS256)
                .compact();

        assertThat(servicio.validarToken(token)).isEmpty();
    }

    @Test
    void deberiaRechazarTextoQueNoEsUnToken() {
        assertThat(servicio.validarToken("esto-no-es-un-jwt")).isEmpty();
        assertThat(servicio.validarToken("")).isEmpty();
    }

    @Test
    void deberiaExigirUnSecretoDeAlMenos32Bytes() {
        assertThatThrownBy(() -> new JwtService("corto", 120))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }

    private static JwtService conReloj(Instant instante) {
        return new JwtService(SECRETO, 120, Clock.fixed(instante, ZoneOffset.UTC));
    }

    private static SecretKey clave(String secreto) {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
}
