package com.lakepayProj.userService.api.controller;

import com.lakepayProj.userService.api.DTOs.JwtAuthenticationResponse;
import com.lakepayProj.userService.application.services.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/telegram")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authService;

    @GetMapping
    public ResponseEntity<Resource> getAuthScript() {
        Resource resource = new ClassPathResource("static/tgAuth.html");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=telegramAuth.html");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @PostMapping("/token")
    public ResponseEntity<JwtAuthenticationResponse> authenticate(@RequestBody Map<String, String> telegramData) {
        try {
            JwtAuthenticationResponse result = authService.authenticateTelegram(telegramData);
            return ResponseEntity.ok(result);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new JwtAuthenticationResponse("Authentication failed", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JwtAuthenticationResponse("Server error: " + e.getMessage(), null));
        }
    }
}