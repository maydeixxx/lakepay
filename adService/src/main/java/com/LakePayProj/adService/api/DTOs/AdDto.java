package com.LakePayProj.adService.api.DTOs;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link com.LakePayProj.adService.infrastructure.AdEntity}
 */
@Value
public class AdDto {
    Long id;
    String title;
    String body;
    String category;
    BigDecimal price;
    Integer countOfViews;
    Integer quantity;
    LocalDate dateOfPush;
    Boolean sold;
}