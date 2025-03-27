package com.lakepayProj.chatService.models;

import com.lakepayProj.chatService.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "messages")
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    private Long senderId;
    private Long recipientId;
    private String content;
    private String senderName;
    private String recipientName;
    private Date date;
    private MessageStatus status;
    @ManyToOne
    @JoinColumn(name = "chatId", nullable = false)
    private ChatRoom chat;
}
