package com.LakePayProj.userService.exception;

import com.LakePayProj.userService.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        log.error("User already exists ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "USER_ALREADY_EXISTS",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("User already exists")
                .error(errorDetails)
                .build(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        log.error("User not found ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "USER_NOT_FOUND",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("User not found")
                .error(errorDetails)
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
        log.error("Token validation failed ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "INVALID_TOKEN",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("Token validation failed")
                .error(errorDetails)
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ApiResponse<?>> handleKafkaException(KafkaException ex, WebRequest request) {
        log.error("Kafka exception ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "KAFKA_EXCEPTION",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("Internal server error")
                .error(errorDetails)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        log.error("Access denied, unauthorized ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "UNAUTHORIZED",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("Access denied, unauthorized")
                .error(errorDetails)
                .build(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse<?>> handleDatabaseException(DatabaseException ex, WebRequest request) {
        log.error("Database error ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "DATABASE_ERROR",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("Internal server error")
                .error(errorDetails)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle generic exceptions as a fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Internal server error ", ex);
        ErrorDetails errorDetails = new ErrorDetails(
                "SOMETHING_WENT_WRONG",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(ApiResponse.<Void>builder()
                .success(false)
                .message("Internal server error")
                .error(errorDetails)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}