package com.LakePayProj.userService.dto;

import com.LakePayProj.userService.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private Long telegramId;
    private String username;
    private String avatarUrl;
    private UserRole role;
    private LocalDateTime creationDate;
}