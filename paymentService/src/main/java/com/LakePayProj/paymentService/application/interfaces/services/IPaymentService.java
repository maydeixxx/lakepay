package com.LakePayProj.paymentService.application.interfaces.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface IPaymentService {
    String createInvoice(BigDecimal amount, String currency, String description);
    void buyAd(Long userId, Long adId);
    BigDecimal updateUserBalance(Long userId, BigDecimal amount, String operation, String asset);
    void updateAdStatus(Long adId);
    boolean transferFunds(Long userId, BigDecimal amount, String currency);
    BigDecimal getExchangeCourse(String sourceAsset, String targetAsset);
}
