package com.LakePayProj.chatService.mappers;

import com.LakePayProj.chatService.DTOs.ChatRoomDTO;
import com.LakePayProj.chatService.models.ChatRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IChatRoomMapper {
    ChatRoomDTO toDTO(ChatRoom chatRoom);
}
