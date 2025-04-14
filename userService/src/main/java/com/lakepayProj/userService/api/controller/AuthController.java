package com.lakepayProj.userService.api.controller;


import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.services.UserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/auth/telegram")
@AllArgsConstructor
public class AuthController {
    private final UserService service;
    private final IUserMapper mapper;

    private final String tgBotToken = "7906616449:AAGLMQphhjOTHCgyAW9d9xlV94vN-Deai54";

    @GetMapping
    public ResponseEntity<Resource> getAuthScript() {
        Resource resource = new ClassPathResource("static/tgAuth.html");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=telegramAuth.html");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private boolean telegramDataIsValid(Map<String, Object> telegramData) {
        String hash = (String) telegramData.get("hash");
        if (hash == null) {
            throw new IllegalArgumentException("Hash is missing from telegram data");
        }

        telegramData.remove("hash");

        if (telegramData.isEmpty()) {
            throw new IllegalArgumentException("Telegram data is empty");
        }

        StringBuilder sb = new StringBuilder();
        telegramData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"));

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        String dataCheckString = sb.toString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(tgBotToken.getBytes(UTF_8));

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(secretKeySpec);

            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(UTF_8));
            StringBuilder validateHash = new StringBuilder();
            for (byte b : hmacBytes) {
                validateHash.append(String.format("%02x", b));
            }

            return hash.equals(validateHash.toString());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }

    @PostMapping("/token")
    public String authenticate(@RequestBody Map<String, Object> telegramData) {
        System.out.println("Полученные данные: " + telegramData);
        if (telegramDataIsValid(telegramData)) {
            Long tgId = Long.valueOf((Integer) telegramData.get("id"));
            String userName = (String) telegramData.get("username");
            String urlPhoto = (String) telegramData.get("photo_url");

            User user = service.findUserByTgId(tgId);
            if (user != null) {
                return "User already exists";
            } else {
                UserEntity createUser = new UserEntity();
                createUser.setTgId(tgId);
                createUser.setUserName(userName);
                createUser.setUrlPhoto(urlPhoto);
                createUser.setDateOfReg(LocalDate.now());
                createUser.setBalance(new BigDecimal(0));
                createUser.setRole(Role.User);
                createUser.setSubscriptions(List.of());

                User newUser = mapper.userEntityToUser(createUser);
                service.saveUser(newUser);
                return "User logged in successfully!";
            }
        }
        return "error";
    }
}