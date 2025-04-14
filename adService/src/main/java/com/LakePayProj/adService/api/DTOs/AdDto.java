package com.LakePayProj.adService.api.DTOs;

import lombok.Value;

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
    Integer countOfViews;
    Integer quantity;
    LocalDate dateOfPush;
    Boolean sold;
}