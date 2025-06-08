package com.LakePayProj.userService.application.interfaces.mappers;

import com.LakePayProj.userService.api.DTOs.UserDTO;
import com.LakePayProj.userService.domain.model.User;
import com.LakePayProj.userService.infrastructure.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {
    @Mapping(target = "authorities", ignore = true)
    User userEntityToUser(UserEntity userEntity);

    UserEntity userToUserEntity(User user);

    UserDTO userToUserDTO(User user);

    @Mapping(target = "authorities", ignore = true)
    User userDTOToUser(UserDTO userDTO);
}
