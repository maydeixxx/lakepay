package com.lakepayProj.userService.infrastructure;

import com.lakepayProj.userService.domain.valueObject.Role;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column(unique = true)
    private String userName;
    private List<String> subscriptions;
    private Long tgId;
    private Long chatId;
    private String urlPhoto;
    private LocalDate dateOfReg;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private Role role;
}
