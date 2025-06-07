package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.application.interfaces.repos.PaymentRepository;
import com.LakePayProj.paymentService.application.services.PaymentService;
import com.LakePayProj.paymentService.application.services.kafka.PaymentProducer;
import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

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

            Optional<PaymentEntity> existingPayment = paymentRepository.findByInvoiceId(invoiceId);
            if (existingPayment.isPresent() && "COMPLETED".equals(existingPayment.get().getStatus())) {
                log.info("Счет {} уже обработан", invoiceId);
                return ResponseEntity.ok().build();
            }

            PaymentEntity payment = existingPayment.orElseGet(() -> {
                PaymentEntity newPayment = new PaymentEntity();
                newPayment.setInvoiceId(invoiceId);
                newPayment.setStatus("PENDING");
                return paymentRepository.save(newPayment);
            });

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
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> data) {
        try {
            String userId = data.get("userId").toString();
            producer.getUserDataById(userId);
            String currency = data.get("currency").toString();
            BigDecimal amount = new BigDecimal(data.get("amount").toString());

            if (paymentRepository.existsByUserIdAndStatus(Long.parseLong(userId), "PENDING")) {
                log.warn("Для пользователя userId={} уже существует активный счет", userId);
                return ResponseEntity.badRequest().body("У пользователя уже есть активный счет");
            }

            String payUrl = service.createInvoice(amount, currency, "Deposit for user " + userId);
            if (payUrl == null) {
                return ResponseEntity.badRequest().body("Не удалось создать счет");
            }

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "payUrl", payUrl,
                    "amount", amount,
                    "currency", currency
            ));
            producer.sendPaymentCreated(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка создания счета: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Bad request: " + e.getMessage());
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<Void> buyAd(@RequestBody Map<String, Long> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long adId = Long.valueOf(data.get("adId").toString());
        service.buyAd(userId, adId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String currency = request.get("currency").toString();
            log.debug("Запрос на вывод средств: userId={}, amount={}, currency={}", userId, amount, currency);

            producer.getUserDataById(userId.toString());
            Thread.sleep(2000);
            Map<String, Object> userData = service.getUserDataFromCache(userId);
            if (userData == null) {
                log.error("Данные пользователя не найдены для userId={}", userId);
                return ResponseEntity.badRequest().body("Данные пользователя не найдены");
            }
            Long tgId = Long.valueOf(userData.get("tgId").toString());
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());
            log.info("balance = {}, tgId = {}", balance, tgId);
            BigDecimal amountInUsd;
            BigDecimal rate = service.getExchangeCourse(currency, "USD");
            amountInUsd = amount.multiply(rate);

            if (balance.compareTo(amountInUsd) < 0) {
                log.warn("Недостаточно средств для вывода: userId={}, balance={}, amountInUsd={}", userId, balance, amountInUsd);
                return ResponseEntity.badRequest().body("Недостаточно средств на балансе");
            }

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", tgId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance
            ));
            kafkaTemplate.send("withdraw_request", userId.toString(), message);
            log.info("Запрос на вывод отправлен в Kafka: userId={}, amount={}, currency={}", userId, amount, currency);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка обработки запроса на вывод: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}