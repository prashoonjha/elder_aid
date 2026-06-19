package com.elderaid.platform.security;

import com.elderaid.platform.config.JwtProperties;
import com.elderaid.platform.domain.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Access tokens only - refresh tokens are opaque random strings handled by
 * AuthService/RefreshTokenRepository, not JWTs, so they can be revoked
 * server-side without waiting for expiry.
 */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        List<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim(ROLES_CLAIM, roleNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationMinutes() * 60;
    }

    /**
     * Throws JwtException (expired, malformed, bad signature) if invalid -
     * the caller (JwtAuthenticationFilter) decides how to handle that.
     */
    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(Claims claims) {
        return Set.copyOf(claims.get(ROLES_CLAIM, List.class));
    }
}
