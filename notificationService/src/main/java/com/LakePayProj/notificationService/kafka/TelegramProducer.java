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

    public void availableAds(Long tgId) {
        template.send("availableAds", String.valueOf(tgId));
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
}