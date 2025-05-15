package com.LakePayProj.notificationService.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramProducer {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    public void sendTgAndChatId(Long tgId, Long chatId) {
        template.send("userTgChatId", String.valueOf(tgId), String.valueOf(chatId));
    }

    public void sendCategoryToSubscribe(Long tgId, String category) {
        template.send("ads-sub", 0, String.valueOf(tgId), category);
    }

    public void sendToUNSUB(Long tgId, String category) {
        template.send("ads-sub", 1, String.valueOf(tgId), category);
    }

    public void availableAds(Long tgId) {
        template.send("availableAds", String.valueOf(tgId));
    }

    public void sendPaymentRequest(Long userId, Long adId, String asset, Double amount) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "adId", adId,
                    "asset", asset,
                    "amount", amount
            ));
            template.send("payment_request", String.valueOf(userId), message);
            log.info("Отправлен запрос на платёж: userId={}, adId={}, asset={}, amount={}",
                    userId, adId, asset, amount);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации данных платежа: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить запрос на платёж", e);
        }
    }

    public void sendDepositRequest(Long userId, Double amount, String currency) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "amount", amount,
                    "currency", currency
            ));
            template.send("deposit_request", message);
            log.info("Отправлен запрос на пополнение счёта: userid = {}, amount = {}, currency = {}", userId, amount, currency);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void sendWithdrawRequest(Long userId, Double amount, String currency) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "amount", amount,
                    "currency", currency
            ));
            template.send("withdraw_request", message);
            log.info("Отправлен запрос на вывод средств: userid = {}, amount = {}, currency = {}", userId, amount, currency);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}