package com.LakePayProj.paymentService.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long adId;
    private String invoiceId;
    private String currency;
    private Double amount;
    private String status; // PENDING, COMPLETED, FAILED
    private String createdAt;
}