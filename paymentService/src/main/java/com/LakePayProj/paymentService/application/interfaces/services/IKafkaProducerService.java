package com.LakePayProj.paymentService.application.interfaces.services;

import com.LakePayProj.paymentService.domain.model.Transaction;

public interface IKafkaProducerService {
    void sendPaymentEvent(Transaction transaction);
}
