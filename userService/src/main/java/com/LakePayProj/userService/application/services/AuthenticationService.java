package com.LakePayProj.userService.application.services;

import com.LakePayProj.userService.api.DTOs.JwtAuthenticationResponse;
import com.LakePayProj.userService.application.interfaces.mappers.IUserMapper;
import com.LakePayProj.userService.application.interfaces.repos.IRoleRepository;
import com.LakePayProj.userService.application.kafka.UserProducer;
import com.LakePayProj.userService.auth.TelegramAuthenticationManager;
import com.LakePayProj.userService.domain.model.TelegramAuthenticationToken;
import com.LakePayProj.userService.domain.model.User;
import com.LakePayProj.userService.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ConcurrentHashMap<Long, Long> chats = new ConcurrentHashMap<>();
    private final UserProducer producer;
    private final IRoleRepository roleRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Аутентификация пользователя через аккаунт Telegram
     *
     * @param request данные возвращаемые Telegram
     * @return токен
     */
    public JwtAuthenticationResponse authenticateTelegram(Map<String, String> request) {
        logger.info("Получены данные от Telegram: {}", request);

        // Проверка входных данных
        if (request == null || !request.containsKey("id") || request.get("id") == null) {
            logger.error("Некорректные данные Telegram: id отсутствует");
            return new JwtAuthenticationResponse("Invalid Telegram data", null);
        }

        try {
            Authentication auth = new TelegramAuthenticationToken(request);
            authenticationManager.authenticate(auth);

            Long tgId = Long.parseLong(request.get("id"));
            User user = userService.findUserByTgId(tgId);

            String jwt = null;
            if (user != null) {
                jwt = jwtService.generateToken(user);
                logger.info("Токен сгенерирован для существующего пользователя {}: {}", user.getUsername(), jwt);
            } else {
                UserEntity userEntity = UserEntity.builder()
                        .tgId(tgId)
                        .chatId(tgId)
                        .username(request.get("username"))
                        .urlPhoto(request.get("photo_url"))
                        .dateOfReg(LocalDate.now())
                        .balance(new BigDecimal(0))
                        .subscriptions(List.of())
                        .roles(List.of(roleRepository.findRoleByName("ROLE_USER")
                                .orElseThrow(() -> new IllegalStateException("Роль ROLE_USER не найдена"))))
                        .build();

                user = mapper.userEntityToUser(userEntity);
                userService.saveUser(user);

                user = userService.findUserByTgId(tgId);
                logger.info("Создан новый пользователь: {}", user.getUsername());

                producer.sendUser(user);
                jwt = jwtService.generateToken(user);
                logger.info("Токен сгенерирован для нового пользователя {}: {}", user.getUsername(), jwt);
            }

            return new JwtAuthenticationResponse("Logged in", jwt);
        } catch (NumberFormatException e) {
            logger.error("Ошибка при парсинге tgId: {}", request.get("id"), e);
            return new JwtAuthenticationResponse("Invalid Telegram ID format", null);
        } catch (Exception e) {
            logger.error("Ошибка аутентификации Telegram: {}", e.getMessage(), e);
            return new JwtAuthenticationResponse("Authentication failed", null);
        }
    }
}