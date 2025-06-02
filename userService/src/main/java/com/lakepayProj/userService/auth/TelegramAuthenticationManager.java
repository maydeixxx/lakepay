package com.lakepayProj.userService.auth;

import com.lakepayProj.userService.domain.model.TelegramAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class TelegramAuthenticationManager implements AuthenticationManager {

    private String botToken = "7906616449:AAGLMQphhjOTHCgyAW9d9xlV94vN-Deai54";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TelegramAuthenticationToken token = (TelegramAuthenticationToken) authentication;
        Map<String, String> data = token.getTelegramData();

        String hash = data.get("hash");

        if (hash == null) {
            throw new BadCredentialsException("Missing Telegram hash");
        }

        data.remove("hash");

        if (data.isEmpty()) {
            throw new BadCredentialsException("Telegram data is empty");
        }

        String dataCheckString = data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(botToken.getBytes(UTF_8));

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(secretKeySpec);

            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(UTF_8));
            String expectedHash = bytesToHex(hmacBytes);

            if (!expectedHash.equalsIgnoreCase(hash)) {
                throw new BadCredentialsException("Invalid Telegram hash");
            }
        } catch (Exception e) {
            throw new BadCredentialsException("Telegram authentication failed", e);
        }

        // Проверка не истек ли срок годности токена
        long authDate = Long.parseLong(data.get("auth_date"));
        if (System.currentTimeMillis() / 1000 - authDate > 60) {
            throw new BadCredentialsException("Login expired");
        }

        return token;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes)
            result.append(String.format("%02x", b));
        return result.toString();
    }
}
