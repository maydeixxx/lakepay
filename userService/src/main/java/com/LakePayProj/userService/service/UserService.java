package com.LakePayProj.userService.service;

import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final IUserRepository userRepository;

    /**
     * Создание пользователя
     */
    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }


    @Transactional
    public User update(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            if (updatedUser.getFullName() != null) user.setFullName(updatedUser.getFullName());
            if (updatedUser.getAvatarUrl() != null) user.setAvatarUrl(updatedUser.getAvatarUrl());
            if (updatedUser.getRole() != null) user.setRole(updatedUser.getRole());
            if (updatedUser.getCredential() != null) user.setCredential(updatedUser.getCredential());
            if (updatedUser.getTelegramId() != null) user.setTelegramId(updatedUser.getTelegramId());
            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("Can't find user with ID: " + id));
    }

    /**
     * Обновление публичных данных пользователя
     * @param fullName новое имя пользователя
     * @param avatarUrl ссылка на аватарку профиля
     * @throws EntityNotFoundException в случае когда пользователя не существует
     */
    public User updateProfile(Long id, String fullName, String avatarUrl) {
        return userRepository.findById(id).map(user -> {
            if (fullName != null) user.setFullName(fullName);
            if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
            return userRepository.save(user);
        }).orElseThrow(() -> new EntityNotFoundException("Can't find user with ID: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByTelegramId(Long id) {
        return userRepository.findByTelegramId(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<Optional<User>> findUsersByCategory(String category) {
        return userRepository.findAll().stream()
                .filter(user -> user.getCategories().contains(category))
                .map(Optional::ofNullable)
                .toList();
    }

    @Transactional
    public List<User> searchUsersByUsername(String username) {
        List<User> users = userRepository.findUsersByUsernameLike(username);
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found with username containing: " + username);
        }
        return users;
    }

    /**
     * Удаление пользователя по id
     *
     * @param id идентификатор пользователя в БД
     * @throws EntityNotFoundException в случае когда пользователя не существует
     */
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Can't find user with ID: " + id));
        userRepository.delete(user);
    }
}
