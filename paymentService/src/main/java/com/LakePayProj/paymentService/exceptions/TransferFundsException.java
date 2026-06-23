package com.LakePayProj.paymentService.exceptions;

public class TransferFundsException extends RuntimeException {
    public TransferFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
