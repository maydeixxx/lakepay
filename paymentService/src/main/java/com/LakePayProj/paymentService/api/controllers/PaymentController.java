package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.application.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestParam Long userId, @RequestParam Long adId) {
        boolean result = paymentService.buy(userId, adId);
        return result ?
                ResponseEntity.ok("Покупка прошла успешно") :
                ResponseEntity.badRequest().body("Недостаточно средств");
    }
}
