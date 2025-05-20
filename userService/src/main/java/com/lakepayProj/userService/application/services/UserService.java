package com.lakepayProj.userService.application.services;

import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.interfaces.repos.IUserRepository;
import com.lakepayProj.userService.application.interfaces.services.IUserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
                case "userName" -> userById.setUserName((String) value);
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
                default -> {
                    throw new IllegalArgumentException("Unknown field to update");
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
    public void subscribe(Long id, String category) {
        UserEntity userById = repository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with tgId [" + id + "] not found");
        }

        List<String> subscriptions = userById.getSubscriptions();

        if (subscriptions.contains(category)) {
            System.err.println("Пользователь уже подписан на эту категорию!");
            return;
        }

        subscriptions.add(category);
        userById.setSubscriptions(subscriptions);
        repository.saveAndFlush(userById);
    }

    @Override
    public void unSubscribe(Long id, String category) {
        UserEntity userById = repository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with id = [" + id + "] is null");
        }
        List<String> subscriptions = userById.getSubscriptions();
        subscriptions.removeIf(category1 -> category1.equals(category));
        userById.setSubscriptions(subscriptions);
        repository.saveAndFlush(userById);
    }

    @Override
    public List<User> findUserBySubs(String category) {
        List<UserEntity> all = repository.findAll();
        return all.stream()
                .filter(userEntity -> userEntity.getSubscriptions().stream().anyMatch(category1 -> category1.equals(category)))
                .map(mapper ::userEntityToUser)
                .toList();
    }

    @Override
    public List<String> getCategoriesById(Long tgId) {
        UserEntity userById = repository.findUserById(tgId);
        return mapper.userEntityToUser(userById).getSubscriptions();
    }
}
