package com.LakePayProj.paymentService.application.interfaces.services;

public interface IKafkaProducerService {
    void sendPaymentEvent(Transaction transaction);
}
