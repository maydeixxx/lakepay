package com.lakepayProj.chatService.mappers;

import com.lakepayProj.chatService.DTOs.ChatRoomDTO;
import com.lakepayProj.chatService.models.ChatRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IChatRoomMapper {
    ChatRoomDTO toDTO(ChatRoom chatRoom);
}
