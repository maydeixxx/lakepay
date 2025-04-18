package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.application.interfaces.repos.AccountRepository;
import com.LakePayProj.paymentService.application.interfaces.repos.TransactionRepository;
import com.LakePayProj.paymentService.domain.Account;
import com.LakePayProj.paymentService.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionRepository transactionRepo;


    public void topUp(Long userId, BigDecimal amount) {
        Account account = accountRepo.findById(userId)
                .orElse(new Account(userId, BigDecimal.ZERO));

        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        transactionRepo.save(new Transaction(userId, amount, "TOP_UP", LocalDateTime.now()));
    }

    public boolean purchase(Long userId, BigDecimal totalPrice) {
        Account account = accountRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Аккаунт не найден"));

        if (account.getBalance().compareTo(totalPrice) >= 0) {
            account.setBalance(account.getBalance().subtract(totalPrice));
            accountRepo.save(account);

            transactionRepo.save(new Transaction(userId, totalPrice.negate(), "PURCHASE", LocalDateTime.now()));
            return true;
        }

        return false;
    }
}

