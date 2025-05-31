package com.lakepayProj.userService.application.services;

import com.lakepayProj.userService.api.DTOs.JwtAuthenticationResponse;
import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.kafka.UserProducer;
import com.lakepayProj.userService.auth.TelegramAuthenticationManager;
import com.lakepayProj.userService.domain.model.TelegramAuthenticationToken;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final TelegramAuthenticationManager authenticationManager;
    private final IUserMapper mapper;
    private final ConcurrentHashMap<Long, Long> chats= new ConcurrentHashMap<>();
    private final UserProducer producer;

    // The chat ids are stored in memory. Why do we even need this?
    @KafkaListener(topics = "userTgChatId", groupId = "user-notifications")
    public void getTgChatId(ConsumerRecord<String, String> record) {
        chats.put(Long.valueOf(record.key()), Long.valueOf(record.value()));
    }

    /**
     * Аутентификация пользователя через аккаунт Telegram
     *
     * @param request данные возвращаемые Telegram
     * @return токен
     */
    public JwtAuthenticationResponse authenticateTelegram(Map<String, String> request) {
        Authentication auth = new TelegramAuthenticationToken(request);
        authenticationManager.authenticate(auth);

        Long tgId = Long.parseLong(request.get("id"));
        User user = userService.findUserByTgId(tgId);

        Long chatId = chats.get(tgId);
        if (chatId == null) {
            return new JwtAuthenticationResponse("Chat id is null", null);
        }

        if (user != null) {
            var jwt = jwtService.generateToken(user);
            return new JwtAuthenticationResponse("Logged in", jwt);
        } else {
            var userEntity = UserEntity.builder()
                    .tgId(Long.parseLong(request.get("id")))
                    .username(request.get("username"))
                    .urlPhoto(request.get("photo_url"))
                    .dateOfReg(LocalDate.now())
                    .balance(new BigDecimal(0))
                    .subscriptions(List.of())
                    .role(Role.User)
                    .build();

            user = mapper.userEntityToUser(userEntity);
            userService.saveUser(user);

            producer.sendUser(user);

            var jwt = jwtService.generateToken(user);
            return new JwtAuthenticationResponse("Logged in", jwt);
        }
    }
}