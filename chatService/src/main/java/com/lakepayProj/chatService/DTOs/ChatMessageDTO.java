package com.lakepayProj.chatService.DTOs;

import java.util.Date;

public record ChatMessageDTO(
        String content,
        UserDTO sender,
        UserDTO recipient,
        Date date
) {}