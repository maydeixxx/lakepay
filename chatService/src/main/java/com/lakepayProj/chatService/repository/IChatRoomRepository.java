package com.lakepayProj.chatService.repository;

import com.lakepayProj.chatService.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByChatId(String chatId);
    Optional<ChatRoom> findBySenderUsernameAndRecipientUsername(String senderUsername, String recipientUsername);
    List<ChatRoom> findBySenderUsername(String senderUsername);
    List<ChatRoom> findByRecipientUsername(String recipientUsername);
}
