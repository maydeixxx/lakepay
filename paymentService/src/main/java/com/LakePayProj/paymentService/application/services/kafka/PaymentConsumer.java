package com.LakePayProj.paymentService.application.services.kafka;

import com.LakePayProj.paymentService.application.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentConsumer {
    private final PaymentProducer producer;
    private final PaymentService service;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "withdraw_request", groupId = "MONEY")
    public void handleWithdrawRequest(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.valueOf(data.get("userId").toString());
            Long chatId = Long.valueOf(data.get("chatId").toString());
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            String currency = data.get("currency").toString();
            BigDecimal balance = new BigDecimal(data.get("balance").toString());

            log.info("Обработка запроса на вывод: userId={}, amount={}, currency={}", userId, amount, currency);

            boolean transferSuccess = service.transferFunds(chatId, amount, currency);

            if (!transferSuccess) {
                log.error("Вывод средств провалился: userId={}, amount={}, currency={}", userId, amount, currency);
                String errorMessage = objectMapper.writeValueAsString(Map.of(
                        "chatId", chatId,
                        "error", "Не удалось выполнить вывод средств. Попробуйте позже."
                ));
                producer.sendWithdrawFailed(errorMessage);
                return;
            }

            BigDecimal amountInUsd;
            BigDecimal rate = service.getExchangeCourse(currency, "USD");
            amountInUsd = amount.multiply(rate);

            service.updateUserBalance(userId, amountInUsd, "withdraw", null);
            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", chatId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance.subtract(amountInUsd)
            ));
            producer.sendWithdrawConfirmed(message);
            log.info("Вывод подтверждён: userId={}, amount={}, currency={}", userId, amount, currency);

        } catch (Exception e) {
            log.error("Ошибка обработки withdraw_request: {}", e.getMessage(), e);
        }
    }
}
