package com.LakePayProj.adService.exceptions;

public class KafkaException extends RuntimeException {
    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
