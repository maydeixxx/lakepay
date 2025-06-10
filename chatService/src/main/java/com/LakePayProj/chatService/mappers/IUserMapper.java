package com.LakePayProj.chatService.mappers;

import com.LakePayProj.chatService.DTOs.UserDTO;
import com.LakePayProj.chatService.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    UserDTO toDTO(User user);
    @Mapping(target = "tgId", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "dateOfReg", ignore = true)
    @Mapping(target = "chatId", ignore = true)
    @Mapping(target = "balance", ignore = true)
    User toUser(UserDTO userDTO);
}
