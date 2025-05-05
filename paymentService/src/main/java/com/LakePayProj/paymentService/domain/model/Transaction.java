package com.LakePayProj.paymentService.domain.model;

import com.LakePayProj.paymentService.domain.valueObject.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Transaction {
    @Id
    private String id;
    private Long userId;
    private Double amount;
    private PaymentType type; // DEPOSIT, PURCHASE
    private Long adId; // null для пополнения
    private String status; // PENDING, SUCCESS, FAILED

    public String getId() {
        return id;
    }
}