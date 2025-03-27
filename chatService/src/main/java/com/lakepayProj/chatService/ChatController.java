package com.lakepayProj.chatService;

import com.lakepayProj.chatService.services.ChatMessageService;
import com.lakepayProj.chatService.services.ChatRoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private SimpMessagingTemplate messagingTemplate;
    private ChatMessageService chatMessageService;
    private ChatRoomService chatRoomService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(
            @Payload ChatMessageDTO chatMessage
    ) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(
            @Payload ChatMessageDTO chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // Add username to websocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }
}
