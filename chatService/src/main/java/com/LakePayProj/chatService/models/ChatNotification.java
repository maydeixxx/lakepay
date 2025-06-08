package com.LakePayProj.chatService.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ChatNotification {
    private Long id;
    private String senderName;
}
