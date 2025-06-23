package com.LakePayProj.paymentService.controllers;

import com.LakePayProj.paymentService.DTOs.ApiResponse;
import com.LakePayProj.paymentService.exceptions.PaymentExistsException;
import com.LakePayProj.paymentService.exceptions.PaymentNotFoundException;
import com.LakePayProj.paymentService.exceptions.TransferFundsException;
import com.LakePayProj.paymentService.exceptions.UserNotFoundException;
import com.LakePayProj.paymentService.repos.PaymentRepository;
import com.LakePayProj.paymentService.services.PaymentService;
import com.LakePayProj.paymentService.services.kafka.PaymentProducer;
import com.LakePayProj.paymentService.entity.PaymentEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class PaymentController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentService service;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PaymentProducer producer;


    @Transactional
    @PostMapping("/pay/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.debug("Получен webhook: payload={}", payload);
        try {
            String type = (String) payload.get("update_type");
            log.debug("Webhook update_type: {}", type);
            if (!"invoice_paid".equals(type)) {
                log.debug("Игнорируем webhook с update_type: {}", type);
                return ResponseEntity.ok().build();
            }

            Map<String, Object> invoice = (Map) payload.get("payload");

            String invoiceId = invoice.get("invoice_id").toString();

            PaymentEntity existingPayment = paymentRepository.findByInvoiceId(invoiceId).orElseThrow(() -> new PaymentNotFoundException(String.format("Payment by invoice id %s not found", invoiceId)));
            if (existingPayment != null && "COMPLETED".equals(existingPayment.getStatus())) {
                log.info("Счет {} уже обработан", invoiceId);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Payment already handled")
                        .build()
                );
            }

            PaymentEntity payment = new PaymentEntity();
            payment.setInvoiceId(invoiceId);
            payment.setStatus("PENDING");
            paymentRepository.save(payment);

            String description = (String) invoice.get("description");
            BigDecimal amount = new BigDecimal(invoice.get("amount").toString());
            String currency = (String) invoice.get("asset");
            log.debug("Webhook invoice: invoiceId={}, description={}, amount={}, currency={}", invoiceId, description, amount, currency);

            if (description.startsWith("Deposit for user")) {
                Long userId = Long.parseLong(description.replace("Deposit for user ", ""));
                Map<String, Object> userData = service.getUserDataFromCache(userId);
                if (userData == null) {
                    log.warn("Данные пользователя не найдены в кэше для userId={}", userId);
                    producer.getUserDataById(userId.toString());
                    Thread.sleep(2000);
                    userData = service.getUserDataFromCache(userId);
                    if (userData == null) {
                        log.error("Не удалось получить данные пользователя для userId={}", userId);
                        return ResponseEntity.badRequest().body("Данные пользователя не найдены");
                    }
                }
                Long tgId = Long.valueOf(userData.get("tgId").toString());

                String operation = "deposit";
                BigDecimal newBalance = service.updateUserBalance(userId, amount, operation, currency);
                payment.setStatus("COMPLETED");
                payment.setUserId(userId);
                payment.setAmount(amount);
                payment.setCurrency(currency);
                paymentRepository.save(payment);

                String message = objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "chatId", tgId,
                        "amount", amount,
                        "currency", currency,
                        "balance", newBalance
                ));
                kafkaTemplate.send("deposit_confirmed", userId.toString(), message);
                log.info("Пополнение подтверждено: userId={}, amount={}, currency={}", userId, amount, currency);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка обработки webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<?>> deposit(@RequestBody Map<String, Object> data) throws JsonProcessingException {
        String userId = data.get("userId").toString();
        producer.getUserDataById(userId);
        String currency = data.get("currency").toString();
        BigDecimal amount = new BigDecimal(data.get("amount").toString());

        if (paymentRepository.existsByUserIdAndStatus(Long.parseLong(userId), "PENDING")) {
            log.warn("Для пользователя userId={} уже существует активный счет", userId);
            throw new PaymentExistsException(String.format("payment already exists for user %s", userId));
        }

        String payUrl = service.createInvoice(amount, currency, "Deposit for user " + userId);

        String message = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "payUrl", payUrl,
                "amount", amount,
                "currency", currency
        ));
        producer.sendPaymentCreated(message);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Successfully created invoice for deposit")
                .data(payUrl)
                .build()
        );
    }

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<?>> buyAd(@RequestBody Map<String, Long> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long adId = Long.valueOf(data.get("adId").toString());
        service.buyAd(userId, adId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("successfully bought ad " + adId)
                .build()
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<?>> withdrawFunds(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String currency = request.get("currency").toString();
            log.debug("Запрос на вывод средств: userId={}, amount={}, currency={}", userId, amount, currency);

            producer.getUserDataById(userId.toString());
            Thread.sleep(2000);
            Map<String, Object> userData = service.getUserDataFromCache(userId);
            if (userData == null) {
                throw new UserNotFoundException(String.format("user %s not found", userId));
            }
            Long tgId = Long.valueOf(userData.get("tgId").toString());
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());
            log.info("balance = {}, tgId = {}", balance, tgId);
            BigDecimal amountInUsd;
            BigDecimal rate = service.getExchangeCourse(currency, "USD");
            amountInUsd = amount.multiply(rate);

            if (balance.compareTo(amountInUsd) < 0) {
                throw new IllegalArgumentException("balance is fewer than amount to transfer");
            }

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", tgId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance
            ));
            producer.sendWithdrawRequest(userId, message);
            log.info("Запрос на вывод отправлен в Kafka: userId={}, amount={}, currency={}", userId, amount, currency);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("success transfer funds")
                    .build()
            );
        } catch (Exception e) {
            throw new TransferFundsException("error transfer funds", e);
        }
    }
}