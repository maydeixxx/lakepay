package com.lakepayProj.chatService.DTOs;

import lombok.Value;

@Value
public class ChatRoomDTO {
    Long id;
    String senderName;
    String recipientName;
}
