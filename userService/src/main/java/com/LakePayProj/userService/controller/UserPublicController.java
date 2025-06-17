package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/public")
@RequiredArgsConstructor
public class UserPublicController {

    private final IUserMapper userMapper;
    private final UserService userService;

    @GetMapping("/profile/{username}")
    public ResponseEntity<ApiResponse<?>> userProfile(@PathVariable String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));
        UserDto response = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchUsersByUsername(@RequestParam String username) {
        List<UserDto> users = userService.searchUsersByUsername(username).stream().map(userMapper::toDto).toList();
        return ResponseEntity.ok(ApiResponse.<List<UserDto>>builder()
                .success(true)
                .message("Users found")
                .data(users)
                .build());
    }


}
