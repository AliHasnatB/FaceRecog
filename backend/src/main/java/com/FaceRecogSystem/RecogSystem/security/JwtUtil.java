package com.FaceRecogSystem.RecogSystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // ── JJWT 0.12.x uses SecretKey, not the old Key interface ────────────────
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                // 0.12.x: subject() replaces setSubject()
                .subject(username)
                .claim("role", role)
                // 0.12.x: issuedAt() replaces setIssuedAt()
                .issuedAt(new Date())
                // 0.12.x: expiration() replaces setExpiration()
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // 0.12.x: signWith(key) — algorithm is inferred from key type
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        // 0.12.x: parser() replaces parserBuilder()
        // verifyWith() replaces setSigningKey()
        // parseSignedClaims() replaces parseClaimsJws()
        // .getPayload() replaces .getBody()
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}