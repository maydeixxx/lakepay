package com.LakePayProj.paymentService.api.DTOs;

import com.LakePayProj.paymentService.domain.valueObject.PaymentType;
import lombok.Data;

@Data
public class TransactionDto {
    private String id;
    private Long userId;
    private Double amount;
    private PaymentType type; // DEPOSIT, PURCHASE
    private Long adId;
    private String status;
}