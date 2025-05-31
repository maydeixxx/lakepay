package com.lakepayProj.chatService.models;

import com.lakepayProj.chatService.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Builder
@Table(name = "messages")
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User recipient;
    private String content;
    private Date date;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private String chatId;
}
