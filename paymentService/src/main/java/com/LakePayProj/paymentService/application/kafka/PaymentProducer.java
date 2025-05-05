package com.LakePayProj.paymentService.application.kafka;

import com.LakePayProj.paymentService.application.interfaces.services.IKafkaProducerService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentProducer implements IKafkaProducerService {
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    @Override
    public void sendPaymentEvent(Transaction transaction) {
        kafkaTemplate.send("payment-events", transaction.getId(), transaction);
    }
}