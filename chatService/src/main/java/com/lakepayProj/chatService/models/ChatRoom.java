package com.lakepayProj.chatService.models;

import com.lakepayProj.chatService.enums.ChatRoomStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "chats")
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;
    private Long senderId;
    private Long recipientId;
    private ChatRoomStatus state;
    @OneToMany(mappedBy = "chatId")
    private List<ChatMessage> messages;
}
