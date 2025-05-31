package com.lakepayProj.userService.api.DTOs;

import com.lakepayProj.userService.domain.valueObject.Role;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link com.lakepayProj.userService.infrastructure.UserEntity}
 */
@Value
public class UserDTO {
    Long id;
    String username;
    List<String> subscriptions;
    Long tgId;
    Long chatId;
    String urlPhoto;
    LocalDate dateOfReg;
    BigDecimal balance;
    Role role;
}