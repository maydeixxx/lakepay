package com.LakePayProj.userService.mapper;


import com.LakePayProj.userService.dto.PublicUserDto;
import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    UserDto toDto(User user);

    @Mapping(target = "credential", ignore = true)
    User toUser(UserDto userDto);

    PublicUserDto toPublicDto(User user);
}
