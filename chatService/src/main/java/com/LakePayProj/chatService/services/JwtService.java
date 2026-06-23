package com.LakePayProj.chatService.services;

import com.LakePayProj.chatService.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    @Value("${token.signing.lifetime}")
    private Duration lifetime;

    public String getUsername(String token) {
        return getFromToken(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return getFromToken(token).get("roles", List.class);
    }

    public Claims getFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSigningKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = getUsername(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getFromToken(token).getExpiration().before(new Date());
    }
}