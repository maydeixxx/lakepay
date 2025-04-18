package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {}
