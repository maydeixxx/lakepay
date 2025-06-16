package com.LakePayProj.userService.repository;

import com.LakePayProj.userService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long>{
    Optional<User> findUserById(Long id);
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByUsername(String username);
    void deleteUserById(Long id);
}
