package com.LakePayProj.adService.DTOs;

import com.LakePayProj.adService.exceptions.ErrorDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private ErrorDetails errorDetails;
}
