package com.LakePayProj.paymentService.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDetails {
    private String code;
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
