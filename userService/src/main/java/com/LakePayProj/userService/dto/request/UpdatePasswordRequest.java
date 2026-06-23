package com.LakePayProj.userService.dto.request;

public record UpdatePasswordRequest(String currentPassword, String newPassword) {
}
