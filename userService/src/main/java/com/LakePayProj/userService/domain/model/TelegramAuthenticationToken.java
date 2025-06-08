package com.LakePayProj.userService.domain.model;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Map;

@Getter
public class TelegramAuthenticationToken extends AbstractAuthenticationToken {

    private final Map<String, String> telegramData;

    public TelegramAuthenticationToken(Map<String, String> telegramData) {
        super(null);
        this.telegramData = telegramData;
        setAuthenticated(false);
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
