package com.LakePayProj.paymentService.exceptions;

public class UpdateBalanceException extends RuntimeException {
    public UpdateBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
