package com.LakePayProj.paymentService.services;

import com.LakePayProj.paymentService.exceptions.*;
import com.LakePayProj.paymentService.repos.PaymentRepository;
import com.LakePayProj.paymentService.services.kafka.PaymentProducer;
import com.LakePayProj.paymentService.entity.PaymentEntity;
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
    private final ConcurrentHashMap<Long, Long> sellerIdCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Map<String, Object>> cacheUserData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Map<String, Object>> cachedAdDataForSell = new ConcurrentHashMap<>();

    @Value("${crypto-bot.api}")
    private String apiUrl;

    @Value("${crypto-bot.token}")
    private String apiToken;

    @Override
    public String createInvoice(BigDecimal amount, String asset, String description) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Crypto-Pay-API-Token", "36875:AAS1Wn7fEsFVdceQiM0buxOkHHCra0cOrnE");
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
            throw new CreateInvoiceException("Failed to create new invoice :(", e);
        }
    }

    @Transactional
    @Override
    public void buyAd(Long userId, Long adId) {
        try {
            log.warn("Данные пользователя не найдены для userId={}. Запрос данных...", userId);
            producer.getUserDataById(userId.toString());
            Thread.sleep(2000);
            Map<String, Object> userData = cacheUserData.get(userId);
            if (userData == null) {
                log.error("Не удалось получить данные пользователя для userId={}", userId);
                return;
            }
            log.info("Получены данные пользователя: {}", userData);

            Long buyTgId = Long.valueOf(userData.get("tgId").toString());
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());

            cacheUserData.clear();
            producer.getAdData(adId.toString(), "price_credentials");
            Thread.sleep(2000);
            Map<String, Object> adData = cachedAdDataForSell.get(adId);
            if (adData.isEmpty()) {
                log.error("Данные не получены!!");
                return;
            }
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(adData.get("price").toString()));
            String login = adData.get("login").toString();
            String password = adData.get("password").toString();
            log.info("Данные: price = {}, login = {}, password = {}", price, login, password);
            Long sellerId = getSellerIdConsume(adId);

            if (sellerId == null) {
                log.error("sellerId для adId={} не получен", adId);
                return;
            }

            if (balance.compareTo(price) < 0) {
                log.warn("Недостаточно средств: userId={}, balance={}, price={}", userId, balance, price);
                return;
            }
            updateUserBalance(sellerId, price, "sellerUpdate", "TRX");

            String message = objectMapper.writeValueAsString(Map.of(
                    "tgId", buyTgId,
                    "adId", adId,
                    "password", password,
                    "login", login,
                    "sellerId", sellerId
            ));
            sellerIdCache.clear();

            producer.sendAdData(message);
            updateAdStatus(adId);
            updateUserBalance(userId, price, "buy", "default");
            log.info("Покупка завершена: userId={}, adId={}, message={}", userId, adId, message);
        } catch (Exception e) {
            throw new PayAdException(String.format("error while paying ad %s", adId), e);
        }
    }

    @KafkaListener(topics = "get_user_data_by_id_response", groupId = "userData")
    public void getUserData(ConsumerRecord<String, String> record) {
        try {
            log.info("Получено сообщение в responseToUserData: key={}, value={}", record.key(), record.value());
            Map<String, Object> userData = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.parseLong(record.key());
            cacheUserData.put(userId, userData);
            log.info("Обновлён кэш userData для userId={}: {}", userId, userData);
        } catch (Exception e) {
            log.error("Ошибка обработки responseToUserData: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserDataFromCache(Long userId) {
        return cacheUserData.get(userId);
    }

    public Long getSellerIdConsume(Long adId) {
        try {
            Long sellerId = sellerIdCache.get(adId);
            if (sellerId != null) {
                log.info("sellerId={} найден в кэше для adId={}", sellerId, adId);
                return sellerId;
            }

            producer.getAdData(adId.toString(), "sellerId");
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

    @KafkaListener(topics = "get_ad_data_response", groupId = "AD_MONEY")
    public void getSellerIdConsume(ConsumerRecord<String, String> record) {
        if (record.partition() == 0) {
            try {
                log.info("Получено сообщение в get_ad_data_response partition=0: key={}, value={}", record.key(), record.value());
                Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
                Long adId = Long.parseLong(data.get("adId").toString());
                Long sellerId = Long.parseLong(data.get("sellerId").toString());
                sellerIdCache.put(adId, sellerId);
            } catch (Exception e) {
                log.error("Ошибка обработки get_ad_data: {}", e.getMessage());
            }
        } else if (record.partition() == 1) {
            try {
                log.info("Получено сообщение в get_ad_data_response partition=1: key={}, value={}", record.key(), record.value());
                Long adId = Long.parseLong(record.key());
                Map<String, Object> adData = objectMapper.readValue(record.value(), Map.class);
                log.info("Полученные данные = {}", adData);
                cachedAdDataForSell.put(adId, adData);
                log.info("Кэшированные данные = {}", cachedAdDataForSell);
            } catch (Exception e) {
                log.error("Error = {}", e.getMessage());
            }
        }
    }

    public boolean transferFunds(Long userId, BigDecimal amount, String currency) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Crypto-Pay-API-Token", "36875:AAS1Wn7fEsFVdceQiM0buxOkHHCra0cOrnE");
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
            throw new TransferFundsException(String.format("error while transferring funds to user [%s]", userId), e);
        }
    }

    @Override
    public BigDecimal getExchangeCourse(String sourceAsset, String targetAsset) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Crypto-Pay-Api-Token", "36875:AAS1Wn7fEsFVdceQiM0buxOkHHCra0cOrnE");
            HttpEntity<String> request = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/getExchangeRates",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            Map<String, Object> data = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> rates = (List<Map<String, Object>>) data.get("result");
            Optional<Map<String, Object>> rateData = rates.stream()
                    .filter(r -> sourceAsset.equals(r.get("source")) && targetAsset.equals(r.get("target")))
                    .findFirst();

            if (rateData.isEmpty()) {
                log.error("Курс для {} -> {} не найден", sourceAsset, targetAsset);
                throw new RuntimeException("Курс для " + sourceAsset + " -> " + targetAsset + " не найден");
            }

            return new BigDecimal(rateData.get().get("rate").toString());
        } catch (Exception e) {
            throw new GetCourseException(String.format("error while getting course for %s -> %s", sourceAsset, targetAsset), e);
        }
    }

    @Transactional
    public BigDecimal updateUserBalance(Long userId, BigDecimal amount, String operation, String asset) {
        BigDecimal newBalance = null;
        try {
            producer.getUserDataById(userId.toString());
            Thread.sleep(2000);
            Map<String, Object> data = getUserDataFromCache(userId);
            if (data == null) {
                log.error("Не удалось получить данные пользователя userId={}", userId);
                return null;
            }
            log.info("Получены данные для пользователя: id = {}", data);

            BigDecimal balance = new BigDecimal(data.get("balance").toString());

            if ("deposit".equals(operation)) {
                PaymentEntity lastPayment = paymentRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
                if (lastPayment != null && lastPayment.getAmount().compareTo(amount) == 0 &&
                        "COMPLETED".equals(lastPayment.getStatus())) {
                    log.info("Платёж уже обработан: userId={}, amount={}", userId, amount);
                    return balance;
                }
            }

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
                case "sellerUpdate" -> newBalance = balance.add(amount);
                default -> {
                    log.error("Недопустимая операция: userId={}, operation={}", userId, operation);
                    return null;
                }
            }
            producer.updateUserData(userId, newBalance);
            Thread.sleep(1000);
            log.info("Баланс обновлён: userId={}, operation={}, amount={}, newBalance={}", userId, operation, amount, newBalance);
        } catch (Exception e) {
            throw new UpdateBalanceException(String.format("error while updating user [%s] balance", userId), e);
        }
        return newBalance;
    }

    public void updateAdStatus(Long adId) {
        try {
            producer.updateAdData(adId);
            Thread.sleep(1000);
            log.info("Изменен статус объявления = {}", true);
        } catch (Exception e) {
            throw new UpdateAdStatusException(String.format("error while updating ad [%s] status", adId), e);
        }
    }
}