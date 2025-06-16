package com.LakePayProj.userService.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final SecretKey jwtSecretKey;

    @Value("${token.access.lifetime}")
    public Duration accessTokenValidity;
    @Value("${token.refresh.lifetime}")
    public Duration refreshTokenValidity;

    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getAuthorities().iterator().next().getAuthority())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenValidity)))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenValidity)))
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
