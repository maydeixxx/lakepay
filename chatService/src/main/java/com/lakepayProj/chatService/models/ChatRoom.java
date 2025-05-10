package com.lakepayProj.chatService.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chats")
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;
    private String chatId;
    @Column(nullable = false)
    private String senderName;
    @Column(nullable = false)
    private String recipientName;
    @OneToMany(mappedBy = "chatId")
    private List<ChatMessage> messages;
}
