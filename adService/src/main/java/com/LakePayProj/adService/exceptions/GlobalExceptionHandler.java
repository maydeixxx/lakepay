package com.LakePayProj.adService.exceptions;

import com.LakePayProj.adService.DTOs.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AdNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAdNotfoundException(AdNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "AD_NOT_FOUND",
                exception.getMessage(),
                request.getDescription(false),
                LocalDate.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("ad not found")
                .errorDetails(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse<?>> handleDatabaseException(DatabaseException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "DATABASE_ERROR",
                exception.getMessage(),
                request.getDescription(false),
                LocalDate.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("database error")
                .errorDetails(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(EmptyAdListException.class)
    public ResponseEntity<ApiResponse<?>> handleEmptyAdList(EmptyAdListException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "EMPTY_AD_LIST",
                exception.getMessage(),
                request.getDescription(false),
                LocalDate.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("empty ad list")
                .errorDetails(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ApiResponse<?>> handleKafkaException(KafkaException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "Kafka error",
                exception.getMessage(),
                request.getDescription(false),
                LocalDate.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("kafka error")
                .errorDetails(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }
}
