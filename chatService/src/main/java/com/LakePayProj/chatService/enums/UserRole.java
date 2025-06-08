package com.LakePayProj.chatService.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@AllArgsConstructor
public enum UserRole {
    Admin,
    Moderator,
    User
}
