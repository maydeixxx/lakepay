package com.lakepayProj.userService.application.interfaces.mappers;

import com.lakepayProj.userService.api.DTOs.UserDTO;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.infrastructure.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IUserMapper {
    User userEntityToUser(UserEntity userEntity);

    UserEntity userToUserEntity(User user);

    UserDTO userToUserDTO(User user);

    //    @Mapping(target = "dateOfReg", source = "dateOfReg")
//    @Mapping(target = "balance", source = "balance")
    User userDTOToUser(UserDTO userDTO);

    UserEntity toEntity(UserDTO userEntityDto);

    UserDTO toUserEntityDto(UserEntity userEntity);
}
