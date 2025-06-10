package com.LakePayProj.chatService.services;

import com.LakePayProj.chatService.DTOs.ChatRoomDTO;
import com.LakePayProj.chatService.mappers.IChatRoomMapper;
import com.LakePayProj.chatService.models.ChatRoom;
import com.LakePayProj.chatService.models.User;
import com.LakePayProj.chatService.repository.IChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ChatRoomService {
    private IChatRoomRepository repository;
    private IChatRoomMapper chatRoomMapper;

    @Transactional
    public Optional<String> getChatId(User sender, User recipient, boolean createIfNotExist) {
        return repository
                .findBySenderUsernameAndRecipientUsername(sender.getUsername(), recipient.getUsername())
                .map(ChatRoom::getChatId)
                .or(() -> {
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

    @Transactional
    public String createChatId(User sender, User recipient) {
        log.info("Creating chat with sender: id={}, username={}, recipient: id={}, username={}",
                sender.getId(), sender.getUsername(), recipient.getId(), recipient.getUsername());

        String chatId = String.format("%s_%s", sender.getUsername(), recipient.getUsername());

        ChatRoom senderRoom = ChatRoom.builder()
                .sender(sender)
                .recipient(recipient)
                .chatId(chatId)
                .build();

        ChatRoom recipientRoom = ChatRoom.builder()
                .sender(recipient)
                .recipient(sender)
                .chatId(chatId)
                .build();

        repository.save(senderRoom);
        repository.save(recipientRoom);
        log.info("Saved chat rooms for chat ID: {}", chatId);

        return chatId;
    }

    public List<ChatRoomDTO> getChatList(String username) {
        return repository.findByRecipientUsername(username)
                .stream()
                .map(chatRoomMapper::toDTO)
                .toList();
    }
}
