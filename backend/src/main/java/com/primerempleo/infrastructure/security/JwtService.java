package com.primerempleo.infrastructure.security;

import com.primerempleo.domain.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Genera y valida los JWT de la API (docs/contrato-api.md, sección 2.1):
 * HS256 con JWT_SECRET y claims sub (id del usuario), email, rol, iat y exp.
 */
@Component
public class JwtService {

    /** HS256 exige una clave de al menos 256 bits. */
    private static final int BYTES_MINIMOS_SECRETO = 32;
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final Duration expiracion;
    private final Clock reloj;
    private final JwtParser parser;

    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this(secreto, expiracionMinutos, Clock.systemUTC());
    }

    JwtService(String secreto, long expiracionMinutos, Clock reloj) {
        byte[] bytesSecreto = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytesSecreto.length < BYTES_MINIMOS_SECRETO) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + BYTES_MINIMOS_SECRETO + " bytes (por ejemplo: openssl rand -base64 48)");
        }
        this.clave = Keys.hmacShaKeyFor(bytesSecreto);
        this.expiracion = Duration.ofMinutes(expiracionMinutos);
        this.reloj = reloj;
        this.parser = Jwts.parser()
                .verifyWith(clave)
                .clock(() -> Date.from(reloj.instant()))
                .build();
    }

    public String generarToken(UUID idUsuario, String email, Rol rol) {
        Instant ahora = reloj.instant();
        return Jwts.builder()
                .subject(idUsuario.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracion)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Devuelve el usuario del token si la firma es correcta y no ha vencido.
     * Cualquier token alterado, vencido o con claims inválidos se trata igual: vacío.
     */
    public Optional<UsuarioAutenticado> validarToken(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String sub = claims.getSubject();
            String email = claims.get(CLAIM_EMAIL, String.class);
            String rol = claims.get(CLAIM_ROL, String.class);
            if (sub == null || email == null || rol == null) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioAutenticado(UUID.fromString(sub), email, Rol.valueOf(rol)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
