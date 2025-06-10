package com.LakePayProj.chatService.repository;

import com.LakePayProj.chatService.enums.MessageStatus;
import com.LakePayProj.chatService.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatId(String chatId);
    long countByRecipientUsernameAndStatus(String recipientUsername, MessageStatus status);
    List<ChatMessage> findAllMessages();
}
