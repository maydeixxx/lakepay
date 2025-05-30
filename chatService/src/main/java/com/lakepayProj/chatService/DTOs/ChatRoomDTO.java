package com.lakepayProj.chatService.DTOs;

public record ChatRoomDTO(
        Long id,
        UserDTO sender,
        UserDTO recipient
) {}
