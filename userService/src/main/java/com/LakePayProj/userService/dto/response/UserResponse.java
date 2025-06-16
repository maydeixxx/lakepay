package com.LakePayProj.userService.dto.response;

import com.LakePayProj.userService.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        Long telegramId,
        String username,
        String avatarUrl,
        UserRole role,
        LocalDateTime creationDate
) {}