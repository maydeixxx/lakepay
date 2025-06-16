package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.exception.DatabaseException;
import com.LakePayProj.userService.exception.InvalidTokenException;
import com.LakePayProj.userService.exception.UserAlreadyExistsException;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.util.JwtTokenProvider;
import com.LakePayProj.userService.security.UserDetailsImpl;
import com.LakePayProj.userService.security.TelegramAuthenticationToken;
import com.LakePayProj.userService.dto.request.AuthRequest;
import com.LakePayProj.userService.dto.response.AuthResponse;
import com.LakePayProj.userService.dto.request.RegisterRequest;
import com.LakePayProj.userService.dto.request.TelegramAuthRequest;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.entity.UserCredential;
import com.LakePayProj.userService.enums.UserRole;
import com.LakePayProj.userService.kafka.UserProducer;
import com.LakePayProj.userService.service.UserDetailsServiceImpl;
import com.LakePayProj.userService.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserProducer userProducer;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;

    @PostMapping("/telegram/auth")
    public ResponseEntity<?> authenticateWithTelegram(@RequestBody TelegramAuthRequest req, HttpServletResponse res) {
        try {
            // Проверка валидности хэша от Telegram
            authManager.authenticate(new TelegramAuthenticationToken(req.toMap()));

            // Сохранение пользователя, если он еще не зарегистрирован
            User user = userService.findByUsername(req.username()).orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(req.username());
                newUser.setAvatarUrl(req.photo_url());
                newUser.setTelegramId(req.id());
                newUser.setRole(UserRole.USER);

                newUser =  userService.create(newUser);
                userProducer.sendUser(newUser);

                return newUser;
            });

            // Генерация токена доступа
            return buildAuthResponse(user, res, "Authentication successful");
        } catch (AuthenticationException e) {
            throw new InvalidTokenException("Invalid Telegram authentication data: " + e.getMessage());
        } catch (Exception e) {
            throw new DatabaseException("Failed to authenticate with Telegram", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req, HttpServletResponse res) {
        if (userService.findByUsername(req.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken: " + req.username());
        }

        UserCredential credential = new UserCredential();
        credential.setPasswordHash(passwordEncoder.encode(req.password()));

        User user = new User();
        user.setUsername(req.username());
        user.setCredential(credential);
        user.setRole(UserRole.USER);

        try {
            user = userService.create(user);
            userProducer.sendUser(user);
            return buildAuthResponse(user, res, "Registration successful");
        } catch (Exception e) {
            throw new DatabaseException("Failed to register user", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpServletResponse res) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return buildAuthResponse(userDetails, res, "Login successful");
        } catch (AuthenticationException e) {
            throw new UserNotFoundException("Invalid credentials for user: " + req.username());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isValid(refreshToken)) {
            throw new InvalidTokenException("Invalid or missing refresh token");
        }

        try {
            String username = jwtTokenProvider.getUsername(refreshToken);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtTokenProvider.generateAccessToken(user);
            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .data(new AuthResponse(newAccessToken))
                    .build());
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException("User not found for refresh: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    // Вспомогательные функции
    private ResponseEntity<ApiResponse<?>> buildAuthResponse(User user, HttpServletResponse res, String message) {
        return buildAuthResponse(new UserDetailsImpl(user), res, message);
    }

    private ResponseEntity<ApiResponse<?>> buildAuthResponse(UserDetails userDetails, HttpServletResponse res, String message) {
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(jwtTokenProvider.refreshTokenValidity)
                .sameSite("Strict")
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(message)
                .data(new AuthResponse(accessToken))
                .build());
    }
}
