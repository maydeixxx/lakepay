package com.lakepayProj.chatService.mappers;

import com.lakepayProj.chatService.DTOs.UserDTO;
import com.lakepayProj.chatService.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    UserDTO toDTO(User user);
}
