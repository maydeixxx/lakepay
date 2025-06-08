package com.LakePayProj.chatService.services;

import com.LakePayProj.chatService.models.ChatRoom;
import com.LakePayProj.chatService.models.User;
import com.LakePayProj.chatService.repository.IChatRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatRoomService {
    private IChatRoomRepository repository;

    public Optional<String> getChatId(User sender, User recipient, boolean createIfNotExist) {
        return repository
                .findBySenderUsernameAndRecipientUsername(sender.getUsername(), recipient.getUsername())
                .map(ChatRoom::getChatId)
                .or(() -> {
                    // Prevent user from opening a chat with themselves
                    if (recipient.getUsername().compareTo(sender.getUsername()) == 0) {
                        return Optional.empty();
                    }

                    if (!createIfNotExist) {
                        return Optional.empty();
                    }

                    String chatId = createChatId(sender, recipient);

                    return Optional.of(chatId);
                });
    }

    public List<String> getChatList(String username) {
        return repository.findByRecipientUsername(username)
                .stream()
                .map(room -> room.getRecipient().getUsername())
                .toList();
    }

    public String createChatId(User sender, User recipient) {
        String chatId = String.format("%s_%s", sender.getUsername(), recipient.getUsername());

        ChatRoom senderRoom = ChatRoom
                .builder()
                .sender(sender)
                .recipient(recipient)
                .chatId(chatId)
                .build();

        ChatRoom recipientRoom = ChatRoom
                .builder()
                .sender(recipient)
                .recipient(sender)
                .chatId(chatId)
                .build();

        repository.save(senderRoom);
        repository.save(recipientRoom);

        return chatId;
    }
}
