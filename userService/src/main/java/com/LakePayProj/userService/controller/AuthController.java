package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.request.AuthRequest;
import com.LakePayProj.userService.dto.request.RegisterRequest;
import com.LakePayProj.userService.dto.request.TelegramAuthRequest;
import com.LakePayProj.userService.dto.request.UpdatePasswordRequest;
import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.dto.response.AuthResponse;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.entity.UserCredential;
import com.LakePayProj.userService.enums.UserRole;
import com.LakePayProj.userService.exception.DatabaseException;
import com.LakePayProj.userService.exception.InvalidTokenException;
import com.LakePayProj.userService.exception.UserAlreadyExistsException;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.kafka.UserProducer;
import com.LakePayProj.userService.security.TelegramAuthenticationToken;
import com.LakePayProj.userService.security.UserDetailsImpl;
import com.LakePayProj.userService.service.JwtTokenService;
import com.LakePayProj.userService.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
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
                newUser.setRole(UserRole.ROLE_USER);

                newUser = userService.create(newUser);
                userProducer.publishCreateUser(newUser);

                return newUser;
            });

            // Привязать аккаунт телеграм, если не привязан
            if (user.getTelegramId() == null) {
                user.setTelegramId(req.id());
                user = userService.update(user.getId(), user);
                userProducer.publishUpdateUser(user);
            }

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
        user.setRole(UserRole.ROLE_USER);

        try {
            user = userService.create(user);
            userProducer.publishCreateUser(user);
            return buildAuthResponse(user, res, "Registration successful");
        } catch (Exception e) {
            log.error("Failed to register user", e);
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
            log.error("Invalid credentials for user: {}", req.username(), e);
            throw new UserNotFoundException("Invalid credentials for user: " + req.username());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenService.isValid(refreshToken)) {
            throw new InvalidTokenException("Invalid or missing refresh token");
        }

        try {
            String username = jwtTokenService.getUsername(refreshToken);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtTokenService.generateAccessToken(user);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        if (refreshToken == null || !jwtTokenService.isValid(refreshToken)) {
            throw new InvalidTokenException("No valid session to log out");
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing access token");
        }

        String accessToken = authorizationHeader.substring(7);

        return invalidateTokensResponse(accessToken, refreshToken, "Successfully logged out");
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody UpdatePasswordRequest req
    ) {
        if (refreshToken == null || !jwtTokenService.isValid(refreshToken)) {
            throw new InvalidTokenException("No valid session to log out");
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing access token");
        }

        String accessToken = authorizationHeader.substring(7);

        String userId = jwtTokenService.getUsername(accessToken);
        User user = userService.findById(Long.parseLong(userId)).orElseThrow(() -> new UserNotFoundException("Can not find user with ID: " + userId));

        if (user.getCredential() == null) {
            // Для пользователей вошедших через телеграм пароль пустой
            if (req.currentPassword() != null && !req.currentPassword().isEmpty()) {
                throw new InvalidTokenException("Telegram users cannot provide a current password; set a new password instead");
            }
            UserCredential credential = new UserCredential();
            credential.setPasswordHash(passwordEncoder.encode(req.newPassword()));
            user.setCredential(credential);
        } else {
            // Валидация текущего пароля
            if (!passwordEncoder.matches(req.currentPassword(), user.getCredential().getPasswordHash())) {
                throw new InvalidTokenException("Current password is incorrect");
            }
            user.getCredential().setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }

        try {
            // Обновление пароля
            userService.update(user.getId(), user);
        } catch (Exception e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }

        return invalidateTokensResponse(accessToken, refreshToken, "Password updated successfully");
    }

    // Вспомогательные функции
    private ResponseEntity<ApiResponse<?>> buildAuthResponse(User user, HttpServletResponse res, String message) {
        return buildAuthResponse(new UserDetailsImpl(user), res, message);
    }

    private ResponseEntity<ApiResponse<?>> buildAuthResponse(UserDetails userDetails, HttpServletResponse res, String message) {
        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(jwtTokenService.refreshTokenValidity)
                .sameSite("Strict")
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(message)
                .data(new AuthResponse(accessToken))
                .build());
    }

    private ResponseEntity<ApiResponse<?>> invalidateTokensResponse(String accessToken, String refreshToken, String message) {
        try {
            // Инвалидация токенов доступа
            jwtTokenService.blockToken(accessToken);
            jwtTokenService.blockToken(refreshToken);

            // Очистить куки с refresh токеном
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth/refresh")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.<Void>builder()
                            .success(true)
                            .message(message)
                            .build());
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException("User not found during logout: " + e.getMessage());
        } catch (Exception e) {
            throw new DatabaseException("Failed to logout: " + e.getMessage(), e);
        }
    }
}
