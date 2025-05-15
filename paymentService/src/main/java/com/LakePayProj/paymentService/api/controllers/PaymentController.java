package com.LakePayProj.paymentService.api.controllers;

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
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class PaymentController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String lakePayUrl = "https://lakepay.ru";

    @PostMapping()
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.debug("Получен webhook: payload={}", payload); // Добавлено
        try {
            String type = (String) payload.get("update_type");
            log.debug("Webhook update_type: {}", type); // Добавлено
            if (!"invoice_paid".equals(type)) {
                log.debug("Игнорируем webhook с update_type: {}", type);
                return ResponseEntity.ok().build();
            }

            Map<String, Object> invoice = (Map) payload.get("payload");
            String description = (String) invoice.get("description");
            Double amount = Double.valueOf(invoice.get("amount").toString());
            String currency = (String) invoice.get("asset");
            log.debug("Webhook invoice: description={}, amount={}, currency={}", description, amount, currency); // Добавлено

            if (description.startsWith("Payment for ad")) {
                Long adId = Long.parseLong(description.replace("Payment for ad ", ""));
                String message = objectMapper.writeValueAsString(Map.of("adId", adId));
                kafkaTemplate.send("payment_confirmed", message);
                log.info("Оплата подтверждена: adId={}", adId);
            } else if (description.startsWith("Deposit for user")) {
                Long userId = Long.parseLong(description.replace("Deposit for user ", ""));
                String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
                Map<String, Object> data = objectMapper.readValue(response, Map.class);
                Long chatId = Long.valueOf(data.get("chatId").toString());
                log.info("chatId = {}", chatId);
                updateUserBalance(userId, amount, "deposit");
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

    private void updateUserBalance(Long userId, Double amount, String operation) {


        try {
            HttpHeaders headers = new HttpHeaders();
            String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            Double balance = Double.valueOf(data.get("balance").toString());
            Double newBalance = balance + amount;
            Map<String, Object> request = Map.of("balance", newBalance);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(
                    lakePayUrl + "/update_user/" + userId,
                    HttpMethod.PATCH,
                    entity,
                    String.class
            );
            log.info("Баланс обновлён: userId={}, operation={}, amount={}", userId, operation, amount);
        } catch (Exception e) {
            log.error("Ошибка обновления баланса: userId={}, operation={}, error={}", userId, operation, e.getMessage(), e);
        }
    }
}