package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.application.interfaces.repos.PaymentRepository;
import com.LakePayProj.paymentService.application.interfaces.services.IPaymentService;
import com.LakePayProj.paymentService.application.services.kafka.PaymentProducer;
import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentProducer producer;
    private final PaymentRepository paymentRepository;
    private final String lakePayUrl = "https://lakepay.ru";
    private final ConcurrentHashMap<Long, Long> sellerIdCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> cacheUserData = new ConcurrentHashMap<>();

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
            String invoiceId = result.get("invoice_id").toString();
            if (payUrl == null) {
                log.error("No pay_url in Crypto Bot response: {}", result);
                return null;
            }
            PaymentEntity payment = new PaymentEntity();
            payment.setUserId(Long.parseLong(description.replace("Deposit for user ", "")));
            payment.setCreatedAt(LocalDateTime.now().toString());
            payment.setAmount(amount);
            payment.setInvoiceId(invoiceId);
            payment.setCurrency(asset);
            payment.setStatus("PENDING");
            paymentRepository.save(payment);

            return payUrl;
        } catch (Exception e) {
            log.error("Ошибка создания счёта в Crypto Bot: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    @Override
    public void buyAd(Long userId, Long adId) {
        try {
            // User data
            producer.getUserDataById(userId.toString());
            Thread.sleep(2000);
            if (cacheUserData.isEmpty()) {
                log.error("User data not recorded");
                return;
            }
            Long buyTgId = Long.valueOf(cacheUserData.get("tgId").toString());
            log.info("TGID = {}", buyTgId);
            BigDecimal balance = new BigDecimal(cacheUserData.get("balance").toString());
            log.info("BALANCE = {}", balance);

            // Ad data
            String adResponse = restTemplate.getForObject(lakePayUrl + "/ad_id/" + adId, String.class);
            Map<String, Object> adData = objectMapper.readValue(adResponse, Map.class);
            String title = adData.get("title").toString();
            log.info("TITLE = {}", title);
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(adData.get("price").toString()));

            // Credentials
            String credentialsResponse = restTemplate.getForObject(lakePayUrl + "/ad_credentials/" + adId, String.class);
            Map<String, String> credentials = objectMapper.readValue(credentialsResponse, Map.class);
            String login = credentials.get("login");
            String password = credentials.get("password");
            log.info("LOGIN = {}", login);
            log.info("PASSWORD = {}", password);

            // Get sellerId
            Long sellerId = getSellerIdConsume(adId);
            if (sellerId == null) {
                log.error("sellerId для adId={} не получен", adId);
                return;
            }
            log.info("sellerId = {}", sellerId);

            if (balance.compareTo(price) < 0) {
                log.warn("Недостаточно средств: userId={}, balance={}, price={}", userId, balance, price);
                return;
            }


            BigDecimal priceToTrx = price.divide(getExchangeCourse("TRX", "USD"), 8, RoundingMode.HALF_UP);
            boolean transferSuccess = transferFunds(sellerId, priceToTrx, "TRX");
            if (!transferSuccess) {
                log.error("Не удалось перевести средства продавцу: sellerId={}, amount={}", sellerId, price);
                return;
            }

            String message = objectMapper.writeValueAsString(Map.of(
                    "tgId", buyTgId,
                    "adId", adId,
                    "password", password,
                    "login", login,
                    "sellerId", sellerId
            ));

            producer.sendAdData(message);
            updateAdStatus(adId);
            BigDecimal newBalance = updateUserBalance(userId, price, "buy", "default");
            log.info("Покупка завершена: userId={}, adId={}, message={}", userId, adId, message);
        } catch (Exception e) {
            log.error("Ошибка покупки объявления: userId={}, adId={}, error={}", userId, adId, e.getMessage(), e);
        }
    }

    public Long getSellerIdConsume(Long adId) {
        try {
            Long sellerId = sellerIdCache.get(adId);
            if (sellerId != null) {
                log.info("sellerId={} найден в кэше для adId={}", sellerId, adId);
                return sellerId;
            }

            producer.getAdData(adId.toString());
            log.info("Отправлен запрос для sellerId, adId={}", adId);

            Thread.sleep(2000);
            sellerId = sellerIdCache.get(adId);
            if (sellerId != null) {
                log.info("sellerId={} получен для adId={}", sellerId, adId);
                return sellerId;
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @KafkaListener(topics = "get_ad_data", groupId = "AD_MONEY")
    public void getSellerIdConsume(ConsumerRecord<String, String> record) {
        try {
            if (record.partition() == 1) {
                log.info("Получено сообщение в get_ad_data partition=1: key={}, value={}", record.key(), record.value());
                Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
                Long adId = Long.parseLong(data.get("adId").toString());
                Long sellerId = Long.parseLong(data.get("sellerId").toString());
                sellerIdCache.put(adId, sellerId);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "responseToUserData", groupId = "userData")
    public void getUserData(ConsumerRecord<String, String> record) {
        try {
            log.info("New message = {}, {}", record.key(), record.value());
            Map<String, Object> userData = objectMapper.readValue(record.value(), Map.class);
            Long tgId = Long.parseLong(userData.get("tgId").toString());
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());
            cacheUserData.put("tgId", tgId);
            cacheUserData.put("balance", balance);
            log.info("Recorded USER DATA: tgId = {}, balance = {}", tgId, balance);
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

    @Transactional
    public BigDecimal updateUserBalance(Long userId, BigDecimal amount, String operation, String asset) {
        BigDecimal newBalance = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> data = objectMapper.readValue(response, Map.class);
            BigDecimal balance = new BigDecimal(data.get("balance").toString());

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
                    return null;
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
        return newBalance;
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