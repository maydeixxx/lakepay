package com.lakepayProj.userService.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.userService.api.DTOs.UserDTO;
import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.kafka.UserProducer;
import com.lakepayProj.userService.application.services.UserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {
    private final IUserMapper mapper;
    private final UserService service;
    private final ConcurrentHashMap<Long, Long> hash = new ConcurrentHashMap<>();
    private final UserProducer producer;
    private final ObjectMapper objectMapper;

    @PatchMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> data) {
        try {
            Long id = Long.valueOf(data.get("id").toString());
            String category = data.get("category").toString();
            User userById = service.findUserById(id);
            Long chatId = userById.getChatId();
            if (chatId == null || category == null) {
                return ResponseEntity.badRequest().body("ChatId or category cant be null");
            }
            service.subscribe(id, category);
            producer.sendInfoAboutSub(chatId, category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Не удалось подписаться" + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, Object> data) {
        try {
            Long id = Long.valueOf(data.get("id").toString());
            String category = data.get("category").toString();
            User userById = service.findUserById(id);
            Long chatId = userById.getChatId();
            service.unSubscribe(id, category);
            if (chatId == null || category == null) {
                return ResponseEntity.badRequest().body("ChatId or category cant be null");
            }
            producer.sendInfoAboutUnSub(chatId, category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Не удалось отписаться" + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all_users")
    public ResponseEntity<?> allUsers() {
        try {
            List<User> allUsers = service.findAllUsers();
            if (allUsers.isEmpty()) {
                return ResponseEntity.badRequest().body("List is empty");
            }
            List<UserDTO> list = allUsers.stream()
                    .map(mapper::userToUserDTO)
                    .toList();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/categoriesById/{id}")
    public ResponseEntity<?> findCategoriesByTgId(@PathVariable Long id) {
        try {
            User userByTgId = service.findUserByTgId(id);
            if (userByTgId == null) {
                return ResponseEntity.badRequest().body("There is no person with id = {" + id + "}");
            }
            List<String> subscriptions = userByTgId.getSubscriptions();
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/save_user")
    public ResponseEntity<?> saveUser(@RequestBody UserDTO userDTO) {
        try {
            User userByTgId = service.findUserByTgId(userDTO.getTgId());
            if (userByTgId != null) {
                return ResponseEntity.badRequest().body("user already exists!");
            } else {
                service.saveUser(mapper.userDTOToUser(userDTO));
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/update_user/{id}")
    public ResponseEntity<?> updatesUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            service.updateUser(id, updates);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = service.findUserById(id);
            if (user == null) {
                return ResponseEntity.badRequest().body("There is no person with id = {" + id + "}");
            }
            service.deleteUserByID(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user_id/{id}")
    public ResponseEntity<?> findUserById(@PathVariable Long id) {
        try {
            User userDom = service.findUserById(id);
            if (userDom == null) {
                return ResponseEntity.badRequest().body("There is no person with id = {" + id + "}");
            }
            UserDTO userDTO = mapper.userToUserDTO(userDom);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

    @GetMapping("user_role/{role}")
    public ResponseEntity<?> findUsersByRole(@PathVariable Role role) {
        try {
            List<User> usersByRole = service.findUsersByRole(role);
            if (usersByRole.isEmpty()) {
                return ResponseEntity.badRequest().body("There are no persons with role = {" + role + "}");
            }
            List<UserDTO> usersDTO = usersByRole.stream()
                    .map(mapper::userToUserDTO)
                    .toList();
            return new ResponseEntity<>(usersDTO, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/user_category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findUserBySubs(@PathVariable String category) {
        try {
            List<User> userBySubs = service.findUserBySubs(category);
            if (userBySubs.isEmpty()) {
                return ResponseEntity.badRequest().body("There are no persons with category = {" + category + "}");
            }
            List<UserDTO> list = userBySubs.stream()
                    .map(mapper::userToUserDTO)
                    .toList();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

    @GetMapping("/user_tg/{id}")
    public ResponseEntity<?> findUserByTgID(@PathVariable Long id) {
        try {
            User userByTgId = service.findUserByTgId(id);
            if (userByTgId == null) {
                return ResponseEntity.badRequest().body("There is no person with id = {" + id + "}");
            }
            UserDTO userDTO = mapper.userToUserDTO(userByTgId);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("There is no person with id = {" + id + "}");
        }

    }

    @GetMapping("auth/telegram")
    public ResponseEntity<Resource> getAuthScript() {
        Resource resource = new ClassPathResource("static/tgAuth.html");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=telegramAuth.html");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private boolean telegramDataIsValid(Map<String, Object> telegramData) {
        String hash = (String) telegramData.get("hash");
        if (hash == null) {
            throw new IllegalArgumentException("Hash is missing from telegram data");
        }

        telegramData.remove("hash");

        if (telegramData.isEmpty()) {
            throw new IllegalArgumentException("Telegram data is empty");
        }

        StringBuilder sb = new StringBuilder();
        telegramData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"));

        if (!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }

        String dataCheckString = sb.toString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String tgBotToken = "7906616449:AAGLMQphhjOTHCgyAW9d9xlV94vN-Deai54";
            byte[] key = digest.digest(tgBotToken.getBytes(UTF_8));

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(secretKeySpec);

            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(UTF_8));
            StringBuilder validateHash = new StringBuilder();
            for (byte b : hmacBytes) {
                validateHash.append(String.format("%02x", b));
            }

            return hash.contentEquals(validateHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }

    @KafkaListener(topics = "userTgChatId", groupId = "user-notifications")
    public void getTgChatId(ConsumerRecord<String, String> record) {
        hash.put(Long.valueOf(record.key()), Long.valueOf(record.value()));
    }

    @PostMapping("auth/telegram/token")
    public String authenticate(@RequestBody Map<String, Object> telegramData) {
        System.out.println("Полученные данные: " + telegramData);

        if (telegramDataIsValid(telegramData)) {
            Long tgId = Long.valueOf((Integer) telegramData.get("id"));

            String userName = (String) telegramData.get("username");
            String urlPhoto = (String) telegramData.get("photo_url");
            Long chatId = hash.get(tgId);
            if (chatId == null) {
                return "Chat id is null";
            }

            User user = service.findUserByTgId(tgId);
            if (user != null) {
                return "User already exists";
            } else {
                UserEntity createUser = new UserEntity();
                createUser.setTgId(tgId);
                createUser.setChatId(chatId);
                createUser.setUserName(userName);
                createUser.setUrlPhoto(urlPhoto);
                createUser.setDateOfReg(LocalDate.now());
                createUser.setBalance(new BigDecimal(0));
                createUser.setRole(Role.User);
                createUser.setSubscriptions(List.of());

                User newUser = mapper.userEntityToUser(createUser);
                service.saveUser(newUser);

                producer.sendUser(newUser);

                return "User logged in successfully!";
            }
        }
        return "error";
    }
}
