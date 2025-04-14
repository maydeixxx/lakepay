package com.lakepayProj.chatService.repository;

import com.lakepayProj.chatService.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findBySenderNameAndRecipientName(String senderName, String recipientName);
    Optional<ChatRoom> findByChatId(String chatId);
    List<ChatRoom> findBySenderName(String senderName);
    List<ChatRoom> findByRecipientName(String recipientName);
}
