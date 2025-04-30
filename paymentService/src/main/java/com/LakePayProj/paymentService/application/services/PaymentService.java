package com.LakePayProj.paymentService.application.services;
import com.LakePayProj.paymentService.application.interfaces.repos.UserPaymentRepository;
import com.LakePayProj.paymentService.domain.UserPayment;
import com.LakePayProj.paymentService.infrastructure.external.clients.AdClient.AdHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
public class PaymentService {

    @Autowired
    private UserPaymentRepository paymentRepo;

    @Autowired
    private final AdHttpClient adHttpClient;

    public PaymentService(AdHttpClient adHttpClient) {
        this.adHttpClient = adHttpClient;
    }

    public void topUp(Long userId, BigDecimal amount) {
        UserPayment user = paymentRepo.findById(userId)
                .orElse(new UserPayment(userId, BigDecimal.ZERO, new ArrayList<>()));

        user.setBalance(user.getBalance().add(amount));
        user.addTransaction("TOP_UP", amount);

        paymentRepo.save(user);
    }

    public boolean buy(Long userId, Long adId) {
        BigDecimal price = adHttpClient.getAdPrice(adId);

        UserPayment user = paymentRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBalance().compareTo(price) >= 0) {
            user.setBalance(user.getBalance().subtract(price));
            user.addTransaction("PURCHASE", price.negate());
            paymentRepo.save(user);
            return true;
        }
        return false;
    }

}
