package com.LakePayProj.userService.service;

import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final IUserRepository userRepository;

    /**
     * Создание пользователя
     */
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Обновление данных пользователя
     * @param updatedUser поля avatarUrl, username и role будут обновлены в БД
     * @throws EntityNotFoundException в случае когда пользователя не существует
     */
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setAvatarUrl(updatedUser.getAvatarUrl());
            user.setRole(updatedUser.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("Can't find user with ID: " + id));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    public Optional<User> findUserByTelegramId(Long id) {
        return userRepository.findByTelegramId(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        Optional<User> user = userRepository.findUserById(id);
        user.ifPresent(userRepository::delete);
    }
}
