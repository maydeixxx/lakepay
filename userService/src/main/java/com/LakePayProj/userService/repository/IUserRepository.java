package com.LakePayProj.userService.repository;

import com.LakePayProj.userService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findUsersByUsernameLike(String username);

    Optional<User> findByTelegramId(Long telegramId);

    Optional<User> findByUsername(String username);

}
