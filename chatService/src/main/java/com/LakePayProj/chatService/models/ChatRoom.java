package com.LakePayProj.chatService.models;

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
    @ManyToOne
    private User sender;
    @ManyToOne
    private User recipient;
    @OneToMany(mappedBy = "chatId")
    private List<ChatMessage> messages;
}
