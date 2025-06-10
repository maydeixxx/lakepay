package com.LakePayProj.userService.api.controller;

import com.LakePayProj.userService.api.DTOs.UserDTO;
import com.LakePayProj.userService.api.DTOs.UserUpdateDTO;
import com.LakePayProj.userService.application.interfaces.mappers.IUserMapper;
import com.LakePayProj.userService.application.interfaces.repos.IRoleRepository;
import com.LakePayProj.userService.application.kafka.UserProducer;
import com.LakePayProj.userService.application.services.UserService;
import com.LakePayProj.userService.domain.model.User;
import com.LakePayProj.userService.domain.valueObject.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {
    private final IUserMapper mapper;
    private final UserService userService;
    private final UserProducer producer;
    private final IRoleRepository roleRepository;

    @PostMapping("/add_role")
    public ResponseEntity<?> saveRole(@RequestBody List<Role> roles) {
        try {
            roleRepository.saveAll(roles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get_roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            return ResponseEntity.ok(roleRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping( "/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> data) {
        try {
            Long id = Long.valueOf(data.get("id").toString());
            String category = data.get("category").toString();
            User userById = userService.findUserById(id);
            Long chatId = userById.getChatId();
            userService.subscribe(id, category);
            producer.sendInfoAboutSub(chatId, category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Не удалось подписаться" + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, Object> data) {
        try {
            Long id = Long.valueOf(data.get("id").toString());
            String category = data.get("category").toString();
            User userById = userService.findUserById(id);
            Long chatId = userById.getChatId();
            userService.unSubscribe(id, category);
            producer.sendInfoAboutUnSub(chatId, category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Не удалось подписаться" + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all_users")
    public ResponseEntity<List<UserDTO>> allUsers() {
        List<User> allUsers = userService.findAllUsers();
        List<UserDTO> list = allUsers.stream()
                .map(mapper::userToUserDTO)
                .toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/categoriesById/{id}")
        public ResponseEntity<List<String>> findCategoriesByTgId(@PathVariable Long id) {
        User userByTgId = userService.findUserByTgId(id);
        List<String> subscriptions = userByTgId.getSubscriptions();
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @PostMapping("/save_user")
    public ResponseEntity<Void> saveUser(@RequestBody UserDTO userDTO) {
        User userByTgId = userService.findUserByTgId(userDTO.getTgId());
        if (userByTgId != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "user already exists!");
        } else {
            userService.saveUser(mapper.userDTOToUser(userDTO));
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping("/update_user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updates) {
        try {
            userService.updateUser(id, updates);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserByID(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user_id/{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable Long id) {
        User userDom = userService.findUserById(id);
        UserDTO userDTO = mapper.userToUserDTO(userDom);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

//    @GetMapping("user_role/{role}")
//    public ResponseEntity<List<UserDTO>> findUsersByRole(@PathVariable Role role) {
//        List<User> usersByRole = service.findUsersByRole(role);
//        List<UserDTO> usersDTO = usersByRole.stream()
//                .map(mapper::userToUserDTO)
//                .toList();
//        return new ResponseEntity<>(usersDTO, HttpStatus.OK);
//    }

    @GetMapping(value = "/user_category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> findUserBySubs(@PathVariable String category) {
        List<User> userBySubs = userService.findUserBySubs(category);
        List<UserDTO> list = userBySubs.stream()
                .map(mapper::userToUserDTO)
                .toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/user_tg/{id}")
    public ResponseEntity<UserDTO> findUserByTgID(@PathVariable Long id) {
        User userByTgId = userService.findUserByTgId(id);
        UserDTO userDTO = mapper.userToUserDTO(userByTgId);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}
