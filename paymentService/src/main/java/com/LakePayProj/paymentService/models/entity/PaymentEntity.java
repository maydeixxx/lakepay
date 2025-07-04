package com.LakePayProj.paymentService.models.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String invoiceId;
    private String currency;
    private BigDecimal amount;
    private String status; // PENDING, COMPLETED, FAILED
    private String createdAt;
}