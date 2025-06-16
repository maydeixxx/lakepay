package com.LakePayProj.userService.auth;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

@Getter
public class TelegramAuthenticationToken extends AbstractAuthenticationToken {
    private final Map<String, String> telegramData;

    public TelegramAuthenticationToken(Map<String, String> telegramData) {
        super(null);
        this.telegramData = telegramData;
        this.setAuthenticated(false);
    }

    public TelegramAuthenticationToken(Map<String, String> telegramData, @Nullable Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.telegramData = telegramData;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return telegramData.get("hash");
    }

    @Override
    public Object getPrincipal() {
        return telegramData.get("username");
    }

}
