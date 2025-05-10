package com.lakepayProj.chatService.services;

import com.lakepayProj.chatService.models.ChatRoom;
import com.lakepayProj.chatService.repository.IChatRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatRoomService {
    private IChatRoomRepository repository;

    public Optional<String> getChatId(String senderName, String recipientName, boolean createIfNotExist) {
        return repository
                .findBySenderNameAndRecipientName(senderName, recipientName)
                .map(ChatRoom::getChatId)
                .or(() -> {
                    // Prevent user from opening a chat with themselves
                    if (recipientName.compareTo(senderName) == 0) {
                        return Optional.empty();
                    }

                    if (!createIfNotExist) {
                        return Optional.empty();
                    }

                    String chatId = createChatId(senderName, recipientName);

                    return Optional.of(chatId);
                });
    }

    public List<String> getChatList(String username) {
        return repository.findByRecipientName(username).stream().map(ChatRoom::getRecipientName).toList();
    }

    public String createChatId(String senderName, String recipientName) {
        String chatId = String.format("%s_%s", senderName, recipientName);

        ChatRoom senderRoom = ChatRoom
                .builder()
                .senderName(senderName)
                .recipientName(recipientName)
                .chatId(chatId)
                .build();

        ChatRoom recipientRoom = ChatRoom
                .builder()
                .senderName(recipientName)
                .recipientName(senderName)
                .chatId(chatId)
                .build();

        repository.save(senderRoom);
        repository.save(recipientRoom);

        return chatId;
    }
}
