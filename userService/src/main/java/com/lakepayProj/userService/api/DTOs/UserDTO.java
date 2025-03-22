package com.lakepayProj.userService.api.DTOs;

import com.lakepayProj.userService.domain.valueObject.Role;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link com.lakepayProj.userService.infrastructure.UserEntity}
 */
@Value
public class UserDTO {
    Long id;
    String userName;
    Long tgId;
    String urlPhoto;
    LocalDate dateOfReg;
    BigDecimal balance;
    Role role;
}