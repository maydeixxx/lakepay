package com.LakePayProj.adService.DTOs;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdDtoUpdate(
        String field,
        String title,
        String body,
        String category,
        BigDecimal price,
        String login,
        String password,
        Boolean sold
) {
}
