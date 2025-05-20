package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.application.interfaces.services.IPaymentService;
import com.LakePayProj.paymentService.application.services.kafka.PaymentProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentProducer producer;
    private final String lakePayUrl = "https://lakepay.ru";

    @Value("${crypto-bot.api}")
    private String apiUrl;

    @Value("${crypto-bot.token}")
    private String apiToken;

    @Override
    public String createInvoice(BigDecimal amount, String asset, String description) {
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

    @Override
    public void buyAd(Long userId, Long adId) {
        try {
            String userResponse = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> userData = objectMapper.readValue(userResponse, Map.class);
            Long tgId = Long.valueOf(userData.get("tgId").toString());
            log.info("TGID = {}", tgId);
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());
            log.info("BALANCE = {}", balance);

            String adResponse = restTemplate.getForObject(lakePayUrl + "/ad_id/" + adId, String.class);
            Map<String, Object> adData = objectMapper.readValue(adResponse, Map.class);
            String title = adData.get("title").toString();
            log.info("TITLE = {}", title);
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(adData.get("price").toString()));

            String credentialsResponse = restTemplate.getForObject(lakePayUrl + "/ad_credentials/" + adId, String.class);
            Map<String, String> credentials = objectMapper.readValue(credentialsResponse, Map.class);
            String login = credentials.get("login");
            String password = credentials.get("password");
            log.info("LOGIN = {}", login);
            log.info("PASSWORD = {}", password);

            String message = objectMapper.writeValueAsString(Map.of(
                    "tgId", tgId,
                    "adId", adId,
                    "password", password,
                    "login", login
            ));
            if (balance.compareTo(price) >= 0) {
                producer.sendAdData(message);
                updateAdStatus(adId);
                log.info("Отправлено сообщение в топик ad_data message = {}", message);
                updateUserBalance(userId, price, "buy", "default");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public boolean transferFunds(Long userId, BigDecimal amount, String currency) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Crypto-Pay-API-Token", apiToken);
            String spendId = "withdraw-" + userId + "-" + System.currentTimeMillis();
            Map<String, Object> request = Map.of(
                    "user_id", userId,
                    "asset", currency,
                    "amount", amount,
                    "spend_id", spendId
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/transfer",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            Map<String, Object> responseData = objectMapper.readValue(response.getBody(), Map.class);

            if (!(Boolean) responseData.getOrDefault("ok", false)) {
                log.error("Не получилось отправить средства: {}", responseData.get("error"));
                return false;
            }
            log.info("Деньги перевелись: userId={}, amount={}, currency={}, spendId={}", userId, amount, currency, spendId);
            return true;

        } catch (Exception e) {
            log.error("Ошибка перевода средств через Crypto Bot: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public BigDecimal getExchangeCourse(String sourceAsset, String targetAsset) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Crypto-Pay-Api-Token", apiToken);
            HttpEntity<String> request = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/getExchangeRates",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            Map<String, Object> data = objectMapper.readValue(response.getBody(), Map.class);

            List<Map<String, Object>> rates = (List<Map<String, Object>>) (data.get("result"));
            Optional<Map<String, Object>> rateData = rates.stream()
                    .filter(r -> sourceAsset.equals(r.get("source")) && targetAsset.equals(r.get("target")))
                    .findFirst();

            if (rateData.isEmpty()) {
                log.error("Курс для {} -> {} не найден", sourceAsset, targetAsset);
                throw new RuntimeException("Курс для " + sourceAsset + " -> " + targetAsset + " не найден");
            }

            return new BigDecimal(rateData.get().get("rate").toString());
        } catch (Exception e) {
            log.error("Ошибка получения курса валют: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка получения курса валют: " + e.getMessage());
        }
    }

    public void updateUserBalance(Long userId, BigDecimal amount, String operation, String asset) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            BigDecimal balance = new BigDecimal(data.get("balance").toString());

            BigDecimal newBalance;
            switch (operation) {
                case "deposit" -> {
                    BigDecimal amountInUsd = amount;
                    if (asset != null && !asset.isEmpty()) {
                        BigDecimal rate = getExchangeCourse(asset, "USD");
                        amountInUsd = amount.multiply(rate);
                    }
                    newBalance = balance.add(amountInUsd);
                }
                case "buy", "withdraw" -> {
                    newBalance = balance.subtract(amount);
                }
                default -> {
                    log.error("Недопустимая операция: userId={}, operation={}", userId, operation);
                    return;
                }
            }

            Map<String, Object> request = Map.of("balance", newBalance);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(
                    lakePayUrl + "/update_user/" + userId,
                    HttpMethod.PATCH,
                    entity,
                    String.class
            );
            log.info("Баланс обновлён: userId={}, operation={}, amount={}, newBalance={}", userId, operation, amount, newBalance);

        } catch (Exception e) {
            log.error("Ошибка обновления баланса: userId={}, operation={}, error={}", userId, operation, e.getMessage(), e);
        }
    }

    public void updateAdStatus(Long adId) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            Map<String, Object> request = Map.of("sold", true);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);
            restTemplate.exchange(
                    lakePayUrl + "/update_ad/" + adId,
                    HttpMethod.PATCH,
                    entity,
                    String.class
            );
            log.info("Изменен статус объявления = {}", true);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}