package com.vkcreation.platform.security;

import com.vkcreation.platform.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final String secret;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    private SecretKey key() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured with a base64-encoded secret of at least 32 bytes.");
        }
        byte[] bytes = Decoders.BASE64.decode(secret);
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getId()).claim("email", user.getEmail())
                .claim("role", user.getRole().name()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs))).signWith(key()).compact();
    }

    public String subject(String token) { return claims(token).getSubject(); }

    public boolean isValid(String token, UserAccount user) {
        Claims claims = claims(token);
        return user.getId().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
}
