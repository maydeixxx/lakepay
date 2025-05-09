package com.lakepayProj.chatService.repository;

import com.lakepayProj.chatService.enums.MessageStatus;
import com.lakepayProj.chatService.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatId(String chatId);
    long countByRecipientNameAndStatus(String recipientName, MessageStatus status);
}
