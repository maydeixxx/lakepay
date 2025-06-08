package com.lakepayProj.userService.api.controller;


import com.lakepayProj.userService.api.DTOs.JwtAuthenticationResponse;
import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.services.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/telegram")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationService authService;


    @GetMapping
    public ResponseEntity<Resource> getAuthScript() {
        Resource resource = new ClassPathResource("static/tgAuth.html");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=telegramAuth.html");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

//    @PostMapping("/mock")
//    public ResponseEntity<JwtAuthenticationResponse> authenticateMock(
//            @RequestBody Map<String, String> telegramData,
//            HttpServletResponse response
//    ) {
//        JwtAuthenticationResponse result = authService.authenticateTelegramMock(telegramData);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/token")
    public ResponseEntity<JwtAuthenticationResponse> authenticate(
            @RequestBody Map<String, String> telegramData,
            HttpServletResponse response
    ) {
        // System.out.println("Полученные данные: " + telegramData);

        try {
            JwtAuthenticationResponse result = authService.authenticateTelegram(telegramData);
            return ResponseEntity.ok(result);
        } catch (BadCredentialsException e) {
            response.setStatus(403);
            log.error("Authentication failed: {}", e.getMessage());
            return new ResponseEntity<>(new JwtAuthenticationResponse("Authentication failed", null), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            response.setStatus(500);
            log.error("Server error: {}", e.getMessage());
            return new ResponseEntity<>(new JwtAuthenticationResponse("Server error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}