package com.LakePayProj.paymentService.application.interfaces.services;

import com.LakePayProj.paymentService.api.DTOs.PaymentRequestDto;
import com.LakePayProj.paymentService.api.DTOs.PaymentStatusDto;
import org.springframework.transaction.annotation.Transactional;

public interface IPaymentService {
    @Transactional
    PaymentStatusDto deposit(PaymentRequestDto request);

    @Transactional
    PaymentStatusDto purchaseAd(PaymentRequestDto request);
}
