package com.LakePayProj.paymentService.application.interfaces.services;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface IPaymentService {
    String createInvoice(Double amount, String currency, String description);
    void buyAd(Long userId, Long adId);
    void updateUserBalance(Long userId, Double amount, String operation);
    void updateAdStatus(Long adId);
//    void withdraw(Long userId, )
}
