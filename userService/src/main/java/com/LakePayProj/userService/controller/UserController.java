package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.exception.DatabaseException;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.kafka.UserProducer;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IUserMapper userMapper;
    private final UserService userService;
    private final UserProducer userProducer;

    // Защищенные маршруты

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));
        UserDto response = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched current user data")
                .data(response)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDto req) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

        try {
            user = userService.update(user.getId(), userMapper.toUser(req));

            return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                    .success(true)
                    .message("Successfully updated user")
                    .data(userMapper.toDto(user))
                    .build());
        } catch (Exception e) {
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

        try {
            userService.delete(user.getId());

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Successfully deleted user")
                    .data(null)
                    .build());
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete user", e);
        }
    }

    // Публичные маршруты

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<?>> userById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        if (user.isEmpty()) {
            throw new UserNotFoundException("Can't find user with ID: " + id);
        }

        UserDto response = userMapper.toDto(user.get());

        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    @GetMapping("/tg/{id}")
    public ResponseEntity<ApiResponse<?>> userByTgId(@PathVariable Long id) {
        Optional<User> user = userService.findByTelegramId(id);

        if (user.isEmpty()) {
            throw new UserNotFoundException("Can't find user with Telegram ID: " + id);
        }

        UserDto response = userMapper.toDto(user.get());

        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    @GetMapping("/search}")
    public ResponseEntity<ApiResponse<List<User>>> searchUsersByUsername(@RequestParam String username) {
        List<User> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(ApiResponse.<List<User>>builder()
                .success(true)
                .message("Users found")
                .data(users)
                .build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<?>> userByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException("Can't find user with username: " + username);
        }

        UserDto response = userMapper.toDto(user.get());

        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched user info")
                .data(response)
                .build());
    }

    // Администраторские маршруты

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<?>> allUsers() {
        List<UserDto> allUsers = userService.findAll().stream().map(userMapper::toDto).toList();
        return ResponseEntity.ok(ApiResponse.<List<UserDto>>builder()
                .success(true)
                .message("Fetched all registered users")
                .data(allUsers)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/save")
    public ResponseEntity<ApiResponse<?>> saveUser(@RequestBody UserDto req) {
        try {
            User user = userService.create(userMapper.toUser(req));
            userProducer.sendUser(user);

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
    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long id, @RequestBody UserDto req) {
        try {
            User user = userService.update(id, userMapper.toUser(req));

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
