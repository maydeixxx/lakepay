package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.exception.DatabaseException;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.kafka.UserProducer;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/admin")
@RequiredArgsConstructor
public class UserAdminController {

    private final IUserMapper userMapper;
    private final UserService userService;
    private final UserProducer userProducer;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> allUsers() {
        List<UserDto> allUsers = userService.findAll().stream().map(userMapper::toDto).toList();
        return ResponseEntity.ok(ApiResponse.<List<UserDto>>builder()
                .success(true)
                .message("Fetched all registered users")
                .data(allUsers)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<?>> userById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with ID: " + id));
        UserDto response = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tg/{id}")
    public ResponseEntity<ApiResponse<?>> userByTgId(@PathVariable Long id) {
        User user = userService.findByTelegramId(id)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with Telegram ID: " + id));
        UserDto response = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<?>> saveUser(@RequestBody UserDto req) {
        try {
            User user = userService.create(userMapper.toUser(req));
            userProducer.publishCreateUser(user);

            return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                    .success(true)
                    .message("Successfully saved new user")
                    .data(userMapper.toDto(user))
                    .build());
        } catch (Exception e) {
            throw new DatabaseException("Failed to save user", e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long id, @RequestBody UserDto req) {
        try {
            User user = userService.update(id, userMapper.toUser(req));
            userProducer.publishUpdateUser(user);

            return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                    .success(true)
                    .message("Successfully updated user")
                    .data(userMapper.toDto(user))
                    .build());
        } catch (Exception e) {
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id);
            userProducer.publishDeleteUser(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Successfully deleted user")
                    .data(null)
                    .build());
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete user", e);
        }
    }
}
