package com.LakePayProj.userService.mapper;


import com.LakePayProj.userService.dto.UserDTO;
import com.LakePayProj.userService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "credential", ignore = true)
    User toUser(UserDTO userDTO);
}
