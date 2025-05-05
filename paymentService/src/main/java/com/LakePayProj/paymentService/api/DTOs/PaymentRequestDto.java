package com.LakePayProj.paymentService.api.DTOs;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private Long userId;
    private Double amount;
    private Long adId; // Для покупки объявления, null для пополнения
}