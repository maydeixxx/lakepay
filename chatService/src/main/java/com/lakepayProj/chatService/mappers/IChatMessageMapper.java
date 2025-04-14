package com.lakepayProj.chatService.mappers;

import com.lakepayProj.chatService.DTOs.ChatMessageDTO;
import com.lakepayProj.chatService.models.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IChatMessageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "chatId", ignore = true)
    @Mapping(target = "chat", ignore = true)
    ChatMessageDTO toDTO(ChatMessage message);
}
