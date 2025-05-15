package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.application.interfaces.services.IPaymentService;
import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;

    @Value("${crypto-bot.api}")
    private String apiUrl;

    @Value("${crypto-bot.token}")
    private String apiToken;

    @Override
    public String createInvoice(Double amount, String asset, String description) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Crypto-Pay-API-Token", apiToken);
            Map<String, Object> request = Map.of(
                    "amount", amount,
                    "asset", asset,
                    "description", description
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/createInvoice",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            Map<String, Object> responseData = objectMapper.readValue(response.getBody(), Map.class);
            if (!(Boolean) responseData.getOrDefault("ok", false)) {
                log.error("Crypto Bot API error: {}", responseData.get("error"));
                return null;
            }
            Map<String, Object> result = (Map<String, Object>) responseData.get("result");
            String payUrl = (String) result.get("pay_url");
            if (payUrl == null) {
                log.error("No pay_url in Crypto Bot response: {}", result);
                return null;
            }
            return payUrl;
        } catch (Exception e) {
            log.error("Ошибка создания счёта в Crypto Bot: {}", e.getMessage(), e);
            return null;
        }
    }

    @KafkaListener(topics = "deposit_request", groupId = "MONEY")
    public void handleDepositRequest(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.valueOf(data.get("userId").toString());
            String currency = data.get("currency").toString();
            Double amount = Double.valueOf(data.get("amount").toString());

            if (currency == null || currency.isEmpty()) {
                log.error("Invalid currency in deposit_request: userId={}", userId);
                return;
            }

            String payUrl = createInvoice(amount, currency, "Deposit for user " + userId);
            if (payUrl == null) {
                log.error("Не удалось создать счёт для пополнения: userId={}, amount={}, currency={}", userId, amount, currency);
                return;
            }
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "payUrl", payUrl,
                    "amount", amount,
                    "currency", currency
            ));
            template.send("payment_created", message);
        } catch (Exception e) {
            log.error("Ошибка обработки = {} ", e.getMessage());
        }
    }
}