package com.LakePayProj.userService.dto;

import com.LakePayProj.userService.enums.UserRole;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        Long telegramId,
        String username,
        String avatarUrl,
        UserRole role,
        LocalDateTime creationDate
) {
}