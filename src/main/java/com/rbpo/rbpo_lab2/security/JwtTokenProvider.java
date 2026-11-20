package com.rbpo.rbpo_lab2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String EXPECTED_ALGORITHM = "HS256";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_AUTH = "auth";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final String secretBase64;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final Clock clock;

    private SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.accessExpiration}") long accessExpirationMs,
            @Value("${jwt.refreshExpiration}") long refreshExpirationMs
    ) {
        this.secretBase64 = secretBase64;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.clock = Clock.systemUTC();
    }

    @PostConstruct
    void init() {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("JWT secret must not be blank");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64.trim());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits) for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public IssuedToken createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> authList = authorities == null
                ? List.of()
                : authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .toList();

        return buildToken(username, TYPE_ACCESS, accessExpirationMs, authList);
    }

    public IssuedToken createRefreshToken(String username) {
        return buildToken(username, TYPE_REFRESH, refreshExpirationMs, null);
    }

    public ParsedToken parseAccess(String token) {
        return parseAndValidate(token, TYPE_ACCESS);
    }

    public ParsedToken parseRefresh(String token) {
        return parseAndValidate(token, TYPE_REFRESH);
    }

    private IssuedToken buildToken(String username, String tokenType, long ttlMs, List<String> authorities) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (ttlMs <= 0) {
            throw new IllegalArgumentException("Token TTL must be positive");
        }

        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plusMillis(ttlMs).truncatedTo(ChronoUnit.SECONDS);
        String jti = UUID.randomUUID().toString();

        var builder = Jwts.builder()
                .subject(username)
                .id(jti)
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt));

        if (authorities != null) {
            builder.claim(CLAIM_AUTH, authorities);
        }

        String jwt = builder
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new IssuedToken(jwt, expiresAt);
    }

    private ParsedToken parseAndValidate(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            throw new InvalidJwtException("JWT token is missing or blank");
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);

            String alg = jws.getHeader().getAlgorithm();
            if (!EXPECTED_ALGORITHM.equals(alg)) {
                throw new InvalidJwtException("Unsupported JWT algorithm: " + alg);
            }

            Claims claims = jws.getPayload();
            String subject = claims.getSubject();
            String jti = claims.getId();
            String type = claims.get(CLAIM_TYPE, String.class);
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();

            if (subject == null || subject.isBlank()
                    || jti == null || jti.isBlank()
                    || issuedAt == null
                    || expiration == null
                    || !expectedType.equals(type)) {
                throw new InvalidJwtException("JWT claims validation failed");
            }

            return new ParsedToken(
                    subject,
                    jti,
                    type,
                    issuedAt.toInstant(),
                    expiration.toInstant()
            );
        } catch (InvalidJwtException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtException("Invalid or expired JWT token", ex);
        }
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public record ParsedToken(
            String subject,
            String jti,
            String type,
            Instant issuedAt,
            Instant expiresAt
    ) {
    }

    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message) {
            super(message);
        }

        public InvalidJwtException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}