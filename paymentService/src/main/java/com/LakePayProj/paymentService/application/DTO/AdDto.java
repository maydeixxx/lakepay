package com.LakePayProj.paymentService.application.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdDto {
    private Long id;
    private String title;
    private String body;
    private String category;
    private BigDecimal price;
    private Integer countOfViews;
    private Integer quantity;
    private String dateOfPush;
    private Boolean sold;
}
