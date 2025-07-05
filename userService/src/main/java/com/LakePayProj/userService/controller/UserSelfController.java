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

    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        User user = userService.findById(Long.parseLong(userId)).orElseThrow(() -> new UserNotFoundException("Can't find user with ID: " + userId));
        UserDto response = userMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message("Fetched current user data")
                .data(response)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDto req) {
        String userId = userDetails.getUsername();
        User user = userService.findById(Long.parseLong(userId)).orElseThrow(() -> new UserNotFoundException("Can't find user with ID: " + userId));

        try {
            user = userService.update(user.getId(), userMapper.toUser(req));
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

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());

            userService.delete(userId);
            userProducer.publishDeleteUser(userId);

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
