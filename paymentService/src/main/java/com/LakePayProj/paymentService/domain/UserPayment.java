package com.LakePayProj.paymentService.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPayment {

    @Id
    private Long userId;

    private BigDecimal balance;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_transactions", joinColumns = @JoinColumn(name = "user_id"))
    private List<TransactionLog> transactions = new ArrayList<>();

    public void addTransaction(String type, BigDecimal amount) {
        transactions.add(new TransactionLog(type, amount, LocalDateTime.now()));
    }
}
