package com.lakepayProj.userService.application.interfaces.repos;

import com.lakepayProj.userService.infrastructure.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<UserEntity, Long>{
    UserEntity findUserById(Long id);
    void deleteUserById(Long id);
//    List<UserEntity> findUsersByRoles(Role role);
    UserEntity findUserByTgId(Long id);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
