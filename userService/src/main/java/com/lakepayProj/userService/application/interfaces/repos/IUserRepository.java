package com.lakepayProj.userService.application.interfaces.repos;

import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserRepository extends JpaRepository<UserEntity, Long>{
    UserEntity findUserById(Long id);
    void deleteUserById(Long id);
    List<UserEntity> findUsersByRole(Role role);
}
