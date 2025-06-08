package com.LakePayProj.chatService.DTOs;

import com.LakePayProj.chatService.enums.UserRole;

public record UserDTO(
        Long id,
        String username,
        String urlPhoto,
        UserRole role
) {}