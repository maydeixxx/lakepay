package com.lakepayProj.userService.application.interfaces.mappers;

import com.lakepayProj.userService.api.DTOs.UserDTO;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.infrastructure.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    User userEntityToUser(UserEntity userEntity);

    UserEntity userToUserEntity(User user);

//    @Mapping(target = "tgId", ignore = true)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "balance", ignore = true)
//    @Mapping(target = "chatId", ignore = true)
    UserDTO userToUserDTO(User user);

    User userDTOToUser(UserDTO userDTO);
}
