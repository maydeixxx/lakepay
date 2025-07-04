package com.LakePayProj.paymentService.exceptions;

public class PaymentExistsException extends RuntimeException {
    public PaymentExistsException(String message) {
        super(message);
    }
}
