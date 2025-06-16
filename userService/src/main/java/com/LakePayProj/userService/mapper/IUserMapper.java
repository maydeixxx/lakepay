package com.LakePayProj.userService.mapper;


import com.LakePayProj.userService.dto.UserResponse;
import com.LakePayProj.userService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    UserResponse toDto(User user);

    @Mapping(target = "credential", ignore = true)
    User toUser(UserResponse userResponse);
}
