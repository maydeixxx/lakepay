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
            }
        });
        repository.save(userById);
    }


    @Override
    public User findUserById(Long id) {
        return mapper.userEntityToUser(repository.findUserById(id));
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
}
