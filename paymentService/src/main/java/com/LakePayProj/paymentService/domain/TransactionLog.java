package com.LakePayProj.paymentService.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionLog {
    private String type; // "TOP_UP" или "PURCHASE"
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
