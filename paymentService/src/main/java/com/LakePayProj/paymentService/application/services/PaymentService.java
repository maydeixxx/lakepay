package com.LakePayProj.paymentService.application.services;

import com.LakePayProj.paymentService.api.DTOs.PaymentRequestDto;
import com.LakePayProj.paymentService.api.DTOs.PaymentStatusDto;
import com.LakePayProj.paymentService.application.interfaces.services.IPaymentService;
import com.LakePayProj.paymentService.application.interfaces.services.IKafkaProducerService;
import com.LakePayProj.paymentService.application.interfaces.repos.IUserBalanceRepository;
import com.LakePayProj.paymentService.application.interfaces.repos.ITransactionRepository;
import com.LakePayProj.paymentService.domain.model.Transaction;
import com.LakePayProj.paymentService.domain.model.UserBalance;
import com.LakePayProj.paymentService.domain.infrastructure.AdServiceClient;
import com.LakePayProj.paymentService.domain.valueObject.PaymentType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService implements IPaymentService {
    private final IUserBalanceRepository balanceRepository;
    private final ITransactionRepository transactionRepository;
    private final AdServiceClient adServiceClient;
    private final IKafkaProducerService kafkaProducer;

    @Transactional
    @Override
    public PaymentStatusDto deposit(PaymentRequestDto request) {
        UserBalance balance = balanceRepository.findById(request.getUserId())
                .orElse(new UserBalance());
        balance.setUserId(request.getUserId());
        balance.setBalance(balance.getBalance() == null ? request.getAmount() : balance.getBalance() + request.getAmount());
        balanceRepository.save(balance);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setType(PaymentType.DEPOSIT);
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        kafkaProducer.sendPaymentEvent(transaction);

        return createStatusDto(transaction);
    }

    @Transactional
    @Override
    public PaymentStatusDto purchaseAd(PaymentRequestDto request) {
        UserBalance balance = balanceRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User balance not found"));

        Double adPrice = adServiceClient.getAdPrice(request.getAdId());
        if (balance.getBalance() < adPrice) {
            throw new RuntimeException("Insufficient funds");
        }

        balance.setBalance(balance.getBalance() - adPrice);
        balanceRepository.save(balance);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(adPrice);
        transaction.setType(PaymentType.PURCHASE);
        transaction.setAdId(request.getAdId());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        kafkaProducer.sendPaymentEvent(transaction);

        return createStatusDto(transaction);
    }

    private PaymentStatusDto createStatusDto(Transaction transaction) {
        PaymentStatusDto status = new PaymentStatusDto();
        status.setTransactionId(transaction.getId());
        status.setStatus(transaction.getStatus());
        status.setAmount(transaction.getAmount());
        status.setUserId(transaction.getUserId());
        status.setAdId(transaction.getAdId());
        return status;
    }
}