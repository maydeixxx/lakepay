package com.lakepayProj.userService.application.interfaces.repos;

import com.lakepayProj.userService.infrastructure.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<UserEntity, Long>{
    UserEntity findUserById(Long id);
    void deleteUserById(Long id);
    UserEntity findUserByRole(String role);
}
