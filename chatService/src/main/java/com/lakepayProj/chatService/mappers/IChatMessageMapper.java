package com.lakepayProj.chatService.mappers;

import com.lakepayProj.chatService.DTOs.ChatMessageDTO;
import com.lakepayProj.chatService.models.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IChatMessageMapper {
    @Mapping(target = "content", source = "content")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "date", source = "date")
    ChatMessageDTO toDTO(ChatMessage message);
}
