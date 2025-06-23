package com.LakePayProj.paymentService.services.kafka;

import com.LakePayProj.paymentService.exceptions.KafkaException;
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
            log.info("Sent message to withdraw_failed: {}", message);
        } catch (Exception e) {
            throw new KafkaException("error while sending message withdraw_failed", e);
        }
    }

    public void sendWithdrawConfirmed(String message) {
        try {
            template.send("withdraw_confirmed", message);
            log.info("Sent message to withdraw_confirmed: {}", message);
        } catch (Exception e) {
            throw new KafkaException("error while sending message to withdraw_confirmed", e);
        }
    }

    public void sendPaymentCreated(String message) {
        try {
            template.send("payment_created", message);
            log.info("Sent message to payment_created: {}", message);
        } catch (Exception e) {
            throw new KafkaException("error while sending message payment_created", e);
        }
    }

    public void sendAdData(String message) {
        try {
            template.send("ad_data", message);
            log.info("Sent message to ad_data: {}", message);
        } catch (Exception e) {
            throw new KafkaException("error while sending message ad_data", e);
        }
    }

    public void getAdData(String id, String action) {
        try {
            if (action.startsWith("sellerId")) {
                template.send("get_ad_data_request", 0, id, id);
                log.info("Sent message to get_ad_data_request. Part = 0. adId = {}", id);
            } else if (action.startsWith("price_credentials")) {
                template.send("get_ad_data_request", 1, id, id);
                log.info("error while sending message to get_ad_data_request. Part = 1. adId = {}", id);
            }
        } catch (Exception e) {
            throw new KafkaException("error while sending message get_ad_data_request", e);
        }
    }

    public void getUserDataById(String id) {
        try {
            template.send("get_user_data_by_id_request", id, id);
            log.info("Sent message to get_user_data_by_id for userId={}", id);
        } catch (Exception e) {
            throw new KafkaException("error while sending message get_user_data_by_id_request", e);
        }
    }

    public void updateUserData(Long id, BigDecimal newBalance) {
        try {
            template.send("update_user_data", id.toString(), newBalance.toString());
            log.info("Sent message to update_user_data for userId={}", id);
        } catch (Exception e) {
            throw new KafkaException("error while sending message update_user_data", e);
        }
    }

    public void updateAdData(Long id) {
        try {
            template.send("update_ad_data", id.toString(), id.toString());
            log.info("Sent message to update_ad_data for adId={}", id);
        } catch (Exception e) {
            throw new KafkaException("error while sending message update_ad_data", e);
        }
    }

    public void sendWithdrawRequest(Long userId, String message) {
        try {
            template.send("withdraw_request", userId.toString(), message);
        } catch (Exception e) {
            throw new KafkaException("error while sending message to withdraw_request", e);
        }
    }
}