package com.lakepayProj.chatService.controllers;

import com.lakepayProj.chatService.DTOs.ChatMessageDTO;
import com.lakepayProj.chatService.enums.MessageStatus;
import com.lakepayProj.chatService.mappers.IChatMessageMapper;
import com.lakepayProj.chatService.models.ChatMessage;
import com.lakepayProj.chatService.models.ChatNotification;
import com.lakepayProj.chatService.services.ChatMessageService;
import com.lakepayProj.chatService.services.ChatRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@AllArgsConstructor
public class ChatController {
    private SimpMessagingTemplate messagingTemplate;
    private ChatMessageService chatMessageService;
    private ChatRoomService chatRoomService;
    private IChatMessageMapper messageMapper;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        //
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        var chatId = chatRoomService.getChatId(chatMessage.getSenderName(), chatMessage.getRecipientName(), true);

        ChatMessage message = ChatMessage
                .builder()
                .chatId(chatId.get())
                .senderName(chatMessage.getSenderName())
                .recipientName(chatMessage.getRecipientName())
                .content(chatMessage.getContent())
                .status(MessageStatus.RECEIVED)
                .date(chatMessage.getDate())
                .build();

        chatMessageService.save(message);

        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientName(),"/queue/messages",
                new ChatNotification(
                        message.getId(),
                        message.getSenderName())
        );
    }

    // TODO: Authenticate user
    @GetMapping("/chat/list/{username}")
    public ResponseEntity<List<String>> getAvailableChats(@PathVariable String username) {

        List<String> data = chatRoomService.getChatList(username);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/messages/{senderName}/{recipientName}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable String senderName, @PathVariable String recipientName) {
        List<ChatMessage> messages = chatMessageService.findChatMessages(senderName, recipientName);
        messages.forEach(message -> chatMessageService.markAsDelivered(message));

        List<ChatMessageDTO> data = messages.stream().map(messageMapper::toDTO).toList();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
