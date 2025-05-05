package com.LakePayProj.paymentService.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserBalance {
    @Id
    private Long userId;
    private Double balance;
}