package com.LakePayProj.paymentService.api.DTOs;

import lombok.Data;

@Data
public class PaymentStatusDto {
    private String transactionId;
    private String status; // SUCCESS, FAILED
    private Double amount;
    private Long userId;
    private Long adId;
}