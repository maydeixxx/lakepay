package com.lakepayProj.userService.application.services;

import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.interfaces.repos.IUserRepository;
import com.lakepayProj.userService.application.interfaces.services.IUserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void updateUser(User user) {
        UserEntity userEntity = mapper.userToUserEntity(user);
        repository.save(userEntity);
    }


    @Override
    public User findUserById(Long id) {
        return mapper.userEntityToUser(repository.findUserById(id));
    }

    @Override
    public void deleteUserByID(Long id) {
        UserEntity userById = repository.findUserById(id);
        repository.delete(userById);
    }

    @Override
    public void saveUser(User user) {
        repository.save(mapper.userToUserEntity(user));
    }

    @Override
    public User findUserByRole(String role) {
        return mapper.userEntityToUser(repository.findUserByRole(role));
    }
}
