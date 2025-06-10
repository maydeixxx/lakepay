package com.LakePayProj.chatService.services;

import com.LakePayProj.chatService.enums.MessageStatus;
import com.LakePayProj.chatService.models.ChatMessage;
import com.LakePayProj.chatService.models.User;
import com.LakePayProj.chatService.repository.IChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatMessageService {
    private IChatMessageRepository repository;
    private ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        repository.save(chatMessage);
        return chatMessage;
    }

    public long newMessageCount(String recipientName) {
        return repository.countByRecipientUsernameAndStatus(recipientName, MessageStatus.RECEIVED);
    }

    public List<ChatMessage> findChatMessages(User sender, User recipient) {
        Optional<String> chatId = chatRoomService.getChatId(sender, recipient, false);
        return chatId.map(id -> repository.findByChatId(id)).orElse(new ArrayList<>());
    }

    public ChatMessage markAsDelivered(ChatMessage chatMessage) {
        chatMessage.setStatus(MessageStatus.DELIVERED);
        repository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findAllMessages() {
        return repository.findAllMessages();
    }
}
