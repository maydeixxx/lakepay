package com.LakePayProj.adService.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class ErrorDetails {
    private String errorCode;
    private String message;
    private String details;
    private LocalDate timestamp;
}
