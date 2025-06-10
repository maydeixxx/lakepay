package com.LakePayProj.chatService.controllers;

import com.LakePayProj.chatService.DTOs.ChatMessageDTO;
import com.LakePayProj.chatService.DTOs.ChatRoomDTO;
import com.LakePayProj.chatService.enums.MessageStatus;
import com.LakePayProj.chatService.mappers.IChatMessageMapper;
import com.LakePayProj.chatService.mappers.IUserMapper;
import com.LakePayProj.chatService.models.ChatMessage;
import com.LakePayProj.chatService.models.ChatNotification;
import com.LakePayProj.chatService.models.User;
import com.LakePayProj.chatService.services.ChatMessageService;
import com.LakePayProj.chatService.services.ChatRoomService;
import com.LakePayProj.chatService.services.UserService;
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

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("chat")
@AllArgsConstructor
public class ChatController {
    private SimpMessagingTemplate messagingTemplate;
    private IUserMapper userMapper;
    private IChatMessageMapper messageMapper;
    private ChatMessageService chatMessageService;
    private ChatRoomService chatRoomService;
    private UserService userService;

    @MessageMapping("/send")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        User user = userService.getCurrentUser();
        log.info("Received websocket message from {}", user.getUsername());

        User sender = userMapper.toUser(chatMessage.sender());
        User recipient = userMapper.toUser(chatMessage.recipient());
        var chatId = chatRoomService.getChatId(sender, recipient, true);

        ChatMessage message = ChatMessage
                .builder()
                .chatId(chatId.get())
                .sender(sender)
                .recipient(recipient)
                .content(chatMessage.content())
                .status(MessageStatus.RECEIVED)
                .date(new Date())
                .build();

        chatMessageService.save(message);

        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),"/queue/messages",
                new ChatNotification(
                        message.getId(),
                        sender.getUsername())
        );
    }

    @GetMapping(path = "/list", produces = "application/json")
    public @ResponseBody ResponseEntity<List<ChatRoomDTO>> getAvailableChats() {
        User sender = userService.getCurrentUser();

        List<ChatRoomDTO> data = chatRoomService.getChatList(sender.getUsername());
        return ResponseEntity.ok(data);
    }

    @GetMapping(path = "/info/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<ChatRoomDTO> getChatInfo(@PathVariable Long id) {
        User sender = userService.getCurrentUser();
        User recipient = userService.getById(id);

        if (recipient == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new ChatRoomDTO(
                id,
                userMapper.toDTO(sender),
                userMapper.toDTO(recipient)
        ));
    }

    @GetMapping(path = "/history/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long id) {
        User sender = userService.getCurrentUser();
        User recipient = userService.getById(id);

        var messages = chatMessageService.findChatMessages(sender, recipient)
                .stream().map(messageMapper::toDTO).toList();
        return ResponseEntity.ok(messages);
    }
}
