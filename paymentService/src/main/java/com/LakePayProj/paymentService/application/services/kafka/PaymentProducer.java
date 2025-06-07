package com.LakePayProj.paymentService.application.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

    public void getAdData(String id) {
        try {
            template.send("get_ad_data_request", id, id);
            log.info("Отправлен запрос в get_ad_data для adId={}", id);
        } catch (Exception e) {
            log.error("Ошибка при отправке в get_ad_data для adId={}: {}", id, e.getMessage());
        }
    }

    public void getUserDataById(String id) {
        try {
            template.send("get_user_data_by_id", id, id);
            log.info("Отправлен запрос в get_user_data_by_id для userId={}", id);
        } catch (Exception e) {
            log.error("Ошибка при отправке в get_user_data_by_id для userId={}: {}", id, e.getMessage());
        }
    }
}