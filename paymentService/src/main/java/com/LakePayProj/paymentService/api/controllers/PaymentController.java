package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import com.LakePayProj.paymentService.application.interfaces.repos.PaymentRepository;
import com.LakePayProj.paymentService.application.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/create_payment")
    public ResponseEntity<String> createPayment(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("userId") || !request.containsKey("adId") ||
                !request.containsKey("asset") || !request.containsKey("amount")) {
            log.error("Отсутствуют обязательные параметры: {}", request);
            return ResponseEntity.badRequest().body("Отсутствуют userId, adId, asset или amount");
        }

        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long adId = Long.valueOf(request.get("adId").toString());
            String asset = (String) request.get("asset");
            Double amount = Double.valueOf(request.get("amount").toString());

            Map<String, String> result = paymentService.createInvoice(userId, adId, asset, amount);
            String payUrl = result.get("payUrl");
            String invoiceId = result.get("invoiceId");

            PaymentEntity payment = new PaymentEntity();
            payment.setUserId(userId);
            payment.setAdId(adId);
            payment.setCurrency(asset);
            payment.setAmount(amount);
            payment.setStatus("PENDING");
            payment.setCreatedAt(LocalDateTime.now().toString());
            payment.setInvoiceId(invoiceId);
            paymentRepository.save(payment);

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "adId", adId,
                    "payUrl", payUrl
            ));
            kafkaTemplate.send("payment_created", message);

            return ResponseEntity.ok(payUrl);
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка создания платежа: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Не удалось создать платёж");
        }
    }

    @KafkaListener(topics = "payment_request", groupId = "payment-group")
    public void handlePaymentRequest(ConsumerRecord<String, String> record) {
        try {
            String jsonString = record.value();
            Map<String, Object> request = objectMapper.readValue(jsonString, Map.class);
            if (request.containsKey("currency")) {
                request.put("asset", request.get("currency"));
                request.remove("currency");
            }
            createPayment(request);
        } catch (Exception e) {
            log.error("Ошибка обработки payment_request: {}", e.getMessage(), e);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> webhookData,
                                              @RequestHeader("Crypto-Pay-Signature") String signature) {
        log.info("Получен webhook: {}", webhookData);

        String invoiceId = (String) webhookData.get("invoice_id");
        String status = (String) webhookData.get("status");
        String payload = (String) webhookData.get("payload");

        if (invoiceId == null || status == null || payload == null) {
            log.error("Недопустимый webhook: invoice_id, status или payload отсутствуют");
            return ResponseEntity.badRequest().build();
        }

        PaymentEntity payment = paymentRepository.findByInvoiceId(invoiceId);
        if (payment == null) {
            log.warn("Платёж с invoice_id={} не найден", invoiceId);
            return ResponseEntity.ok().build();
        }

        String expectedPayload = payment.getUserId() + ":" + payment.getAdId();
        if (!expectedPayload.equals(payload)) {
            log.error("Несоответствие payload: ожидалось {}, получено {}", expectedPayload, payload);
            return ResponseEntity.badRequest().build();
        }

        if ("paid".equals(status)) {
            payment.setStatus("COMPLETED");
            paymentRepository.save(payment);

            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "userId", payment.getUserId(),
                        "adId", payment.getAdId(),
                        "amount", payment.getAmount(),
                        "asset", payment.getCurrency()
                ));
                kafkaTemplate.send("payment_confirmed", message);
                log.info("Платёж {} завершён", invoiceId);
            } catch (Exception e) {
                log.error("Ошибка отправки payment_confirmed: {}", e.getMessage(), e);
            }
        }

        return ResponseEntity.ok().build();
    }
}