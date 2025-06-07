package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByInvoiceId(String invoiceId);
    Optional<PaymentEntity> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndStatus(Long userId, String status);
}