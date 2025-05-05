package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.domain.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    PaymentEntity findByInvoiceId(String invoiceId);
}