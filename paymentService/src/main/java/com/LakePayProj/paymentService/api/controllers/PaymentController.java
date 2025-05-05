package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.domain.model.PaymentEntity;
import com.LakePayProj.paymentService.application.interfaces.repos.PaymentRepository;
import com.LakePayProj.paymentService.application.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentEntity paymentEntity;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/create_payment")
    public ResponseEntity<String> createPayment(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long adId = Long.valueOf(request.get("adId").toString());
        String currency = (String) request.get("currency");
        Double amount = Double.valueOf(request.get("amount").toString());

        String payUrl = paymentService.createInvoice(userId, adId, currency, amount);

        PaymentEntity payment = new PaymentEntity();
        paymentEntity.setUserId(userId);
        paymentEntity.setAdId(adId);
        paymentEntity.setCurrency(currency);
        paymentEntity.setAmount(amount);
        paymentEntity.setStatus("PENDING");
        paymentEntity.setCreatedAt(LocalDateTime.now().toString());
        paymentEntity.setInvoiceId(payUrl.split("=")[1]); // Извлекаем invoice_id из pay_url
        paymentRepository.save(payment);

        kafkaTemplate.send("payment_created", Map.of(
                "userId", userId,
                "adId", adId,
                "payUrl", payUrl
        ));

        return ResponseEntity.ok(payUrl);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        String invoiceId = (String) webhookData.get("invoice_id");
        String status = (String) webhookData.get("status");

        PaymentEntity payment = paymentRepository.findByInvoiceId(invoiceId);
        if (payment != null && "paid".equals(status)) {
            payment.setStatus("COMPLETED");
            paymentRepository.save(payment);

            kafkaTemplate.send("payment_confirmed", Map.of(
                    "userId", payment.getUserId(),
                    "adId", payment.getAdId(),
                    "amount", payment.getAmount(),
                    "currency", payment.getCurrency()
            ));
        }

        return ResponseEntity.ok().build();
    }
}