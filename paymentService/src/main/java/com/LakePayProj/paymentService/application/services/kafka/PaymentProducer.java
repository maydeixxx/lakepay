package com.LakePayProj.paymentService.application.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProducer {
    private final KafkaTemplate<String, String> template;

    public void sendWithdrawFailed(String message) {
        try {
            template.send("withdraw_failed", message);
            log.info("Отправлено сообщение в withdraw_failed: {}", message);
        } catch (Exception e) {
            log.error("Ошибка при отправке в withdraw_failed: {}", e.getMessage());
        }
    }

    public void sendWithdrawConfirmed(String message) {
        try {
            template.send("withdraw_confirmed", message);
            log.info("Отправлено сообщение в withdraw_confirmed: {}", message);
        } catch (Exception e) {
            log.error("Ошибка при отправке в withdraw_confirmed: {}", e.getMessage());
        }
    }

    public void sendPaymentCreated(String message) {
        try {
            template.send("payment_created", message);
            log.info("Отправлено сообщение в payment_created: {}", message);
        } catch (Exception e) {
            log.error("Ошибка при отправке в payment_created: {}", e.getMessage());
        }
    }

    public void sendAdData(String message) {
        try {
            template.send("ad_data", message);
            log.info("Отправлено сообщение в ad_data: {}", message);
        } catch (Exception e) {
            log.error("Ошибка при отправке в ad_data: {}", e.getMessage());
        }
    }

    public void getAdData(String id, String action) {
        try {
            if (action.startsWith("sellerId")) {
                template.send("get_ad_data_request", 0, id, id);
                log.info("Отправлено сообщение в get_ad_data_request. Part = 0. adId = {}", id);
            } else if (action.startsWith("price_credentials")) {
                template.send("get_ad_data_request", 1, id, id);
                log.info("Отправлено сообщение в get_ad_data_request. Part = 1. adId = {}", id);
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке в get_ad_data для adId={}: {}", id, e.getMessage());
        }
    }

    public void getUserDataById(String id) {
        try {
            template.send("get_user_data_by_id_request", id, id);
            log.info("Отправлен запрос в get_user_data_by_id для userId={}", id);
        } catch (Exception e) {
            log.error("Ошибка при отправке в get_user_data_by_id для userId={}: {}", id, e.getMessage());
        }
    }

    public void updateUserData(Long id, BigDecimal newBalance) {
        try {
            template.send("update_user_data", id.toString(), newBalance.toString());
            log.info("Отправлен запрос в update_user_data для userId={}", id);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения в топик update_user_data: {}", e.getMessage());
        }
    }

    public void updateAdData(Long id) {
        try {
            template.send("update_ad_data", id.toString(), id.toString());
            log.info("Отправлен запрос в update_ad_data для adId={}", id);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения в топик update_ad_data: {}", e.getMessage());
        }
    }
}