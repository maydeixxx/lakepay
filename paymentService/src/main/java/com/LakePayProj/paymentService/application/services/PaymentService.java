package com.LakePayProj.paymentService.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${crypto-bot.api}")
    private String apiUrl;

    @Value("${crypto-bot.token}")
    private String apiToken;

    public Map<String, String> createInvoice(Long userId, Long adId, String asset, Double amount) {
        try {
            String payload = userId + ":" + adId;
            String url = apiUrl + "/createInvoice";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Crypto-Pay-API-Token", apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "asset", asset.toUpperCase(),
                    "amount", amount,
                    "payload", payload,
                    "description", "Оплата объявления #" + adId
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                if ((Boolean) responseBody.get("ok")) {
                    Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
                    String payUrl = (String) result.get("pay_url");
                    Integer invoiceId = (Integer) result.get("invoice_id");
                    log.info("Счёт создан: invoiceId={}, payUrl={}", invoiceId, payUrl);
                    return Map.of("payUrl", payUrl, "invoiceId", String.valueOf(invoiceId));
                } else {
                    Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                    throw new RuntimeException("Ошибка API Crypto Bot: " + error.get("name"));
                }
            } else {
                throw new RuntimeException("Ошибка HTTP: " + response.getStatusCode() + " " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Не удалось создать счёт: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать счёт: " + e.getMessage(), e);
        }
    }
}