package com.LakePayProj.userService.security;

import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.service.UserDetailsServiceImpl;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationManagerImpl implements AuthenticationManager {

    @Value("${token.telegram.bot}")
    private String botToken;

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication instanceof TelegramAuthenticationToken telegramToken) {
            return authenticateTelegram(telegramToken);
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken upToken) {
            return authenticateWithPassword(upToken);
        }

        throw new BadCredentialsException("Unsupported authentication type: " + authentication.getClass().getSimpleName());
    }

    /**
     * Аутентификация с Telegram логин виджета
     */
    private Authentication authenticateTelegram(TelegramAuthenticationToken auth) {
        Map<String, String> data = auth.getTelegramData();

        String hash = data.get("hash");
        if (hash == null) throw new BadCredentialsException("Missing Telegram hash");

        data.remove("hash");
        if (data.isEmpty()) throw new BadCredentialsException("Telegram data is empty");

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

            log.debug("Expected telegram hash: {}, at timestamp: {}", expectedHash, data.get("auth_date"));

            if (!expectedHash.equals(hash)) {
                throw new BadCredentialsException("Invalid Telegram hash");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new BadCredentialsException("Telegram authentication error", e);
        }

        long authDate = Long.parseLong(data.get("auth_date"));
        if (System.currentTimeMillis() / 1000 - authDate > 60) {
            throw new BadCredentialsException("Login expired");
        }

        return auth;
    }

    /**
     * Аутентификация с помощью пароля и логина
     */
    private Authentication authenticateWithPassword(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getPrincipal().toString();
        String password = auth.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String passwordHash = userDetails.getPassword();

        if (!passwordEncoder.matches(password, passwordHash)) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes)
            result.append(String.format("%02x", b));
        return result.toString();
    }
}
