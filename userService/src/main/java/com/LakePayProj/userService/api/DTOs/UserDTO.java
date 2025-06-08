package com.LakePayProj.userService.api.DTOs;

import com.LakePayProj.userService.domain.valueObject.Role;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private List<String> subscriptions;
    private Long tgId;
    private Long chatId;
    private String urlPhoto;
    private LocalDate dateOfReg;
    private BigDecimal balance;
    private Collection<Role> role;
}