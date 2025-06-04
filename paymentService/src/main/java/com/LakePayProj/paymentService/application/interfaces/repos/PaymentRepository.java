package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    PaymentEntity findByInvoiceId(String invoiceId);
    boolean existsByInvoiceIdAndStatus(String invoiceId, String status);
    boolean existsByUserIdAndStatus(Long userId, String status);
}