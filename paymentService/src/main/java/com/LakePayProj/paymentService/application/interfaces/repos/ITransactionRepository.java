package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.application.interfaces.repos.ITransactionRepository;
import com.LakePayProj.paymentService.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, String>{
    @Override
    Transaction save(Transaction transaction);
}