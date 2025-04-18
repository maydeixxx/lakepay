package com.LakePayProj.paymentService.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Account {
    @Id
    private Long userId;
    private BigDecimal balance;
}

