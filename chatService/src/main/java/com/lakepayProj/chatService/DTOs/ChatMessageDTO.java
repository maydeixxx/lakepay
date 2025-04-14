package com.lakepayProj.chatService.DTOs;

import lombok.Value;

import java.util.Date;

@Value
public class ChatMessageDTO {
    String content;
    String senderName;
    String recipientName;
    Date date;
}