package com.LakePayProj.userService.exception;

public class KafkaException extends RuntimeException {
    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
