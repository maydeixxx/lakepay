package com.LakePayProj.paymentService.api.DTOs;

import lombok.Value;

/**
 * DTO for {@link com.LakePayProj.paymentService.infrastructure.PaymentEntity}
 */
@Value
public class PaymentDto {
    Long id;
    Long userId;
    Long adId;
    String invoiceId;
    String currency;
    Double amount;
    String status;
    String createdAt;
}