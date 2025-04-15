package com.lakepayProj.userService.application.services;

import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.interfaces.repos.IUserRepository;
import com.lakepayProj.userService.application.interfaces.services.IUserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository repository;
    private final IUserMapper mapper;

    @Override
    public List<User> findAllUsers() {
        List<UserEntity> users = repository.findAll();
        return users.stream()
                .map(mapper::userEntityToUser)
                .toList();
    }

    @Override
    @Transactional
    public void updateUser(Long id, Map<String, Object> updates) {
        UserEntity userById = repository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with id [" + id + "] don`t found");
        }
        updates.forEach((key, value) -> {
            switch (key) {
                case "userName" -> userById.setUsername((String) value);
                case "urlPhoto" -> userById.setUrlPhoto((String) value);
                case "balance" -> userById.setBalance(BigDecimal.valueOf((Double) value));
                case "role" -> userById.setRole(Role.valueOf(value.toString()));
                case "adSub" -> {
                    List<String> subscriptions = userById.getSubscriptions();
                    subscriptions.addLast(value.toString());
                    userById.setSubscriptions(subscriptions);
                }
                case "delSub" -> {
                    List<String> subscriptions = userById.getSubscriptions();
                    subscriptions.removeIf(category -> category.equals(value.toString()));
                    userById.setSubscriptions(subscriptions);
                }
            }
        });
        repository.save(userById);
    }


    @Override
    public User findUserById(Long id) {
        return mapper.userEntityToUser(repository.findUserById(id));
    }

    @Override
    public User findUserByTgId(Long id) {
        UserEntity userByTgId = repository.findUserByTgId(id);
        return mapper.userEntityToUser(userByTgId);
    }

    @Override
    @Transactional
    public void deleteUserByID(Long id) {
        UserEntity userById = repository.findUserById(id);
        repository.delete(userById);
    }

    /**
     * Создание пользователя
     */
    @Override
    public void saveUser(User user) {
        repository.save(mapper.userToUserEntity(user));
    }

    @Override
    public List<User> findUsersByRole(Role role) {
        List<UserEntity> usersByRole = repository.findUsersByRole(role);
        return usersByRole.stream()
                .map(mapper::userEntityToUser)
                .toList();
    }

    @Override
    public void subscribe(Long tgId, String category) {
        UserEntity userByTgId = repository.findUserByTgId(tgId);
        List<String> subscriptions = userByTgId.getSubscriptions();
        subscriptions.addLast(category);
        userByTgId.setSubscriptions(subscriptions);
        repository.saveAndFlush(userByTgId);
    }

    @Override
    public void unSubscribe(Long tgId, String category) {
        UserEntity userByTgId = repository.findUserByTgId(tgId);
        List<String> subscriptions = userByTgId.getSubscriptions();
        subscriptions.removeIf(category1 -> category1.equals(category));
        userByTgId.setSubscriptions(subscriptions);
        repository.saveAndFlush(userByTgId);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        UserEntity userEntity = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapper.userEntityToUser(userEntity);
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужно для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Получение текущего пользователя из контекста Spring Security
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }
}
