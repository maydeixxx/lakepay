package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.application.interfaces.services.IPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;
    private final String lakePayUrl = "https://lakepay.ru";

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

    @Override
    public void buyAd(Long userId, Long adId) {
        try {
            String userResponse = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> userData = objectMapper.readValue(userResponse, Map.class);
            Long tgId = Long.valueOf(userData.get("tgId").toString());
            log.info("TGID = {}", tgId);
            Double balance = Double.valueOf(userData.get("balance").toString());
            log.info("BALANCE = {}", balance);

            String adResponse = restTemplate.getForObject(lakePayUrl + "/ad_id/" + adId, String.class);
            Map<String, Object> adData = objectMapper.readValue(adResponse, Map.class);
            String title = adData.get("title").toString();
            log.info("TITLE = {}", title);
            Double price = Double.valueOf(adData.get("price").toString());

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
            if (balance >= price) {
                template.send("ad_data", message);
                updateAdStatus(adId);
                log.info("Отправлено сообщение в топик ad_data message = {}", message);
                updateUserBalance(userId, price, "buy", "default");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public boolean transferFunds(Long userId, Double amount, String currency) {
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

    public void updateUserBalance(Long userId, Double amount, String operation, String asset) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            Double balance = Double.valueOf(data.get("balance").toString());

            Double newBalance;
            switch (operation) {
                case "deposit" -> {
                    Double amountInUsd = amount;
                    if (asset != null && !asset.isEmpty()) {
                        switch (asset) {
                            case "TRX" -> amountInUsd = amount * 0.27;
                            case "ETH" -> amountInUsd = amount * 2551.29;
                            case "BTC" -> amountInUsd = amount * 102521.46;
                            default -> {
                                log.error("Неподдерживаемая валюта: userId={}, asset={}", userId, asset);
                                return;
                            }
                        }
                    }
                    newBalance = balance + amountInUsd;
                }
                case "buy", "withdraw" -> {
                    newBalance = balance - amount;
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

    @KafkaListener(topics = "withdraw_request", groupId = "MONEY")
    public void handleWithdrawRequest(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.valueOf(data.get("userId").toString());
            Long chatId = Long.valueOf(data.get("chatId").toString());
            Double amount = Double.valueOf(data.get("amount").toString());
            String currency = data.get("currency").toString();
            Double balance = Double.valueOf(data.get("balance").toString());

            log.info("Обработка запроса на вывод: userId={}, amount={}, currency={}", userId, amount, currency);

            boolean transferSuccess = transferFunds(chatId, amount, currency);

            if (!transferSuccess) {
                log.error("Вывод средств провалился: userId={}, amount={}, currency={}", userId, amount, currency);
                String errorMessage = objectMapper.writeValueAsString(Map.of(
                        "chatId", chatId,
                        "error", "Не удалось выполнить вывод средств. Попробуйте позже."
                ));
                template.send("withdraw_failed", errorMessage);
                return;
            }

            Double amountInUsd;
            switch (currency) {
                case "TRX" -> amountInUsd = amount * 0.27;
                case "ETH" -> amountInUsd = amount * 2551.29;
                case "BTC" -> amountInUsd = amount * 102521.46;
                default -> {
                    log.error("Неподдерживаемая валюта: {}", currency);
                    return;
                }
            }

            updateUserBalance(userId, amountInUsd, "withdraw", null);
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", chatId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance - amountInUsd
            ));
            template.send("withdraw_confirmed", message);
            log.info("Вывод подтверждён: userId={}, amount={}, currency={}", userId, amount, currency);

        } catch (Exception e) {
            log.error("Ошибка обработки withdraw_request: {}", e.getMessage(), e);
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