package com.LakePayProj.userService.controller;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.dto.request.TelegramAuthRequest;
import com.LakePayProj.userService.dto.response.ApiResponse;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.exception.BadRequestException;
import com.LakePayProj.userService.exception.DatabaseException;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.kafka.UserProducer;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.security.TelegramAuthenticationToken;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/users/self")
@RequiredArgsConstructor
public class UserSelfController {

    private final IUserMapper userMapper;
    private final UserService userService;
    private final UserProducer userProducer;
    private final AuthenticationManager authManager;

    @GetMapping()
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

    @PostMapping("/connect-telegram")
    public ResponseEntity<ApiResponse<?>> connectTelegram(@AuthenticationPrincipal UserDetails userDetails, @RequestBody TelegramAuthRequest req) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

        // Проверка привязки телеграм аккаунта
        userService.findByTelegramId(req.id()).ifPresent((u) -> {
            throw new BadRequestException("This Telegram account is already connected to " + u.getUsername());
        });

        // Проверка валидности данных телеграм
        authManager.authenticate(new TelegramAuthenticationToken(req.toMap()));

        user.setTelegramId(req.id());
        user = userService.update(user.getId(), user);
        userProducer.publishUpdateUser(user);

        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Successfully connected telegram account")
                .data(userMapper.toDto(user))
                .build());
    }

    @PostMapping("/disconnect-telegram")
    public ResponseEntity<ApiResponse<?>> disconnectTelegram(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

        // Проверка привязки телеграм аккаунта
        if (user.getTelegramId() == null) {
            throw new BadRequestException("This account doesn't have a connected Telegram account");
        }

        user.setTelegramId(null);
        user = userService.update(user.getId(), user);
        userProducer.publishUpdateUser(user);

        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Successfully disconnected telegram account")
                .data(userMapper.toDto(user))
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDto req) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

        try {
            user = userService.updateProfile(user.getId(), req.fullName(), req.avatarUrl());
            userProducer.publishUpdateUser(user);

            return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                    .success(true)
                    .message("Successfully updated user")
                    .data(userMapper.toDto(user))
                    .build());
        } catch (Exception e) {
            log.debug("Failed to update user", e);
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));

            userService.delete(user.getId());
            userProducer.publishDeleteUser(user.getId());

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Successfully deleted user")
                    .data(null)
                    .build());
        } catch (Exception e) {
            log.debug("Failed to delete user", e);
            throw new DatabaseException("Failed to delete user", e);
        }
    }
}
