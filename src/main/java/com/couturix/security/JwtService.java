package com.couturix.security;

import com.couturix.config.CouturixProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final CouturixProperties properties;
    private final SecretKey key;

    public JwtService(CouturixProperties properties) {
        this.properties = properties;
        byte[] bytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 octets");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.jwt().accessTokenMinutes() * 60);
        List<String> roles = principal.getAuthorities().stream().map(Object::toString).toList();
        var builder = Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key);
        if (principal.getAtelierId() != null) {
            builder.claim("atelierId", principal.getAtelierId());
        }
        return builder.compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }
}
