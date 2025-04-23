package com.lakepayProj.userService.api.controller;

import com.lakepayProj.userService.api.DTOs.UserDTO;
import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.services.UserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {
    private final IUserMapper mapper;
    private final UserService service;

    @GetMapping("/all_users")
    public ResponseEntity<List<UserDTO>> allUsers() {
        List<User> allUsers = service.findAllUsers();
        List<UserDTO> list = allUsers.stream()
                .map(mapper::userToUserDTO)
                .toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/save_user")
    public ResponseEntity<Void> saveUser(@RequestBody UserDTO userDTO) {
        User userByTgId = service.findUserByTgId(userDTO.getTgId());
        if (userByTgId != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "user already exists!");
        } else {
            service.saveUser(mapper.userDTOToUser(userDTO));
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @PatchMapping("/update_user/{id}")
    public ResponseEntity<Void> updatesUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        service.updateUser(id, updates);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUserByID(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable Long id) {
        User userDom = service.findUserById(id);
        UserDTO userDTO = mapper.userToUserDTO(userDom);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/{role}")
    public ResponseEntity<List<UserDTO>> findUsersByRole(@PathVariable Role role) {
        List<User> usersByRole = service.findUsersByRole(role);
        List<UserDTO> usersDTO = usersByRole.stream()
                .map(mapper::userToUserDTO)
                .toList();
        return new ResponseEntity<>(usersDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> findUserBySubs(@PathVariable String category) {
        List<User> userBySubs = service.findUserBySubs(category);
        List<UserDTO> list = userBySubs.stream()
                .map(mapper::userToUserDTO)
                .toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
