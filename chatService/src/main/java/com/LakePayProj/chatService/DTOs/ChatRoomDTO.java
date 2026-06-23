package com.LakePayProj.chatService.DTOs;

public record ChatRoomDTO(
        Long id,
        UserDTO sender,
        UserDTO recipient
) {}
