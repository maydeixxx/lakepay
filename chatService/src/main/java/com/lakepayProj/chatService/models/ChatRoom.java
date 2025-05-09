package com.lakepayProj.chatService.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Builder
@Table(name = "chats")
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String chatId;
    private String senderName;
    private String recipientName;
    @OneToMany(mappedBy = "chatId")
    private List<ChatMessage> messages;
}
