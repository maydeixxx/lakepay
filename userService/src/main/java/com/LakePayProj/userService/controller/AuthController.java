package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.auth.JwtTokenProvider;
import com.LakePayProj.userService.auth.LakepayUserDetails;
import com.LakePayProj.userService.auth.TelegramAuthenticationToken;
import com.LakePayProj.userService.dto.AuthRequest;
import com.LakePayProj.userService.dto.AuthResponse;
import com.LakePayProj.userService.dto.RegisterRequest;
import com.LakePayProj.userService.dto.TelegramAuthRequest;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.entity.UserCredential;
import com.LakePayProj.userService.enums.UserRole;
import com.LakePayProj.userService.service.LakepayUserDetailsService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final LakepayUserDetailsService userDetailsService;

    @PostMapping("/telegram/auth")
    public ResponseEntity<?> authenticateWithTelegram(@RequestBody TelegramAuthRequest req, HttpServletResponse res) {
        // Проверка валидности хэша от Telegram
        authManager.authenticate(new TelegramAuthenticationToken(req.toMap()));

        // Сохранение пользователя, если он еще не зарегистрирован
        User user = userService.findByUsername(req.username()).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(req.username());
            newUser.setAvatarUrl(req.photo_url());
            newUser.setTelegramId(req.id());
            newUser.setRole(UserRole.USER);
            return userService.create(newUser);
        });

        // Генерация токена доступа
        return buildAuthResponse(user, res);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req, HttpServletResponse res) {
        if (userService.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        UserCredential credential = new UserCredential();
        credential.setPasswordHash(passwordEncoder.encode(req.password()));

        User user = new User();
        user.setUsername(req.username());
        user.setCredential(credential);
        user.setRole(UserRole.USER);

        user = userService.create(user);
        return buildAuthResponse(user, res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpServletResponse res) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        return buildAuthResponse(userDetails, res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.isValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing refresh token");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        UserDetails user = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return ResponseEntity.ok(new AuthResponse(newAccessToken));
    }

    // Вспомогательные функции
    private ResponseEntity<AuthResponse> buildAuthResponse(User user, HttpServletResponse res) {
        return buildAuthResponse(new LakepayUserDetails(user), res);
    }

    private ResponseEntity<AuthResponse> buildAuthResponse(UserDetails userDetails, HttpServletResponse res) {
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

        return ResponseEntity.ok(new AuthResponse(accessToken));
    }
}
