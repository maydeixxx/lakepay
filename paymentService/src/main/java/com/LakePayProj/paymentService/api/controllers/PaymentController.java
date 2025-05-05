package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.api.DTOs.PaymentRequestDto;
import com.LakePayProj.paymentService.api.DTOs.PaymentStatusDto;
import com.LakePayProj.paymentService.application.services.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/deposit")
    public ResponseEntity<PaymentStatusDto> deposit(@RequestBody PaymentRequestDto request) {
        System.out.println("Deposit request received: " + request);
        PaymentStatusDto status = paymentService.deposit(request);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("/purchase")
    public ResponseEntity<PaymentStatusDto> purchaseAd(@RequestBody PaymentRequestDto request) {
        System.out.println("Purchase request received: " + request);
        PaymentStatusDto status = paymentService.purchaseAd(request);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }


}