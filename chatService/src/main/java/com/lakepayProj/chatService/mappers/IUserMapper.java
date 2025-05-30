package com.lakepayProj.chatService.mappers;

import com.lakepayProj.chatService.DTOs.UserDTO;
import com.lakepayProj.chatService.models.User;

public interface IUserMapper {
    UserDTO toDTO(User user);
}
