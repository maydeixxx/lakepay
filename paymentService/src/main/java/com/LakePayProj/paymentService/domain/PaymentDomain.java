package com.LakePayProj.paymentService.domain;

import lombok.Data;

@Data
public class PaymentDomain {
    private Long id;
    private Long userId;
    private Long adId;
    private String invoiceId;
    private String currency;
    private Double amount;
    private String status; // PENDING, COMPLETED, FAILED
    private String createdAt;
}
