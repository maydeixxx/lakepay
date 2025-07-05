package com.LakePayProj.paymentService.services;

import com.LakePayProj.paymentService.exceptions.*;
import com.LakePayProj.paymentService.models.DTOs.AdUpdateDto;
import com.LakePayProj.paymentService.models.redis.AdRedis;
import com.LakePayProj.paymentService.models.redis.UserRedis;
import com.LakePayProj.paymentService.repos.PaymentRepository;
import com.LakePayProj.paymentService.services.kafka.PaymentProducer;
import com.LakePayProj.paymentService.models.entity.PaymentEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final PaymentRepository paymentRepository;
    private final UserServiceRedis userServiceRedis;
    private final AdServiceRedis adServiceRedis;

    @Value("${crypto-bot.api}")
    private String apiUrl;

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
            Map<String, Object> responseData = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
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
            producer.getUserDataById(userId);
            Thread.sleep(2000);
            UserRedis user = userServiceRedis.getById(userId);
            if (user == null) {
                log.error("Не удалось получить данные пользователя для userId={}", userId);
                throw new UserNotFoundException(String.format("user by id %s not found", userId));
            }
            log.info("Получены данные пользователя: {}", user);

            Long buyTgId = user.getTgId();
            BigDecimal balance = user.getBalance();

            producer.getAdData(adId.toString(), "price_credentials");
            Thread.sleep(2000);
            AdRedis ad = adServiceRedis.getAdById(adId).orElseThrow(
                    () -> new AdNotFoundException(String.format("ad by id {%s} not found", adId))
            );
            if (ad == null) {
                log.error("Данные не получены!!");
                throw new AdNotFoundException(String.format("Ad by id %s noty found", adId));
            }
            BigDecimal price = ad.getPrice();
            String login = ad.getLogin();
            String password = ad.getPassword();
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

            producer.sendAdData(message);
            updateAdStatus(adId);
            updateUserBalance(userId, price, "buy", "default");
            log.info("Покупка завершена: userId={}, adId={}, message={}", userId, adId, message);
        } catch (Exception e) {
            throw new PayAdException(String.format("error while paying ad %s", adId), e);
        }
    }


    public UserRedis getUserDataFromCache(Long userId) {
        return userServiceRedis.getById(userId);
    }

    public Long getSellerIdConsume(Long adId) {
        try {
            Long sellerId = adServiceRedis.getAdById(adId).orElseThrow().getSellerId();
            if (sellerId != null) {
                log.info("sellerId={} найден в кэше для adId={}", sellerId, adId);
                return sellerId;
            }

            producer.getAdData(adId.toString(), "sellerId");
            Thread.sleep(2000);
            sellerId = adServiceRedis.getAdById(adId).orElseThrow().getSellerId();
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

    @KafkaListener(topics = "get_user_data_by_id_response", groupId = "userData")
    public void getUserData(ConsumerRecord<String, String> record) {
        try {
            log.info("Получено сообщение в responseToUserData: key={}, value={}", record.key(), record.value());
            Map<String, Object> userData = objectMapper.readValue(record.value(), new TypeReference<>() {});
            Long userId = Long.parseLong(record.key());
            Long tgId = Long.parseLong(userData.get("tgId").toString());
            BigDecimal balance = new BigDecimal(userData.get("balance").toString());
            UserRedis user = UserRedis.builder()
                    .id(userId)
                    .tgId(tgId)
                    .balance(balance)
                    .build();
            userServiceRedis.saveUser(user);
            log.info("Обновлён кэш userData для userId={}: {}", userId, userData);
        } catch (Exception e) {
            log.error("Ошибка обработки responseToUserData: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "get_ad_data_response", partitions = {"0, 1"}), groupId = "AD_MONEY")
    public void getSellerIdConsume(ConsumerRecord<String, String> record) {
        if (record.partition() == 0) {
            try {
                log.info("Получено сообщение в get_ad_data_response partition=0: key={}, value={}", record.key(), record.value());
                Map<String, Object> data = objectMapper.readValue(record.value(), new TypeReference<>() {});
                Long adId = Long.parseLong(data.get("adId").toString());
                Long sellerId = Long.parseLong(data.get("sellerId").toString());
                if (adServiceRedis.getAdById(adId).isEmpty()) {
                    adServiceRedis.saveAd(AdRedis.builder()
                            .id(adId)
                            .sellerId(sellerId)
                            .build()
                    );
                } else {
                    AdUpdateDto updates = AdUpdateDto.builder()
                            .sellerId(sellerId)
                            .build();
                    adServiceRedis.updateCacheAd(adId, updates, "sellerId");
                }
            } catch (Exception e) {
                log.error("Ошибка обработки get_ad_data: {}", e.getMessage());
            }
        } else if (record.partition() == 1) {
            try {
                log.info("Получено сообщение в get_ad_data_response partition=1: key={}, value={}", record.key(), record.value());
                Map<String, Object> adData = objectMapper.readValue(record.value(), new TypeReference<>() {});

                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(adData.get("price").toString()));
                Long adId = Long.parseLong(record.key());
                String login = adData.get("login").toString();
                String password = adData.get("password").toString();
                AdRedis ad = AdRedis.builder()
                        .id(adId)
                        .login(login)
                        .password(password)
                        .price(price)
                        .build();
                adServiceRedis.saveAd(ad);

                log.info("Полученные данные = {}", adData);
                log.info("Кэшированные данные = {}", ad);
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
            Map<String, Object> responseData = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

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

            Map<String, Object> data = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
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
        BigDecimal newBalance;
        try {
            producer.getUserDataById(userId);
            Thread.sleep(2000);
            UserRedis user = getUserDataFromCache(userId);
            if (user == null) {
                log.error("Не удалось получить данные пользователя userId={}", userId);
                return null;
            }
            log.info("Получены данные для пользователя: id = {}", user);

            BigDecimal balance = user.getBalance();

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
                case "buy", "withdraw" -> newBalance = balance.subtract(amount);
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
