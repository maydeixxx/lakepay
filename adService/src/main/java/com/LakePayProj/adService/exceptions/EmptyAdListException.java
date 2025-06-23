package com.LakePayProj.adService.exceptions;

public class EmptyAdListException extends RuntimeException {
    public EmptyAdListException(String message) {
        super(message);
    }
}
