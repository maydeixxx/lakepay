package com.lakepayProj.chatService.DTOs;

import com.lakepayProj.chatService.enums.UserRole;

public record UserDTO(
        Long id,
        String username,
        String urlPhoto,
        UserRole role
) {}