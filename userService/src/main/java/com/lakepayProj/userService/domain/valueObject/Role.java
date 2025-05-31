package com.lakepayProj.userService.domain.valueObject;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@AllArgsConstructor
public enum Role {
    Admin,
    Moderator,
    User
}
