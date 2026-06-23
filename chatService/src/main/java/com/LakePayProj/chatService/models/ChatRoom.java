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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @OneToMany(mappedBy = "chatId")
    private List<ChatMessage> messages;
}
