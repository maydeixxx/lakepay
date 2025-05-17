package com.LakePayProj.paymentService.application.kafka;

import com.LakePayProj.paymentService.application.services.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;
    private final PaymentService paymentService;

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

            String payUrl = paymentService.createInvoice(amount, currency, "Deposit for user " + userId);
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
    public void handleWithdrawRequest(ConsumerRecord<String, String> record){
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.valueOf(data.get("userId").toString());
            Long chatId = Long.valueOf(data.get("chatId").toString());
            Double amount = Double.valueOf(data.get("amount").toString());
            String currency = data.get("currency").toString();
            Double balance = Double.valueOf(data.get("balance").toString());

            boolean transferSuccess = paymentService.transferFunds(userId, amount, currency);

            if (!transferSuccess){
                log.error("Вывод средств провалился userId={}, amount={}, currency={}", userId, amount, currency);
            }

            paymentService.updateUserBalance(userId, amount, "withdraw");

            String message = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "chatId", chatId,
                    "amount", amount,
                    "currency", currency,
                    "balance", balance
            ));

            template.send("withdraw_confirmed", message);

        }catch (Exception e) {
            log.error("Ошибка обработки withdraw_request: {}", e.getMessage(), e);
        }

    }


}
