package com.LakePayProj.chatService.mappers;

import com.LakePayProj.chatService.DTOs.ChatMessageDTO;
import com.LakePayProj.chatService.models.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IChatMessageMapper {
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "date", source = "date")
    ChatMessageDTO toDTO(ChatMessage message);
}