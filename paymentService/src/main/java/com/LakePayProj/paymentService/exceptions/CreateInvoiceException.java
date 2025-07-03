package com.LakePayProj.paymentService.exceptions;

public class CreateInvoiceException extends RuntimeException {
    public CreateInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
