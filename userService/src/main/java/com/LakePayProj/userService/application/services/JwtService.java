package com.LakePayProj.userService.application.services;

import com.LakePayProj.userService.domain.model.User;
import com.LakePayProj.userService.domain.valueObject.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

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

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        claims.put("id", user.getId());
        claims.put("tgId", user.getTgId());
        claims.put("roles", roles);
        claims.put("urlPhoto", user.getUrlPhoto());

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + lifetime.toMillis());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, jwtSigningKey)
                .compact();
    }

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
}
