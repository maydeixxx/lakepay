package com.lakepayProj.chatService.controllers;

import com.lakepayProj.chatService.DTOs.ChatMessageDTO;
import com.lakepayProj.chatService.DTOs.ChatRoomDTO;
import com.lakepayProj.chatService.DTOs.UserDTO;
import com.lakepayProj.chatService.enums.MessageStatus;
import com.lakepayProj.chatService.mappers.IChatMessageMapper;
import com.lakepayProj.chatService.mappers.IChatRoomMapper;
import com.lakepayProj.chatService.mappers.IUserMapper;
import com.lakepayProj.chatService.models.ChatMessage;
import com.lakepayProj.chatService.models.ChatNotification;
import com.lakepayProj.chatService.models.User;
import com.lakepayProj.chatService.services.ChatMessageService;
import com.lakepayProj.chatService.services.ChatRoomService;
import com.lakepayProj.chatService.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("chat")
@AllArgsConstructor
public class ChatController {
    private IChatMessageMapper messageMapper;
    private SimpMessagingTemplate messagingTemplate;
    private IUserMapper userMapper;
    private ChatMessageService chatMessageService;
    private ChatRoomService chatRoomService;
    private UserService userService;

//    @MessageMapping("/chat")
//    public void processMessage(@Payload ChatMessageDTO chatMessage) {
//        User user = userService.getCurrentUser();
//        log.info("Received websocket message from {}", user.getUsername());
//
//        var chatId = chatRoomService.getChatId(chatMessage.senderName(), chatMessage.recipientName(), true);
//
//        ChatMessage message = ChatMessage
//                .builder()
//                .chatId(chatId.get())
//                .senderName(chatMessage.senderName())
//                .recipientName(chatMessage.recipientName())
//                .content(chatMessage.content())
//                .status(MessageStatus.RECEIVED)
//                .date(chatMessage.date())
//                .build();
//
//        chatMessageService.save(message);
//
//        messagingTemplate.convertAndSendToUser(
//                chatMessage.recipientName(),"/queue/messages",
//                new ChatNotification(
//                        message.getId(),
//                        message.getSenderName())
//        );
//    }

    @GetMapping(path = "/list", produces = "application/json")
    public @ResponseBody ResponseEntity<List<String>> getAvailableChats() {
        User sender = userService.getCurrentUser();

        List<String> data = chatRoomService.getChatList(sender.getUsername());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping(path = "/info/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<ChatRoomDTO> getChatInfo(@PathVariable Long id) {
        User sender = userService.getCurrentUser();
        User recipient = userService.getById(id);

        return new ResponseEntity<>(new ChatRoomDTO(
                id,
                userMapper.toDTO(sender),
                userMapper.toDTO(recipient)
        ), HttpStatus.OK);
    }

//    @GetMapping(path = "/history/{id}", produces = "application/json")
//    public @ResponseBody ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long id) {
//        User sender = userService.getCurrentUser();
//        User recipient = userService.getById(id);
//
//
//    }
//
//    @GetMapping("/messages/{senderName}/{recipientName}")
//    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable String senderName, @PathVariable String recipientName) {
//        List<ChatMessage> messages = chatMessageService.findChatMessages(senderName, recipientName);
//        messages.forEach(message -> chatMessageService.markAsDelivered(message));
//
//        List<ChatMessageDTO> data = messages.stream().map(messageMapper::toDTO).toList();
//        return new ResponseEntity<>(data, HttpStatus.OK);
//    }
}
