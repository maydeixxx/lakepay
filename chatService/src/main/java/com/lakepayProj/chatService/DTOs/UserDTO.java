package com.lakepayProj.chatService.DTOs;

import lombok.Value;

import com.lakepayProj.chatService.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
public class UserDTO {
    Long id;
    String userName;
    List<String> subscriptions;
    Long tgId;
    Long chatId;
    String urlPhoto;
    LocalDate dateOfReg;
    BigDecimal balance;
    Role role;
}
