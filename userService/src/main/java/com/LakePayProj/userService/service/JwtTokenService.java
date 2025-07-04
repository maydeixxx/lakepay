package com.LakePayProj.userService.service;

import com.LakePayProj.userService.entity.BlockedToken;
import com.LakePayProj.userService.repository.IBlockedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final SecretKey jwtSecretKey;

    @Value("${token.access.lifetime}")
    public Duration accessTokenValidity;
    @Value("${token.refresh.lifetime}")
    public Duration refreshTokenValidity;

    private final IBlockedTokenRepository blockedTokenRepository;

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
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);

            return blockedTokenRepository.findByToken(token).isEmpty() &&
                    !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public void blockToken(String token) {
        Instant now = Instant.now();
        blockedTokenRepository.save(BlockedToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.from(now.plus(refreshTokenValidity)))
                .build());
    }
}
