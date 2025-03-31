package com.lakepayProj.userService.domain.model;

import com.lakepayProj.userService.domain.valueObject.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private Long id;
    private String userName;
    private Long tgId;
    private Long chatId;
    private String urlPhoto;
    private LocalDate dateOfReg;
    private BigDecimal balance;
    private Role role;
}
