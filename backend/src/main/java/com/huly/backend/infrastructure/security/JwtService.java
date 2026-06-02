package com.huly.backend.infrastructure.security;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Implementación del servicio JWT usando la librería JJWT.
 * Genera y valida tokens JWT firmados con HMAC-SHA256.
 *
 * Dos tipos de tokens:
 *  - accessToken: vida corta, contiene userId, email, role y status en los claims.
 *    El frontend lo incluye en el header "Authorization: Bearer <token>".
 *  - refreshToken: vida larga, solo contiene userId y email.
 *    Se envía en cookie HttpOnly y se persiste en BD para poder revocarlo.
 *
 * La clave de firma (jwt.secret) debe ser suficientemente larga y aleatoria para HMAC-SHA256.
 * Se configura en application.properties y debe diferenciarse entre entornos.
 */
@Service
public class JwtService implements TokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.cookie.secure}")
    private boolean cookieSecure;

    /** Construye la clave de firma HMAC-SHA256 desde el secreto configurado. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Genera el accessToken con claims de identidad y autorización (role, status). */
    @Override
    public String generateAccessToken(Long userId, String email, UserRole role, UserStatus status) {
        return Jwts.builder()
                .setSubject(email)           // Subject estándar JWT = identificador del usuario
                .claim("userId", userId)
                .claim("role", role != null ? role.name() : null)
                .claim("status", status != null ? status.name() : null)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Genera el refreshToken con claims mínimos (sin role/status para reducir superficie). */
    @Override
    public String generateRefreshToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Valida el token verificando la firma y la fecha de expiración.
     * Retorna false sin lanzar excepción para no interrumpir el flujo del filtro.
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Convierte la expiración del refreshToken de milisegundos a segundos (para maxAge de la cookie). */
    @Override
    public long getRefreshTokenMaxAgeSecs() {
        return refreshTokenExpirationMs / 1000;
    }

    @Override
    public boolean isCookieSecure() {
        return cookieSecure;
    }

    /** Extrae el userId del token (útil para lógica que necesita el ID numérico). */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /** Parsea y verifica el token, lanzando JwtException si es inválido o expirado. */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
