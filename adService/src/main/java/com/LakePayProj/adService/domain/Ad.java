package com.LakePayProj.adService.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Ad {
    private Long id;
    private String title;
    private String body;
    private String category;
    private BigDecimal price;
    private Integer countOfViews;
    private Integer quantity;
    private LocalDate dateOfPush;
    private Boolean sold;
}
