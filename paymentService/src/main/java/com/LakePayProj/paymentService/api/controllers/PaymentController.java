package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.application.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        paymentService.topUp(userId, amount);
        return ResponseEntity.ok("Пополнение успешно");
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestParam Long userId, @RequestParam BigDecimal total) {
        boolean result = paymentService.purchase(userId, total);
        return result ? ResponseEntity.ok("Покупка прошла успешно") :
                ResponseEntity.badRequest().body("Недостаточно средств");
    }
}

