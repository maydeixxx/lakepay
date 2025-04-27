package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.domain.UserPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, Long> {
}
