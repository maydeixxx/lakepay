package com.LakePayProj.userService.dto;

import com.LakePayProj.userService.enums.UserRole;

import java.time.LocalDateTime;

public record PublicUserDto(
        String username,
        String fullName,
        String avatarUrl,
        UserRole role,
        LocalDateTime creationDate
) {
}
