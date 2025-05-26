package com.lakepayProj.chatService.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.chatService.models.User;
import com.lakepayProj.chatService.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter implements HandshakeInterceptor {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String lakePayUrl = "https://lakepay.ru";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Получаем токен из заголовка
        var authHeader = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        var jwt = authHeader.substring(BEARER_PREFIX.length());

        // Если токен валиден, то аутентифицируем пользователя
        User user = authenticate(jwt);
        if (user != null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

        } else {
            // Продолжить обработку запроса в случае если пользователь не аутентифицирован
            filterChain.doFilter(request, response);
        }
    }

    private User authenticate(String token) {
        try {
            var id = jwtService.extractUserId(token);

            if (StringUtils.hasText(id) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String userResponse = restTemplate.getForObject(lakePayUrl+ "/user_id/" + id, String.class);
                User user = objectMapper.readValue(userResponse, User.class);

                if (jwtService.isTokenValid(token, user)) {
                    return user;
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при аутентификации пользователя: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        var authHeader = request.getHeaders().getFirst(HEADER_NAME);
        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX)) {
            return false;
        }

        var jwt = authHeader.substring(BEARER_PREFIX.length());
        User user = authenticate(jwt);

        if (user != null) {
            attributes.put("id", user.getId());
            attributes.put("tgId", user.getTgId());
            attributes.put("username", user.getUsername());
            attributes.put("urlPhoto", user.getUrlPhoto());
            attributes.put("role", user.getRole().name());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}

