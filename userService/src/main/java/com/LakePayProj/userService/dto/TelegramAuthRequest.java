package com.LakePayProj.userService.dto;

import java.util.HashMap;
import java.util.Map;

public record TelegramAuthRequest(
        Long id,
        String first_name,
        String last_name,
        String username,
        String photo_url,
        Long auth_date,
        String hash
) {
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(id));
        map.put("first_name", first_name);
        map.put("last_name", last_name);
        map.put("username", username);
        map.put("photo_url", photo_url);
        map.put("auth_date", String.valueOf(auth_date));
        map.put("hash", hash);
        return map;
    }
}
