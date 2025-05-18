package com.LakePayProj.paymentService.api.controllers;

import com.LakePayProj.paymentService.application.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class PaymentController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentService service;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String lakePayUrl = "https://lakepay.ru";

    @PostMapping("/webhook")
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
            String description = (String) invoice.get("description");
            Double amount = Double.valueOf(invoice.get("amount").toString());
            String currency = (String) invoice.get("asset");
            log.debug("Webhook invoice: description={}, amount={}, currency={}", description, amount, currency);


            if (description.startsWith("Deposit for user")) {
                Long userId = Long.parseLong(description.replace("Deposit for user ", ""));
                String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
                Map<String, Object> data = objectMapper.readValue(response, Map.class);
                Long chatId = Long.valueOf(data.get("chatId").toString());
                log.info("chatId = {}", chatId);
                double cryptoToUsd;
                switch (currency) {
                    case "TRX" -> {
                        cryptoToUsd = amount * 0.27;
                        service.updateUserBalance(userId, cryptoToUsd, "deposit");
                    }
                    case "ETH" -> {
                        cryptoToUsd = amount * 2551.29;
                        service.updateUserBalance(userId, cryptoToUsd, "deposit");
                    }
                    case "BTC" -> {
                        cryptoToUsd = amount * 102521.46;
                        service.updateUserBalance(userId, cryptoToUsd, "deposit");
                    }
                }
                String newResponse = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
                Map<String, Object> userData = objectMapper.readValue(newResponse, Map.class);
                Double balance = Double.valueOf(userData.get("balance").toString());
                String message = objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "chatId", chatId,
                        "amount", amount,
                        "currency", currency,
                        "balance", balance
                ));
                kafkaTemplate.send("deposit_confirmed", message);
                log.info("Пополнение подтверждено: userId={}, amount={}, currency={}", userId, amount, currency);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка обработки webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/buy")
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
            Double amount = Double.valueOf(request.get("amount").toString());
            String currency = request.get("currency").toString();
            log.debug("Запрос на вывод средств: userId={}, amount={}, currency={}", userId, amount, currency);

            String userResponse = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> userData = objectMapper.readValue(userResponse, Map.class);
            Double balance = Double.valueOf(userData.get("balance").toString());
            Long chatId = Long.valueOf(userData.get("chatId").toString());

            if (balance < amount) {
                log.warn("Недостаточно средств для вывода: userId={}, balance={}, amount={}", userId, balance, amount);
                return ResponseEntity.badRequest().body("Недостаточно средств на балансе");
            }

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", chatId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance
            ));
            kafkaTemplate.send("withdraw_request", message);
            log.info("Запрос на вывод отправлен в Kafka: userId={}, amount={}, currency={}", userId, amount, currency);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Ошибка обработки запроса на вывод: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}