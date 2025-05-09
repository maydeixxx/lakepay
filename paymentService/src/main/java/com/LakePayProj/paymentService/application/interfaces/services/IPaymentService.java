package com.LakePayProj.paymentService.application.interfaces.services;

import org.springframework.stereotype.Service;

@Service
public interface IPaymentService {
    String createInvoice(Long userId, Long adId, String currency, Double amount);
}
