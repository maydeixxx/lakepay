package com.LakePayProj.paymentService.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;
    private BigDecimal amount;
    private String type; // "TOP_UP" или "PURCHASE"
    private LocalDateTime timestamp;

    public Transaction(Long userId, BigDecimal amount, String topUp, LocalDateTime now) {
    }
}
