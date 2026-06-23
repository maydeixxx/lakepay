package com.LakePayProj.paymentService.exceptions;

import com.LakePayProj.paymentService.models.DTOs.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ApiResponse<?>> handleKafkaException(KafkaException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "KAFKA_EXCEPTION",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("error while using kafka")
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentNotFoundException(PaymentNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "failed to found payment",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message("error while searching payment")
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(CreateInvoiceException.class)
    public ResponseEntity<ApiResponse<?>> handleCreateInvoiceException(CreateInvoiceException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while creating new invoice",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(PayAdException.class)
    public ResponseEntity<ApiResponse<?>> handlePayAdException(PayAdException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while paying ad",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UpdateBalanceException.class)
    public ResponseEntity<ApiResponse<?>> handleUpdateBalanceException(UpdateBalanceException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while updating user balance",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UpdateAdStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleUpdateAdStatusException(UpdateAdStatusException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while updating ad status",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(TransferFundsException.class)
    public ResponseEntity<ApiResponse<?>> handleTransferFundsException(TransferFundsException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while transferring funds",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(GetCourseException.class)
    public ResponseEntity<ApiResponse<?>> handleGetCourseException(GetCourseException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "error while getting course",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(PaymentExistsException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentExistsException(PaymentExistsException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "payment already exists for user",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "user not found",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(AdNotFoundException.class)
    public ResponseEntity<?> handleAdNotFoundException(AdNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "ad not found",
                exception.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .success(false)
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build(), HttpStatus.NOT_FOUND);
    }

}
