package com.LakePayProj.paymentService.application.interfaces.repositories;

import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPaymentRepository extends JpaRepository<PaymentEntity, Long> {
    PaymentEntity findByInvoiceId(String invoiceId);
}
