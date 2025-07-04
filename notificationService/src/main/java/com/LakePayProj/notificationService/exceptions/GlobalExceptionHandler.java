package com.LakePayProj.notificationService.exceptions;

import com.LakePayProj.notificationService.models.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WithdrawException.class)
    public ResponseEntity<ApiResponse<?>> handleWithdrawException(WithdrawException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "WITHDRAW_EXCEPTION",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("withdraw funds failed")
                .error(errorDetails)
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SendMessageException.class)
    public ResponseEntity<ApiResponse<?>> handleSendMessageException(SendMessageException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "SEND_MESSAGE_EXCEPTION",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("failed send message")
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT);
    }

}
