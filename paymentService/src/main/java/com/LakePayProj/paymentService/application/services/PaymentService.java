package com.LakePayProj.paymentService.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${crypto-bot.api-url}")
    private String apiUrl;
    @Value("${crypto-bot.api-token}")
    private String apiToken;

    public String createInvoice(Long userId, Long adId, String currency, Double amount) {
        String url = apiUrl + "createInvoice";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Crypto-Pay-API-Token", apiToken);
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = Map.of(
                "amount", amount,
                "currency", currency,
                "description", "Payment for ad #" + adId
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String response = restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return (String) ((Map<?, ?>) responseMap.get("result")).get("pay_url");
        } catch (Exception e) {
            log.error("Ошибка создания счета: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать счет");
        }
    }
}