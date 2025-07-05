package com.LakePayProj.paymentService.models.DTOs;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdUpdateDto {

    private BigDecimal price;
    private String login;
    private String password;
    private Long sellerId;

}
