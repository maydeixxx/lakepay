package com.LakePayProj.chatService.mappers;

import com.LakePayProj.chatService.DTOs.UserDTO;
import com.LakePayProj.chatService.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    UserDTO toDTO(User user);
}
