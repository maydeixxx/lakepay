package com.LakePayProj.notificationService.exceptions;

public class UserExistsInHash extends RuntimeException {
    public UserExistsInHash(String message) {
        super(message);
    }
}
