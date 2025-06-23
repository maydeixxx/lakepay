package com.LakePayProj.adService.DTOs;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdDto(
        Long id,
        String title,
        String body,
        String category,
        BigDecimal price,
        String login,
        String password,
        LocalDate dateOfPush,
        Boolean sold,
        Long sellerId
) {
}
