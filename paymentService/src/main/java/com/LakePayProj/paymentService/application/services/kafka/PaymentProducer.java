package com.LakePayProj.paymentService.application.services.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {
    private final KafkaTemplate<String, String> template;

    public void sendWithdrawFailed(String message) {
        template.send("withdraw_failed", message);
    }

    public void sendWithdrawConfirmed(String message) {
        template.send("withdraw_confirmed", message);
    }

    public void sendPaymentCreated(String message) {
        template.send("payment_created", message);
    }

    public void sendAdData(String message) {
        template.send("ad_data", message);
    }

    public void getAdData(String id) {
        template.send("get_ad_data", 0, "adId", id );
    }

    public void getUserDataById(String id) {
        template.send("get_user_data_by_id", "userId", id);
    }
}
