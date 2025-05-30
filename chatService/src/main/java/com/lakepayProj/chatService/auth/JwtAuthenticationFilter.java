package com.lakepayProj.chatService.auth;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.chatService.enums.UserRole;
import com.lakepayProj.chatService.models.User;
import com.lakepayProj.chatService.services.JwtService;
import com.lakepayProj.chatService.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = null;

        // Получаем токен из заголовка
        var authHeader = request.getHeader(HEADER_NAME);

        if (StringUtils.hasText(authHeader) && StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX)) {
            jwt = authHeader.substring(BEARER_PREFIX.length());
        } else {
            log.debug("No valid Authorization header found.");
            filterChain.doFilter(request, response);
            return;
        }

        // Если токен валиден, то аутентифицируем пользователя
        log.debug("JWT received: {}", jwt);
        var id = jwtService.extractUserId(jwt);

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // User user = userService.getById(id);
                // Mock user
                User user = userService.getById();
                if (user == null) {
                    log.error("Couldn't find user with ID {}", id);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtService.isTokenValid(jwt, user)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);

                    return;
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("LakePay not responding: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (JsonProcessingException e) {
                log.error("Couldn't parse user: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

